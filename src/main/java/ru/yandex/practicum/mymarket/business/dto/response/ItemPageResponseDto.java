package ru.yandex.practicum.mymarket.business.dto.response;

import java.util.List;

public record ItemPageResponseDto(
        List<ItemResponseDto> items,
        PagingResponseDto paging
) {
}
