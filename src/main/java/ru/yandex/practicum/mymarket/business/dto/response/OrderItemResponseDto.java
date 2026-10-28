package ru.yandex.practicum.mymarket.business.dto.response;

public record OrderItemResponseDto(
        long id,
        String title,
        long price,
        int count
) {
}
