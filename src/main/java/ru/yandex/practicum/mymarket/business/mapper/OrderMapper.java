package ru.yandex.practicum.mymarket.business.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.mymarket.business.dto.response.OrderItemResponseDto;
import ru.yandex.practicum.mymarket.business.dto.response.OrderResponseDto;
import ru.yandex.practicum.mymarket.dal.entities.CartItemEntity;
import ru.yandex.practicum.mymarket.dal.entities.OrderEntity;
import ru.yandex.practicum.mymarket.dal.entities.OrderItemEntity;

@Component
public class OrderMapper {

    public OrderItemEntity toOrderItem(CartItemEntity cartItem) {
        return OrderItemEntity.builder()
                .item(cartItem.getItem())
                .title(cartItem.getItem().getTitle())
                .price(cartItem.getItem().getPrice())
                .count(cartItem.getCount())
                .build();
    }

    public OrderResponseDto toResponse(OrderEntity entity) {
        return new OrderResponseDto(
                entity.getId(),
                entity.getItems().stream()
                        .map(this::toResponse)
                        .toList(),
                entity.getTotalSum()
        );
    }

    private OrderItemResponseDto toResponse(OrderItemEntity entity) {
        return new OrderItemResponseDto(
                entity.getItem().getId(),
                entity.getTitle(),
                entity.getPrice(),
                entity.getCount()
        );
    }
}
