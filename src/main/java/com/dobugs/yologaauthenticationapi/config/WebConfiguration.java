package com.dobugs.yologaauthenticationapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Value("${cors.auth.path}")
    private String path;

    @Value("${cors.auth.yologa.origin}")
    private String yologa;

    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        allowLocalhostForTest(registry);
        allowYologaForAuth(registry);
        allowGlobal(registry);
    }

    private void allowLocalhostForTest(final CorsRegistry registry) {
        registry.addMapping("/api/vi/test/**")
            .allowedOrigins("http://localhost");
    }

    private void allowYologaForAuth(final CorsRegistry registry) {
        registry.addMapping(path)
            .allowedOrigins(yologa)
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .maxAge(3000);
    }

    private void allowGlobal(final CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .maxAge(3000);
    }
}
