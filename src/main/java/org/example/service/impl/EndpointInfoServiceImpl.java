package org.example.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.example.service.EndpointInfoService;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Service implementation for providing application endpoint information.
 */
@Service
@RequiredArgsConstructor
public class EndpointInfoServiceImpl implements EndpointInfoService {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;
    private static final String SWAGGER_UI_PATH = "/swagger-ui.html";
    private static final String API_DOCS_PATH = "/v3/api-docs";
    private static final String HEALTH_ENDPOINT_PATH = "/actuator/health";

    private final ServerProperties serverProperties;

    @Override
    public String getBaseUrl(ApplicationReadyEvent event) {
        String address = getAddress();
        String contextPath = getContextPath(event);
        int port = getPort(event);
        
        return String.format("http://%s:%d%s", address, port, contextPath);
    }

    /**
     * Gets the server address from configuration.
     * Address comes from ServerProperties configuration, not from WebServer.
     *
     * @return the server address, or "localhost" if not configured
     */
    private String getAddress() {
        if (serverProperties.getAddress() == null) {
            return DEFAULT_HOST;
        }
        
        return serverProperties.getAddress().getHostAddress();
    }

    /**
     * Gets the servlet context path.
     * Attempts to get from the actual servlet context, falls back to ServerProperties.
     *
     * @param event the application ready event
     * @return the context path, or empty string if root context
     */
    private String getContextPath(ApplicationReadyEvent event) {
        String contextPath = null;
        
        if (event.getApplicationContext() instanceof ServletWebServerApplicationContext context) {
            var servletContext = context.getServletContext();
            if (servletContext != null) {
                contextPath = servletContext.getContextPath();
            }
        }
        
        // Fallback to configured context path if not available from servlet context
        if (contextPath == null) {
            contextPath = serverProperties.getServlet().getContextPath();
        }
        
        // Normalize: treat null, empty, or "/" as root context (empty string)
        if (contextPath == null || contextPath.isEmpty() || "/".equals(contextPath)) {
            return "";
        }
        
        return contextPath;
    }

    /**
     * Gets the actual port the server is running on.
     * Handles random port assignment (server.port=0) by getting the actual assigned port.
     *
     * @param event the application ready event
     * @return the port number
     */
    private int getPort(ApplicationReadyEvent event) {
        if (event.getApplicationContext() instanceof ServletWebServerApplicationContext context) {
            return context.getWebServer().getPort();
        }
        
        // Fallback to configured port if not a servlet context
        if (serverProperties.getPort() != null) {
            return serverProperties.getPort();
        }
        
        return DEFAULT_PORT;
    }

    @Override
    public List<String> getEndpointInfo(String baseUrl) {
        List<String> endpointInfo = new ArrayList<>();
        endpointInfo.add(String.format("Server running on: %s", baseUrl));
        endpointInfo.add(String.format("Swagger UI available at: %s%s", baseUrl, SWAGGER_UI_PATH));
        endpointInfo.add(String.format("API Docs available at: %s%s", baseUrl, API_DOCS_PATH));
        endpointInfo.add(String.format("Health check available at: %s%s", baseUrl, HEALTH_ENDPOINT_PATH));
        return endpointInfo;
    }
}

