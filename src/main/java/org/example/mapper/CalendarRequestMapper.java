package org.example.mapper;

import java.util.List;

import org.example.dto.request.CalendarRequest;
import org.example.dto.request.EventRequest;
import org.example.dto.request.InfoRequest;
import org.example.persistence.entity.CalendarEntity;
import org.example.persistence.entity.EventEntity;
import org.example.persistence.entity.InfoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between CalendarRequest DTOs and CalendarEntity.
 */
@Mapper(componentModel = "spring")
public interface CalendarRequestMapper {
    /**
     * Maps a CalendarEntity to a CalendarRequest DTO.
     *
     * @param entity the entity to map
     * @return the mapped request DTO
     */
    @Mapping(target = "metadata", source = "metadata")
    @Mapping(target = "events", source = "events")
    CalendarRequest toRequest(CalendarEntity entity);

    /**
     * Maps a CalendarRequest DTO to a CalendarEntity.
     *
     * @param request the request DTO to map
     * @return the mapped entity
     */
    @Mapping(target = "metadata", source = "metadata")
    @Mapping(target = "events", source = "events")
    CalendarEntity toEntity(CalendarRequest request);

    /**
     * Maps an InfoEntity to an InfoRequest.
     *
     * @param infoEntity the info entity to map
     * @return the mapped info request
     */
    InfoRequest toInfoRequest(InfoEntity infoEntity);

    /**
     * Maps an InfoRequest to an InfoEntity.
     *
     * @param infoRequest the info request to map
     * @return the mapped info entity
     */
    InfoEntity toInfoEntity(InfoRequest infoRequest);

    /**
     * Maps an EventRequest to an EventEntity.
     *
     * @param eventRequest the event request to map
     * @return the mapped event entity
     */
    EventEntity toEventEntity(EventRequest eventRequest);

    /**
     * Maps a list of EventRequest to a list of EventEntity.
     *
     * @param eventRequests the list of event requests to map
     * @return the list of mapped event entities
     */
    List<EventEntity> toEventEntityList(List<EventRequest> eventRequests);
}

