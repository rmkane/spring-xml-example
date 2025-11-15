package org.example.service;

import java.util.Map;

import org.springframework.boot.context.event.ApplicationReadyEvent;

/**
 * Service for providing application endpoint information.
 */
public interface EndpointInfoService {
    /**
     * Gets the base URL of the running application.
     * Handles random port assignment (server.port=0) and gets actual running values.
     *
     * @param event the application ready event
     * @return the base URL (e.g., "http://localhost:8080")
     */
    String getBaseUrl(ApplicationReadyEvent event);

    /**
     * Gets endpoint information (connection/endpoint URLs).
     *
     * @param baseUrl the base URL of the application
     * @return map of endpoint labels to their URLs
     */
    Map<String, String> getEndpointInfo(String baseUrl);
}

