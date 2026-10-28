package ru.yandex.practicum.mymarket.business.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.mymarket.business.dto.request.ItemRequestDto;
import ru.yandex.practicum.mymarket.business.dto.response.ItemResponseDto;
import ru.yandex.practicum.mymarket.dal.entities.ItemEntity;

@Component
public class ItemMapper {

    public ItemEntity toEntity(ItemRequestDto dto, String imgPath) {
        return ItemEntity.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .imgPath(imgPath)
                .build();
    }

    public ItemResponseDto toResponse(ItemEntity entity, int count) {
        return new ItemResponseDto(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getImgPath(),
                entity.getPrice(),
                count
        );
    }
}
