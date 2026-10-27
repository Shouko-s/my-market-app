package ru.yandex.practicum.mymarket.business.dto.response;

import java.util.List;

public record OrderResponseDto(
        long id,
        List<OrderItemResponseDto> items,
        long totalSum
) {
}
