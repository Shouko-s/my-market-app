package ru.yandex.practicum.mymarket.business.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ImageStorageServiceTest {

    @TempDir
    private Path imagesDir;

    @Test
    void save_shouldStoreFileUnderUniqueNameAndReturnUrlPath() throws IOException {
        ImageStorageService storage = new ImageStorageService(imagesDir);
        byte[] content = {1, 2, 3};

        String path = storage.save(new MockMultipartFile("image", "ball.png", "image/png", content));

        assertThat(path).startsWith("images/").endsWith(".png");
        Path stored = imagesDir.resolve(path.substring("images/".length()));
        assertThat(Files.readAllBytes(stored)).isEqualTo(content);
    }

    @Test
    void save_shouldGiveDifferentNamesToFilesWithSameName() {
        ImageStorageService storage = new ImageStorageService(imagesDir);

        String first = storage.save(new MockMultipartFile("image", "ball.png", "image/png", new byte[]{1}));
        String second = storage.save(new MockMultipartFile("image", "ball.png", "image/png", new byte[]{2}));

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void save_shouldReturnNullWhenImageIsMissing() {
        ImageStorageService storage = new ImageStorageService(imagesDir);

        assertThat(storage.save(null)).isNull();
        assertThat(storage.save(new MockMultipartFile("image", new byte[0]))).isNull();
    }
}
