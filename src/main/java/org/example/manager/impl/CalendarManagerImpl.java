package org.example.manager.impl;

import java.util.List;
import java.util.Optional;

import org.example.manager.CalendarManager;
import org.example.persistence.entity.Calendar;
import org.example.persistence.repository.CalendarRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarManagerImpl implements CalendarManager {
    private final CalendarRepository calendarRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Calendar save(Calendar entity) {
        return calendarRepository.save(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(String id) {
        calendarRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Calendar> findById(String id) {
        return calendarRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Calendar> findAll() {
        return calendarRepository.findAll();
    }
}
