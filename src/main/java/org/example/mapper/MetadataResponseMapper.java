package org.example.mapper;

import java.util.List;

import org.example.dto.response.EntryResponse;
import org.example.dto.response.InfoResponse;
import org.example.dto.response.MetadataResponse;
import org.example.persistence.entity.EntryEntity;
import org.example.persistence.entity.InfoEntity;
import org.example.persistence.entity.MetadataEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between MetadataEntity and MetadataResponse DTOs.
 */
@Mapper(componentModel = "spring")
public interface MetadataResponseMapper {
    /**
     * Maps a MetadataEntity to a MetadataResponse DTO.
     *
     * @param entity the entity to map
     * @return the mapped response DTO
     */
    @Mapping(target = "info", source = "info")
    @Mapping(target = "entries", source = "entries")
    MetadataResponse toResponse(MetadataEntity entity);

    /**
     * Maps a MetadataResponse DTO to a MetadataEntity.
     *
     * @param response the response DTO to map
     * @return the mapped entity
     */
    @Mapping(target = "info", source = "info")
    @Mapping(target = "entries", source = "entries")
    MetadataEntity toEntity(MetadataResponse response);

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
     * Maps an EntryEntity to an EntryResponse.
     *
     * @param entryEntity the entry entity to map
     * @return the mapped entry response
     */
    EntryResponse toEntryResponse(EntryEntity entryEntity);

    /**
     * Maps a list of EntryEntity to a list of EntryResponse.
     *
     * @param entryEntities the list of entry entities to map
     * @return the list of mapped entry responses
     */
    List<EntryResponse> toEntryResponseList(List<EntryEntity> entryEntities);
}

