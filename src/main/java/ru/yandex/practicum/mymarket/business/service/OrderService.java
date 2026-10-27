package ru.yandex.practicum.mymarket.business.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.business.exception.EmptyCartException;
import ru.yandex.practicum.mymarket.business.exception.OrderNotFoundException;
import ru.yandex.practicum.mymarket.business.mapper.OrderMapper;
import ru.yandex.practicum.mymarket.dal.entities.CartItemEntity;
import ru.yandex.practicum.mymarket.dal.entities.OrderEntity;
import ru.yandex.practicum.mymarket.dal.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;

    @Transactional
    public OrderEntity create(List<CartItemEntity> cartItems) {
        if (cartItems.isEmpty()) {
            throw new EmptyCartException();
        }

        OrderEntity order = OrderEntity.builder()
                .createdAt(LocalDateTime.now())
                .build();
        cartItems.stream()
                .map(mapper::toOrderItem)
                .forEach(order::addItem);
        order.setTotalSum(order.getItems().stream()
                .mapToLong(item -> item.getPrice() * item.getCount())
                .sum());

        return repository.save(order);
    }

    public List<OrderEntity> findAll() {
        return repository.findAllByOrderByIdDesc();
    }

    public OrderEntity findById(long id) {
        return repository.findWithItemsById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
