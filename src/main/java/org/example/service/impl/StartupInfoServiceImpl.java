package org.example.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.example.service.HealthCheckService;
import org.example.service.StartupInfoService;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Service implementation for providing application startup information.
 */
@Service
@RequiredArgsConstructor
public class StartupInfoServiceImpl implements StartupInfoService {
    private final HealthCheckService healthCheckService;

    @Override
    public List<String> getStartupInfo(String baseUrl, HealthComponent healthComponent) {
        List<String> startupInfo = new ArrayList<>();
        
        // Health status
        addHealthStatus(startupInfo, healthComponent);
        addHealthComponentDetails(startupInfo, healthComponent);
        
        return startupInfo;
    }

    /**
     * Adds the overall health status to the startup info.
     * Shows "READY" if critical components are UP, even if overall status is OUT_OF_SERVICE due to readinessState.
     *
     * @param startupInfo the list to add to
     * @param healthComponent the health component
     */
    private void addHealthStatus(List<String> startupInfo, HealthComponent healthComponent) {
        String healthStatus;
        if (healthCheckService.isHealthAcceptable(healthComponent)) {
            healthStatus = "✓ READY";
        } else {
            healthStatus = healthCheckService.getFormattedHealthStatus(healthComponent);
        }
        startupInfo.add(String.format("Health Status: %s", healthStatus));
    }

    /**
     * Adds detailed health component information to the startup info.
     *
     * @param startupInfo the list to add to
     * @param healthComponent the health component
     */
    private void addHealthComponentDetails(List<String> startupInfo, HealthComponent healthComponent) {
        Map<String, Object> healthDetails = healthCheckService.extractHealthDetails(healthComponent);
        if (healthDetails == null || healthDetails.isEmpty()) {
            return;
        }
        
        healthDetails.forEach((key, value) -> {
            String componentStatus = healthCheckService.extractComponentStatus(value);
            if (componentStatus == null) {
                return;
            }
            startupInfo.add(String.format("  %s: %s", capitalize(key), componentStatus));
        });
    }

    /**
     * Capitalizes the first letter of a string.
     *
     * @param str the string to capitalize
     * @return capitalized string
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}

