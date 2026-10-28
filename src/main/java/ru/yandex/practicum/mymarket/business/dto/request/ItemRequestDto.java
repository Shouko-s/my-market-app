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

    @NotBlank(message = "Введите название товара")
    @Size(max = 255, message = "Название не должно быть длиннее 255 символов")
    private String title;

    private String description;

    @NotNull(message = "Введите цену товара")
    @PositiveOrZero(message = "Цена не может быть отрицательной")
    private Long price;

    private MultipartFile image;
}
