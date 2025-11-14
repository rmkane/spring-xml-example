package org.example.mapper;

import java.util.List;

import org.example.dto.request.EntryRequest;
import org.example.dto.request.InfoRequest;
import org.example.dto.request.MetadataRequest;
import org.example.persistence.entity.EntryEntity;
import org.example.persistence.entity.InfoEntity;
import org.example.persistence.entity.MetadataEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between MetadataRequest DTOs and MetadataEntity.
 */
@Mapper(componentModel = "spring")
public interface MetadataRequestMapper {
    /**
     * Maps a MetadataEntity to a MetadataRequest DTO.
     *
     * @param entity the entity to map
     * @return the mapped request DTO
     */
    @Mapping(target = "info", source = "info")
    @Mapping(target = "entries", source = "entries")
    MetadataRequest toRequest(MetadataEntity entity);

    /**
     * Maps a MetadataRequest DTO to a MetadataEntity.
     *
     * @param request the request DTO to map
     * @return the mapped entity
     */
    @Mapping(target = "info", source = "info")
    @Mapping(target = "entries", source = "entries")
    MetadataEntity toEntity(MetadataRequest request);

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
     * Maps an EntryRequest to an EntryEntity.
     *
     * @param entryRequest the entry request to map
     * @return the mapped entry entity
     */
    EntryEntity toEntryEntity(EntryRequest entryRequest);

    /**
     * Maps a list of EntryRequest to a list of EntryEntity.
     *
     * @param entryRequests the list of entry requests to map
     * @return the list of mapped entry entities
     */
    List<EntryEntity> toEntryEntityList(List<EntryRequest> entryRequests);
}

