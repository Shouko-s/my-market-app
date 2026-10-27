package ru.yandex.practicum.mymarket.business.dto.response;

public record ItemResponseDto(
        long id,
        String title,
        String description,
        String imgPath,
        long price,
        int count
) {
}
