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
    public Calendar saveCalendar(Calendar calendar) {
        return calendarRepository.save(calendar);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteCalendarById(String id) {
        calendarRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Calendar> findCalendarById(String id) {
        return calendarRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Calendar> findAllCalendars() {
        return calendarRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean calendarExists(String id) {
        return calendarRepository.existsById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCalendarCount() {
        return calendarRepository.count();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAllCalendars() {
        calendarRepository.deleteAll();
    }
}
