package org.example.service;

import java.util.List;

import org.springframework.boot.actuate.health.HealthComponent;

/**
 * Service for providing application startup information.
 */
public interface StartupInfoService {
    /**
     * Gets startup information including health status.
     *
     * @param baseUrl the base URL of the application
     * @param healthComponent the health component
     * @return list of startup information lines (body section)
     */
    List<String> getStartupInfo(String baseUrl, HealthComponent healthComponent);
}

