package ru.yandex.practicum.mymarket.business.dto.response;

public record PagingResponseDto(
        int pageSize,
        int pageNumber,
        boolean hasPrevious,
        boolean hasNext
) {
}
