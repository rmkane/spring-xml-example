package org.example.mapper;

import java.util.List;

import org.example.dto.response.CalendarResponse;
import org.example.dto.response.EventResponse;
import org.example.dto.response.CalendarMetadataResponse;
import org.example.persistence.entity.Calendar;
import org.example.persistence.entity.CalendarEvent;
import org.example.persistence.entity.CalendarMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between Calendar and CalendarResponse DTOs.
 */
@Mapper(componentModel = "spring")
public interface CalendarResponseMapper {
    /**
     * Maps a Calendar to a CalendarResponse DTO.
     *
     * @param calendar the calendar entity to map
     * @return the mapped response DTO
     */
    @Mapping(target = "metadata", source = "metadata")
    @Mapping(target = "events", source = "events")
    CalendarResponse toResponse(Calendar calendar);

    /**
     * Maps a CalendarResponse DTO to a Calendar.
     *
     * @param response the response DTO to map
     * @return the mapped calendar entity
     */
    @Mapping(target = "metadata", source = "metadata")
    @Mapping(target = "events", source = "events")
    Calendar toEntity(CalendarResponse response);

    /**
     * Maps a CalendarMetadata to a CalendarMetadataResponse.
     *
     * @param calendarMetadata the calendar metadata to map
     * @return the mapped calendar metadata response
     */
    CalendarMetadataResponse toCalendarMetadataResponse(CalendarMetadata calendarMetadata);

    /**
     * Maps a CalendarMetadataResponse to a CalendarMetadata.
     *
     * @param calendarMetadataResponse the calendar metadata response to map
     * @return the mapped calendar metadata
     */
    CalendarMetadata toCalendarMetadata(CalendarMetadataResponse calendarMetadataResponse);

    /**
     * Maps a CalendarEvent to an EventResponse.
     *
     * @param calendarEvent the calendar event to map
     * @return the mapped event response
     */
    EventResponse toEventResponse(CalendarEvent calendarEvent);

    /**
     * Maps a list of CalendarEvent to a list of EventResponse.
     *
     * @param calendarEvents the list of calendar events to map
     * @return the list of mapped event responses
     */
    List<EventResponse> toEventResponseList(List<CalendarEvent> calendarEvents);
}

