package com.example.sampleproject.webconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/student/**")  // Apply to specific endpoints
                .allowedOrigins("http://localhost:8081")  // Allow requests from React frontend on port 8081
                .allowedMethods("GET", "POST", "PUT", "DELETE")  // Allow these HTTP methods
                .allowedHeaders("*");  // Allow all headers
    }
}
