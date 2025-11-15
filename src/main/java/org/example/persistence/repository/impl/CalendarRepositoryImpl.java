package org.example.persistence.repository.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.example.model.CalendarState;
import org.example.model.CalendarVisibility;
import org.example.model.EventType;
import org.example.persistence.entity.Calendar;
import org.example.persistence.entity.CalendarEvent;
import org.example.persistence.entity.CalendarMetadata;
import org.example.persistence.repository.CalendarRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JDBC implementation of CalendarRepository using PostgreSQL.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CalendarRepositoryImpl implements CalendarRepository {
    private final JdbcTemplate jdbcTemplate;

    private static final String UPSERT_CALENDAR = """
        INSERT INTO calendars (id, name, description, status, visibility, created_at, created_by, updated_at, updated_by, count)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT (id) DO UPDATE SET
            name = EXCLUDED.name,
            description = EXCLUDED.description,
            status = EXCLUDED.status,
            visibility = EXCLUDED.visibility,
            updated_at = EXCLUDED.updated_at,
            updated_by = EXCLUDED.updated_by,
            count = EXCLUDED.count
        """;

    private static final String UPSERT_EVENT = """
        INSERT INTO events (id, calendar_id, name, description, type, disabled, all_day, start_datetime, end_datetime, 
                           location, created_at, created_by, updated_at, updated_by)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT (id) DO UPDATE SET
            calendar_id = EXCLUDED.calendar_id,
            name = EXCLUDED.name,
            description = EXCLUDED.description,
            type = EXCLUDED.type,
            disabled = EXCLUDED.disabled,
            all_day = EXCLUDED.all_day,
            start_datetime = EXCLUDED.start_datetime,
            end_datetime = EXCLUDED.end_datetime,
            location = EXCLUDED.location,
            updated_at = EXCLUDED.updated_at,
            updated_by = EXCLUDED.updated_by
        """;

    private static final String SELECT_CALENDAR = """
        SELECT id, name, description, status, visibility, created_at, created_by, updated_at, updated_by, count
        FROM calendars
        WHERE id = ?
        """;

    private static final String SELECT_ALL_CALENDARS = """
        SELECT id, name, description, status, visibility, created_at, created_by, updated_at, updated_by, count
        FROM calendars
        ORDER BY created_timestamp DESC
        """;

    private static final String SELECT_EVENTS_BY_CALENDAR = """
        SELECT id, name, description, type, disabled, all_day, start_datetime, end_datetime, 
               location, created_at, created_by, updated_at, updated_by
        FROM events
        WHERE calendar_id = ?
        ORDER BY start_datetime ASC
        """;

    private static final String DELETE_CALENDAR = "DELETE FROM calendars WHERE id = ?";
    private static final String DELETE_EVENTS = "DELETE FROM events WHERE calendar_id = ?";
    private static final String DELETE_ALL_EVENTS = "DELETE FROM events";
    private static final String DELETE_ALL_CALENDARS = "DELETE FROM calendars";
    private static final String EXISTS_CALENDAR = "SELECT EXISTS(SELECT 1 FROM calendars WHERE id = ?)";
    private static final String COUNT_CALENDARS = "SELECT COUNT(*) FROM calendars";

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Calendar save(Calendar entity) {
        log.debug("Saving calendar to database: id={}, name={}", entity.getId(), entity.getName());
        // Save/update calendar
        CalendarMetadata metadata = entity.getMetadata();
        int calendarRows = jdbcTemplate.update(UPSERT_CALENDAR,
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            metadata != null && metadata.getStatus() != null ? metadata.getStatus().name().toLowerCase() : "unknown",
            metadata != null && metadata.getVisibility() != null ? metadata.getVisibility().name().toLowerCase() : "personal",
            metadata != null ? metadata.getCreatedAt() : null,
            metadata != null ? metadata.getCreatedBy() : null,
            metadata != null ? metadata.getUpdatedAt() : null,
            metadata != null ? metadata.getUpdatedBy() : null,
            metadata != null && metadata.getCount() != null ? metadata.getCount() : 0
        );
        log.debug("Calendar saved: {} row(s) affected", calendarRows);

        // Delete existing events and save new ones
        int deletedEvents = jdbcTemplate.update(DELETE_EVENTS, entity.getId());
        if (deletedEvents > 0) {
            log.debug("Deleted {} existing event(s) for calendar: id={}", deletedEvents, entity.getId());
        }
        
        // Save events
        int savedEvents = 0;
        if (entity.getEvents() != null && !entity.getEvents().isEmpty()) {
            for (CalendarEvent event : entity.getEvents()) {
                jdbcTemplate.update(UPSERT_EVENT,
                    event.getId(),
                    entity.getId(),
                    event.getName(),
                    event.getDescription(),
                    event.getType() != null ? event.getType().name().toLowerCase() : "other",
                    event.getDisabled() != null ? event.getDisabled() : false,
                    event.getAllDay() != null ? event.getAllDay() : false,
                    event.getStartDateTime() != null ? Timestamp.valueOf(event.getStartDateTime()) : null,
                    event.getEndDateTime() != null ? Timestamp.valueOf(event.getEndDateTime()) : null,
                    event.getLocation(),
                    event.getCreatedAt(),
                    event.getCreatedBy(),
                    event.getUpdatedAt(),
                    event.getUpdatedBy()
                );
                savedEvents++;
            }
            log.debug("Saved {} event(s) for calendar: id={}", savedEvents, entity.getId());
        }
        
        log.info("Calendar saved successfully: id={}, name={}, eventCount={}", 
            entity.getId(), entity.getName(), savedEvents);
        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteById(String id) {
        log.debug("Deleting calendar from database: id={}", id);
        // Delete events first (foreign key constraint)
        int deletedEvents = jdbcTemplate.update(DELETE_EVENTS, id);
        log.debug("Deleted {} event(s) for calendar: id={}", deletedEvents, id);
        // Delete calendar
        int deletedCalendars = jdbcTemplate.update(DELETE_CALENDAR, id);
        if (deletedCalendars > 0) {
            log.info("Calendar deleted from database: id={}, eventCount={}", id, deletedEvents);
        } else {
            log.warn("No calendar found to delete: id={}", id);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Calendar> findById(String id) {
        log.debug("Finding calendar in database: id={}", id);
        List<Calendar> calendars = jdbcTemplate.query(SELECT_CALENDAR, new CalendarRowMapper(), id);
        if (calendars.isEmpty()) {
            log.debug("Calendar not found in database: id={}", id);
            return Optional.empty();
        }
        Calendar calendar = calendars.get(0);
        // Load events
        List<CalendarEvent> events = jdbcTemplate.query(SELECT_EVENTS_BY_CALENDAR, new EventRowMapper(), id);
        calendar.setEvents(events);
        log.debug("Calendar found in database: id={}, name={}, eventCount={}", 
            id, calendar.getName(), events.size());
        return Optional.of(calendar);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Calendar> findAll() {
        log.debug("Finding all calendars in database");
        List<Calendar> calendars = jdbcTemplate.query(SELECT_ALL_CALENDARS, new CalendarRowMapper());
        log.debug("Found {} calendar(s) in database, loading events...", calendars.size());
        // Load events for each calendar
        for (Calendar calendar : calendars) {
            List<CalendarEvent> events = jdbcTemplate.query(SELECT_EVENTS_BY_CALENDAR, new EventRowMapper(), calendar.getId());
            calendar.setEvents(events);
        }
        log.debug("Loaded all calendars with events: totalCalendars={}", calendars.size());
        return calendars;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsById(String id) {
        log.debug("Checking if calendar exists: id={}", id);
        Boolean exists = jdbcTemplate.queryForObject(EXISTS_CALENDAR, Boolean.class, id);
        log.debug("Calendar exists check: id={}, exists={}", id, exists);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count() {
        log.debug("Counting calendars");
        Long count = jdbcTemplate.queryForObject(COUNT_CALENDARS, Long.class);
        log.debug("Calendar count: {}", count);
        return count != null ? count : 0L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteAll() {
        log.debug("Deleting all calendars from database");
        // Delete all events first (foreign key constraint)
        int deletedEvents = jdbcTemplate.update(DELETE_ALL_EVENTS);
        log.debug("Deleted {} event(s) from database", deletedEvents);
        // Delete all calendars
        int deletedCalendars = jdbcTemplate.update(DELETE_ALL_CALENDARS);
        log.info("Deleted all calendars from database: calendarCount={}, eventCount={}", 
            deletedCalendars, deletedEvents);
    }

    /**
     * RowMapper for CalendarEntity.
     */
    private static class CalendarRowMapper implements RowMapper<Calendar> {
        @Override
        public Calendar mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            CalendarMetadata metadata = new CalendarMetadata(
                parseCalendarState(rs.getString("status")),
                parseCalendarVisibility(rs.getString("visibility")),
                rs.getString("created_at"),
                rs.getString("created_by"),
                rs.getString("updated_at"),
                rs.getString("updated_by"),
                rs.getInt("count")
            );

            return new Calendar(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("description"),
                metadata,
                new ArrayList<>() // Events loaded separately
            );
        }

        private CalendarState parseCalendarState(String status) {
            if (status == null) {
                return CalendarState.UNKNOWN;
            }
            try {
                return CalendarState.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return CalendarState.UNKNOWN;
            }
        }

        private CalendarVisibility parseCalendarVisibility(String visibility) {
            if (visibility == null) {
                return CalendarVisibility.PERSONAL;
            }
            try {
                return CalendarVisibility.valueOf(visibility.toUpperCase());
            } catch (IllegalArgumentException e) {
                return CalendarVisibility.PERSONAL;
            }
        }
    }

    /**
     * RowMapper for EventEntity.
     */
    private static class EventRowMapper implements RowMapper<CalendarEvent> {
        @Override
        public CalendarEvent mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            Timestamp startTs = rs.getTimestamp("start_datetime");
            Timestamp endTs = rs.getTimestamp("end_datetime");
            LocalDateTime startDateTime = startTs != null ? startTs.toLocalDateTime() : null;
            LocalDateTime endDateTime = endTs != null ? endTs.toLocalDateTime() : null;

            return new CalendarEvent(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("description"),
                parseEventType(rs.getString("type")),
                rs.getBoolean("disabled"),
                rs.getBoolean("all_day"),
                startDateTime,
                endDateTime,
                rs.getString("location"),
                rs.getString("created_at"),
                rs.getString("created_by"),
                rs.getString("updated_at"),
                rs.getString("updated_by")
            );
        }

        private EventType parseEventType(String type) {
            if (type == null) {
                return EventType.OTHER;
            }
            try {
                return EventType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                return EventType.OTHER;
            }
        }
    }
}
