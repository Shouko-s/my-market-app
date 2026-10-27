package ru.yandex.practicum.mymarket.business.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class ItemRequestDto {

    @NotBlank
    @Size(max = 255)
    private String title;

    private String description;

    @NotNull
    @PositiveOrZero
    private Long price;

    private MultipartFile image;
}
