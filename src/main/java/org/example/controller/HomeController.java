package org.example.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.example.service.EndpointInfoService;
import org.example.service.HealthCheckService;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;

/**
 * Controller for the home page displaying application information.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {
    private final EndpointInfoService endpointInfoService;
    private final HealthCheckService healthCheckService;
    private final HealthEndpoint healthEndpoint;
    private final ServerProperties serverProperties;

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home(Model model) {
        // Get base URL
        String baseUrl = getBaseUrl();
        
        // Get endpoint information
        Map<String, String> endpoints = endpointInfoService.getEndpointInfo(baseUrl);
        model.addAttribute("endpoints", endpoints);
        
        // Get health status
        HealthComponent health = healthEndpoint.health();
        model.addAttribute("healthStatus", healthCheckService.getFormattedHealthStatus(health));
        model.addAttribute("isHealthy", healthCheckService.isHealthAcceptable(health));
        
        // Get health component details with formatted status
        Map<String, Object> healthDetails = healthCheckService.extractHealthDetails(health);
        Map<String, String> healthComponents = new LinkedHashMap<>();
        if (healthDetails != null) {
            healthDetails.forEach((key, value) -> {
                String status = healthCheckService.extractComponentStatus(value);
                if (status != null) {
                    healthComponents.put(key, status);
                }
            });
        }
        model.addAttribute("healthComponents", healthComponents);
        
        return "index";
    }

    private String getBaseUrl() {
        String address = serverProperties.getAddress() != null 
            ? serverProperties.getAddress().getHostAddress() 
            : "localhost";
        int port = serverProperties.getPort() != null 
            ? serverProperties.getPort() 
            : 8080;
        String contextPath = serverProperties.getServlet().getContextPath();
        if (contextPath == null || contextPath.isEmpty() || "/".equals(contextPath)) {
            contextPath = "";
        }
        return String.format("http://%s:%d%s", address, port, contextPath);
    }
}

