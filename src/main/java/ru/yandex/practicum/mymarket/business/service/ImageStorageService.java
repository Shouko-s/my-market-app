package ru.yandex.practicum.mymarket.business.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class ImageStorageService {

    private static final String URL_PREFIX = "images/";

    private final Path imagesDir;

    public ImageStorageService(@Value("${app.images-dir}") Path imagesDir) {
        this.imagesDir = imagesDir;
    }

    public String save(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null;
        }
        String extension = StringUtils.getFilenameExtension(image.getOriginalFilename());
        String fileName = UUID.randomUUID() + (extension != null ? "." + extension : "");
        try {
            Files.createDirectories(imagesDir);
            image.transferTo(imagesDir.resolve(fileName));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save image " + fileName, e);
        }
        return URL_PREFIX + fileName;
    }
}
