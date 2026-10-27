package ru.yandex.practicum.mymarket.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final Path imagesDir;

    public WebConfig(@Value("${app.images-dir}") Path imagesDir) {
        this.imagesDir = imagesDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations(
                        "file:" + imagesDir.toAbsolutePath() + "/",
                        "classpath:/static/images/"
                );
    }
}
