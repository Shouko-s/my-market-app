package ru.yandex.practicum.mymarket.business.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.business.dto.response.OrderResponseDto;
import ru.yandex.practicum.mymarket.business.mapper.OrderMapper;
import ru.yandex.practicum.mymarket.business.service.CartService;
import ru.yandex.practicum.mymarket.business.service.OrderService;
import ru.yandex.practicum.mymarket.dal.entities.OrderEntity;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderFacade {

    private final OrderService orderService;
    private final CartService cartService;
    private final OrderMapper mapper;

    @Transactional
    public long buy() {
        OrderEntity order = orderService.create(cartService.getCartItems());
        cartService.clear();
        return order.getId();
    }

    public List<OrderResponseDto> findAll() {
        return orderService.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    public OrderResponseDto findById(long id) {
        return mapper.toResponse(orderService.findById(id));
    }
}
