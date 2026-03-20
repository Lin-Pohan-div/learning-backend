package com.learning.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 這樣寫可以確保在任何作業系統路徑都絕對正確
        String location = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
