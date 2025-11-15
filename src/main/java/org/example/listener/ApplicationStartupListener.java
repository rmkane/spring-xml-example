package org.example.listener;

import java.util.List;

import org.example.service.EndpointInfoService;
import org.example.service.HealthCheckService;
import org.example.service.StartupInfoService;
import org.example.util.BoxFormatter;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Listener that prints application startup information to STDOUT.
 */
@Component
@RequiredArgsConstructor
public class ApplicationStartupListener {
    private final EndpointInfoService endpointInfoService;
    private final HealthCheckService healthCheckService;
    private final StartupInfoService startupInfoService;

    /**
     * Handles the application ready event and prints startup information to STDOUT.
     *
     * @param event the application ready event
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        String baseUrl = endpointInfoService.getBaseUrl(event);
        
        healthCheckService.waitForStableHealthAsync()
            .thenAccept(healthComponent -> {
                List<String> startupInfo = startupInfoService.getStartupInfo(baseUrl, healthComponent);
                List<String> endpointInfo = endpointInfoService.getEndpointInfo(baseUrl);
                
                BoxFormatter.printBoxed("Application Started", startupInfo, endpointInfo);
                logHealthWarningIfNeeded(healthComponent);
            })
            .join(); // Wait for async health check to complete before continuing
    }

    /**
     * Logs a warning if there are critical health issues.
     *
     * @param healthComponent the health component
     */
    private void logHealthWarningIfNeeded(HealthComponent healthComponent) {
        if (healthCheckService.hasCriticalHealthIssues(healthComponent)) {
            System.err.println("WARNING: Application health check indicates critical issues. Please review the health endpoint for details.");
        }
    }
}
