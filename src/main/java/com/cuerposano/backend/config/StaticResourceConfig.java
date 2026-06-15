package com.cuerposano.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final Path fotosDirectory;

    public StaticResourceConfig(
            @Value("${app.fotos.dir:uploads/fotos}") String fotosDir
    ) {
        this.fotosDirectory = Paths.get(fotosDir)
                .toAbsolutePath()
                .normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String fotosLocation = fotosDirectory.toUri().toString();

        if (!fotosLocation.endsWith("/")) {
            fotosLocation += "/";
        }

        registry
                .addResourceHandler("/fotos/**")
                .addResourceLocations(fotosLocation);
    }
}
