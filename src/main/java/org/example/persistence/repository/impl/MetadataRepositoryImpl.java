package org.example.persistence.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.example.persistence.entity.MetadataEntity;
import org.example.persistence.repository.MetadataRepository;
import org.springframework.stereotype.Component;

/**
 * In-memory implementation of MetadataRepository.
 */
@Component
public class MetadataRepositoryImpl implements MetadataRepository {
    private List<MetadataEntity> metadataList = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public MetadataEntity save(MetadataEntity entity) {
        metadataList.add(entity);
        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(String id) {
        metadataList.removeIf(m -> m.getId().equals(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<MetadataEntity> findById(String id) {
        return metadataList.stream().filter(m -> m.getId().equals(id)).findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MetadataEntity> findAll() {
        return metadataList;
    }
}
