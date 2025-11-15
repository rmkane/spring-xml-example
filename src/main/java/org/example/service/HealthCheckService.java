package org.example.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.boot.actuate.health.HealthComponent;

/**
 * Service for checking application health status.
 */
public interface HealthCheckService {
    /**
     * Waits for health to stabilize by retrying health checks asynchronously.
     * 
     * @return CompletableFuture that completes with the health component after stabilization attempts
     */
    CompletableFuture<HealthComponent> waitForStableHealthAsync();

    /**
     * Checks if health is acceptable for startup purposes.
     * Considers health acceptable if all critical components (excluding readinessState) are UP.
     * 
     * @param health the health component
     * @return true if health is acceptable (all critical components UP)
     */
    boolean isHealthAcceptable(HealthComponent health);

    /**
     * Extracts health details from various health component types.
     * 
     * @param health the health component
     * @return map of health details, or null if not available
     */
    Map<String, Object> extractHealthDetails(HealthComponent health);

    /**
     * Gets the formatted health status string for display.
     * 
     * @param healthComponent the health component
     * @return formatted health status
     */
    String getFormattedHealthStatus(HealthComponent healthComponent);

    /**
     * Extracts the status string from a health component value.
     * 
     * @param value the health component value (can be HealthComponent or Map)
     * @return formatted status string, or null if status cannot be extracted
     */
    String extractComponentStatus(Object value);

    /**
     * Checks if there are critical health issues that should be logged as warnings.
     * Does not consider readinessState as a critical issue (expected behavior when probes are enabled).
     * 
     * @param healthComponent the health component
     * @return true if there are critical issues that should be warned about
     */
    boolean hasCriticalHealthIssues(HealthComponent healthComponent);
}

