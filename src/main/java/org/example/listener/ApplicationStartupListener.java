package org.example.listener;

import java.util.List;

import org.example.util.BoxFormatter;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Listener that prints application startup information including host and port to STDOUT.
 */
@Component
@RequiredArgsConstructor
public class ApplicationStartupListener {
    private static final String SWAGGER_UI_PATH = "/swagger-ui.html";
    private static final String API_DOCS_PATH = "/v3/api-docs";

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;

    private final ServerProperties serverProperties;

    /**
     * Handles the application ready event and prints startup information to STDOUT.
     *
     * @param event the application ready event
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        // Get actual running port (handles random port assignment when server.port=0)
        String baseUrl = getBaseUrl(event);
                
        BoxFormatter.printBoxed("Application Started", List.of(
            String.format("Server running on: %s", baseUrl),
            String.format("Swagger UI available at: %s%s", baseUrl, SWAGGER_UI_PATH),
            String.format("API Docs available at: %s%s", baseUrl, API_DOCS_PATH)
        ));
    }

    /**
     * Gets the base URL of the running application.
     * Handles random port assignment (server.port=0) and gets actual running values.
     *
     * @param event the application ready event
     * @return the base URL (e.g., "http://localhost:8080")
     */
    private String getBaseUrl(ApplicationReadyEvent event) {
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
        if (serverProperties.getAddress() != null) {
            return serverProperties.getAddress().getHostAddress();
        }
        return DEFAULT_HOST;
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
        return serverProperties.getPort() != null ? serverProperties.getPort() : DEFAULT_PORT;
    }
}

