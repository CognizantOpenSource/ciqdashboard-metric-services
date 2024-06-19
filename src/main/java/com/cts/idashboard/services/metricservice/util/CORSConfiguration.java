package com.cts.idashboard.services.metricservice.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CORSConfiguration {


    protected static final String[] HTTP_METHODS = {"POST", "GET", "PUT", "DELETE"};
    protected static final String[] ALLOWED_ORIGIN = {"http://localhost:80", "http://localhost:8092", "http://localhost:8091", "http://localhost:8090", "http://localhost:8089",
            "https://izone.cts.com:8090", "http://10.142.204.23:8090", "http://10.142.204.82:8090"
    };

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods(HTTP_METHODS);
            }
        };
    }

}
