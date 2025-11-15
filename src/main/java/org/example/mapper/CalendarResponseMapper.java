package org.example.mapper;

import java.util.List;

import org.example.dto.response.CalendarResponse;
import org.example.dto.response.EventResponse;
import org.example.dto.response.InfoResponse;
import org.example.persistence.entity.CalendarEntity;
import org.example.persistence.entity.EventEntity;
import org.example.persistence.entity.InfoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between CalendarEntity and CalendarResponse DTOs.
 */
@Mapper(componentModel = "spring")
public interface CalendarResponseMapper {
    /**
     * Maps a CalendarEntity to a CalendarResponse DTO.
     *
     * @param entity the entity to map
     * @return the mapped response DTO
     */
    @Mapping(target = "metadata", source = "metadata")
    @Mapping(target = "events", source = "events")
    CalendarResponse toResponse(CalendarEntity entity);

    /**
     * Maps a CalendarResponse DTO to a CalendarEntity.
     *
     * @param response the response DTO to map
     * @return the mapped entity
     */
    @Mapping(target = "metadata", source = "metadata")
    @Mapping(target = "events", source = "events")
    CalendarEntity toEntity(CalendarResponse response);

    /**
     * Maps an InfoEntity to an InfoResponse.
     *
     * @param infoEntity the info entity to map
     * @return the mapped info response
     */
    InfoResponse toInfoResponse(InfoEntity infoEntity);

    /**
     * Maps an InfoResponse to an InfoEntity.
     *
     * @param infoResponse the info response to map
     * @return the mapped info entity
     */
    InfoEntity toInfoEntity(InfoResponse infoResponse);

    /**
     * Maps an EventEntity to an EventResponse.
     *
     * @param eventEntity the event entity to map
     * @return the mapped event response
     */
    EventResponse toEventResponse(EventEntity eventEntity);

    /**
     * Maps a list of EventEntity to a list of EventResponse.
     *
     * @param eventEntities the list of event entities to map
     * @return the list of mapped event responses
     */
    List<EventResponse> toEventResponseList(List<EventEntity> eventEntities);
}

