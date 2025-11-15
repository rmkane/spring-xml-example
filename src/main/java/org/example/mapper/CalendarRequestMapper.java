package org.example.mapper;

import java.util.List;

import org.example.dto.request.CalendarRequest;
import org.example.dto.request.EventRequest;
import org.example.dto.request.CalendarMetadataRequest;
import org.example.persistence.entity.Calendar;
import org.example.persistence.entity.CalendarEvent;
import org.example.persistence.entity.CalendarMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between CalendarRequest DTOs and Calendar.
 */
@Mapper(componentModel = "spring")
public interface CalendarRequestMapper {
    /**
     * Maps a Calendar to a CalendarRequest DTO.
     *
     * @param calendar the calendar entity to map
     * @return the mapped request DTO
     */
    @Mapping(target = "metadata", source = "metadata")
    @Mapping(target = "events", source = "events")
    CalendarRequest toRequest(Calendar calendar);

    /**
     * Maps a CalendarRequest DTO to a Calendar.
     *
     * @param request the request DTO to map
     * @return the mapped calendar entity
     */
    @Mapping(target = "metadata", source = "metadata")
    @Mapping(target = "events", source = "events")
    Calendar toEntity(CalendarRequest request);

    /**
     * Maps a CalendarMetadata to a CalendarMetadataRequest.
     *
     * @param calendarMetadata the calendar metadata to map
     * @return the mapped calendar metadata request
     */
    CalendarMetadataRequest toCalendarMetadataRequest(CalendarMetadata calendarMetadata);

    /**
     * Maps a CalendarMetadataRequest to a CalendarMetadata.
     *
     * @param calendarMetadataRequest the calendar metadata request to map
     * @return the mapped calendar metadata
     */
    CalendarMetadata toCalendarMetadata(CalendarMetadataRequest calendarMetadataRequest);

    /**
     * Maps an EventRequest to a CalendarEvent.
     *
     * @param eventRequest the event request to map
     * @return the mapped calendar event
     */
    CalendarEvent toCalendarEvent(EventRequest eventRequest);

    /**
     * Maps a list of EventRequest to a list of CalendarEvent.
     *
     * @param eventRequests the list of event requests to map
     * @return the list of mapped calendar events
     */
    List<CalendarEvent> toCalendarEventList(List<EventRequest> eventRequests);
}

