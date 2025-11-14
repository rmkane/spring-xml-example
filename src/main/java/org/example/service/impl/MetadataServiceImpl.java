package org.example.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.example.dto.request.InfoRequest;
import org.example.dto.request.MetadataRequest;
import org.example.dto.response.MetadataResponse;
import org.example.exception.MetadataAlreadyExistsException;
import org.example.mapper.MetadataRequestMapper;
import org.example.mapper.MetadataResponseMapper;
import org.example.model.MetadataState;
import org.example.persistence.entity.MetadataEntity;
import org.example.persistence.repository.MetadataRepository;
import org.example.service.MetadataService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetadataServiceImpl implements MetadataService {
    private final MetadataRepository metadataRepository;
    private final MetadataRequestMapper metadataRequestMapper;
    private final MetadataResponseMapper metadataResponseMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<MetadataResponse> findById(String id) {
        return metadataRepository.findById(id)
            .map(metadataResponseMapper::toResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(String id) {
        metadataRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MetadataResponse> findAll() {
        return metadataRepository.findAll()
            .stream()
            .map(metadataResponseMapper::toResponse)
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetadataResponse create(MetadataRequest metadata) {
        if (metadataRepository.findById(metadata.getId()).isPresent()) {
            throw new MetadataAlreadyExistsException(metadata.getId());
        }
        if (metadata.getId() == null || metadata.getId().isEmpty()) {
            metadata.setId(generateId());
        }
        // Set default state from info if not provided
        if (metadata.getInfo() != null && metadata.getInfo().getState() == null) {
            metadata.getInfo().setState(MetadataState.UNKNOWN);
        } else if (metadata.getInfo() == null) {
            InfoRequest info = InfoRequest.builder()
                .state(MetadataState.UNKNOWN)
                .build();
            metadata.setInfo(info);
        }
        MetadataEntity entity = metadataRequestMapper.toEntity(metadata);
        return metadataResponseMapper.toResponse(metadataRepository.save(entity));
    }

    /**
     * Generates a unique UUID for metadata entries.
     *
     * @return a UUID string
     */
    private String generateId() {
        return UUID.randomUUID().toString();
    }
}

