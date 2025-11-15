package org.example.persistence.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.example.persistence.entity.CalendarEntity;
import org.example.persistence.repository.CalendarRepository;
import org.springframework.stereotype.Component;

/**
 * In-memory implementation of CalendarRepository.
 */
@Component
public class CalendarRepositoryImpl implements CalendarRepository {
    private List<CalendarEntity> calendarList = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public CalendarEntity save(CalendarEntity entity) {
        calendarList.add(entity);
        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(String id) {
        calendarList.removeIf(c -> c.getId().equals(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CalendarEntity> findById(String id) {
        return calendarList.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CalendarEntity> findAll() {
        return calendarList;
    }
}

