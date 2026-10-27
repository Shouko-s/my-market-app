package ru.yandex.practicum.mymarket.business.dto.response;

import java.util.List;

public record CartResponseDto(
        List<ItemResponseDto> items,
        long total
) {
}
