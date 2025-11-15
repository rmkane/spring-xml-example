package org.example.service.impl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.example.service.HealthCheckService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for checking application health status.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCheckServiceImpl implements HealthCheckService {
    private static final int HEALTH_CHECK_RETRIES = 5;
    private static final long HEALTH_CHECK_DELAY_MS = 1000;

    private final HealthEndpoint healthEndpoint;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "health-check-scheduler");
        t.setDaemon(true);
        return t;
    });

    @Override
    public CompletableFuture<HealthComponent> waitForStableHealthAsync() {
        CompletableFuture<HealthComponent> future = new CompletableFuture<>();
        checkHealthWithRetry(future, 0);
        return future;
    }

    @Override
    public boolean isHealthAcceptable(HealthComponent health) {
        Status overallStatus = health.getStatus();
        
        if (overallStatus == Status.UP) {
            return true;
        }
        
        if (overallStatus == Status.OUT_OF_SERVICE) {
            Map<String, Object> details = extractHealthDetails(health);
            if (details != null && !details.isEmpty()) {
                boolean allCriticalUp = details.entrySet().stream()
                    .filter(entry -> !"readinessState".equals(entry.getKey()))
                    .allMatch(entry -> {
                        Object value = entry.getValue();
                        if (value instanceof HealthComponent component) {
                            return component.getStatus() == Status.UP;
                        } else if (value instanceof Map<?, ?> componentMap) {
                            Object componentStatus = componentMap.get("status");
                            return Status.UP.getCode().equals(componentStatus != null ? componentStatus.toString() : null);
                        }
                        return false;
                    });
                
                return allCriticalUp;
            }
        }
        
        return false;
    }

    @Override
    public Map<String, Object> extractHealthDetails(HealthComponent health) {
        if (health instanceof Health healthDetails) {
            return healthDetails.getDetails();
        }
        
        try {
            var method = health.getClass().getMethod("getComponents");
            @SuppressWarnings("unchecked")
            Map<String, HealthComponent> components = (Map<String, HealthComponent>) method.invoke(health);
            if (components != null && !components.isEmpty()) {
                return components.entrySet().stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (Object) entry.getValue()
                    ));
            }
        } catch (Exception e) {
            log.trace("Could not extract components from health: {}", e.getMessage());
        }
        
        return null;
    }

    @Override
    public String getFormattedHealthStatus(HealthComponent healthComponent) {
        Status healthStatus = healthComponent.getStatus();
        return formatHealthStatus(healthStatus);
    }

    @Override
    public String extractComponentStatus(Object value) {
        if (value instanceof HealthComponent component) {
            return formatHealthStatus(component.getStatus());
        }
        
        if (!(value instanceof Map<?, ?> componentDetails)) {
            return null;
        }
        
        Object componentStatus = componentDetails.get("status");
        if (componentStatus instanceof Status status) {
            return formatHealthStatus(status);
        }
        
        if (componentStatus != null) {
            return componentStatus.toString();
        }
        
        return null;
    }

    /**
     * Recursively checks health with retries using async scheduling.
     * Waits for readinessState to transition to UP if probes are enabled.
     *
     * @param future the future to complete when health is stable
     * @param attempt the current attempt number (0-indexed)
     */
    private void checkHealthWithRetry(CompletableFuture<HealthComponent> future, int attempt) {
        if (future.isDone()) {
            return;
        }
        
        CompletableFuture.supplyAsync(() -> healthEndpoint.health(), scheduler)
            .thenAccept(health -> {
                Status status = health.getStatus();
                
                logHealthDetails(health, attempt + 1, status);
                
                // Check if critical components are UP (excluding readinessState which may not transition
                // properly when probes are enabled - the readiness endpoint works, but the state doesn't update)
                boolean criticalComponentsUp = isHealthAcceptable(health);
                
                // If critical components are UP, we're good - don't wait for readinessState
                // When probes are enabled, readinessState in aggregated health may never transition,
                // but the app is ready (readiness endpoint returns UP immediately)
                if (criticalComponentsUp) {
                    if (attempt > 0) {
                        log.info("Health stabilized after {} attempt(s)", attempt + 1);
                    }
                    future.complete(health);
                    return;
                }
                
                // If we've exhausted retries, accept current state
                if (attempt >= HEALTH_CHECK_RETRIES - 1) {
                    logHealthWarning(health, status);
                    future.complete(health);
                    return;
                }
                
                // Wait and retry for critical components
                log.debug("Health not ready yet, retrying in {}ms (attempt {}/{})", 
                    HEALTH_CHECK_DELAY_MS, attempt + 1, HEALTH_CHECK_RETRIES);
                scheduler.schedule(
                    () -> checkHealthWithRetry(future, attempt + 1),
                    HEALTH_CHECK_DELAY_MS,
                    TimeUnit.MILLISECONDS
                );
            })
            .exceptionally(ex -> {
                log.warn("Health check failed: {}", ex.getMessage(), ex);
                future.completeExceptionally(ex);
                return null;
            });
    }


    /**
     * Logs detailed health information for debugging.
     *
     * @param health the health component
     * @param attempt the attempt number
     * @param status the health status
     */
    private void logHealthDetails(HealthComponent health, int attempt, Status status) {
        Map<String, Object> details = extractHealthDetails(health);
        
        if (details != null && !details.isEmpty()) {
            log.debug("Health check attempt {}: status={}", attempt, status);
            details.forEach((key, value) -> {
                if (value instanceof HealthComponent component) {
                    Status componentStatus = component.getStatus();
                    log.debug("  Component '{}': status={}", key, componentStatus);
                } else if (value instanceof Map<?, ?> componentMap) {
                    Object componentStatus = componentMap.get("status");
                    log.debug("  Component '{}': status={}", key, componentStatus);
                } else {
                    log.debug("  Component '{}': {}", key, value);
                }
            });
        } else {
            log.debug("Health check attempt {}: status={} (health component type: {}, no details available)", 
                attempt, status, health.getClass().getSimpleName());
        }
    }

    /**
     * Logs a warning with detailed health information when health is not UP after all retries.
     *
     * @param health the health component
     * @param status the final health status
     */
    private void logHealthWarning(HealthComponent health, Status status) {
        log.warn("Health check completed after {} attempts with status: {}", 
            HEALTH_CHECK_RETRIES, status);
        
        Map<String, Object> details = extractHealthDetails(health);
        if (details != null && !details.isEmpty()) {
            log.warn("Health component details:");
            details.forEach((key, value) -> {
                if (value instanceof HealthComponent component) {
                    Status componentStatus = component.getStatus();
                    if (componentStatus != Status.UP) {
                        log.warn("  Component '{}': status={} (not UP)", key, componentStatus);
                    } else {
                        log.debug("  Component '{}': status={} (UP)", key, componentStatus);
                    }
                } else if (value instanceof Map<?, ?> componentMap) {
                    Object componentStatus = componentMap.get("status");
                    if (componentStatus != null && !Status.UP.getCode().equals(componentStatus.toString())) {
                        log.warn("  Component '{}': status={} (not UP)", key, componentStatus);
                    }
                }
            });
        }
        
        log.warn("This may indicate that health indicators are still initializing. " +
            "Check /actuator/health endpoint for details.");
    }

    @Override
    public boolean hasCriticalHealthIssues(HealthComponent healthComponent) {
        if (isHealthAcceptable(healthComponent)) {
            return false;
        }
        
        Map<String, Object> details = extractHealthDetails(healthComponent);
        if (details == null || details.isEmpty()) {
            return false;
        }
        
        // Check if there are any critical components (excluding readinessState) that are not UP
        return details.entrySet().stream()
            .filter(entry -> !"readinessState".equals(entry.getKey()))
            .anyMatch(entry -> {
                Object value = entry.getValue();
                if (value instanceof HealthComponent component) {
                    return component.getStatus() != Status.UP;
                } else if (value instanceof Map<?, ?> componentMap) {
                    Object componentStatus = componentMap.get("status");
                    return componentStatus != null && !Status.UP.getCode().equals(componentStatus.toString());
                }
                return false;
            });
    }

    /**
     * Formats the health status for display.
     *
     * @param status the health status
     * @return formatted status string with visual indicator
     */
    private String formatHealthStatus(Status status) {
        if (status == Status.UP) {
            return "✓ UP";
        } else if (status == Status.DOWN) {
            return "✗ DOWN";
        } else if (status == Status.OUT_OF_SERVICE) {
            return "⚠ OUT_OF_SERVICE";
        } else if (status == Status.UNKNOWN) {
            return "? UNKNOWN";
        } else {
            return String.format("⚠ %s", status.getCode());
        }
    }

    /**
     * Shuts down the scheduler when the bean is destroyed.
     */
    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

