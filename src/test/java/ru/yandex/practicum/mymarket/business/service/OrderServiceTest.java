package ru.yandex.practicum.mymarket.business.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.mymarket.business.exception.EmptyCartException;
import ru.yandex.practicum.mymarket.business.exception.OrderNotFoundException;
import ru.yandex.practicum.mymarket.business.mapper.OrderMapper;
import ru.yandex.practicum.mymarket.dal.entities.CartItemEntity;
import ru.yandex.practicum.mymarket.dal.entities.ItemEntity;
import ru.yandex.practicum.mymarket.dal.entities.OrderEntity;
import ru.yandex.practicum.mymarket.dal.entities.OrderItemEntity;
import ru.yandex.practicum.mymarket.dal.repository.OrderRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @Spy
    private OrderMapper mapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void create_shouldCopyCartItemsAndCalculateTotal() {
        ItemEntity ball = ItemEntity.builder().id(1L).title("Мяч").price(2500).build();
        ItemEntity rope = ItemEntity.builder().id(2L).title("Скакалка").price(800).build();
        when(repository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderEntity order = orderService.create(List.of(cartItem(ball, 2), cartItem(rope, 3)));

        assertThat(order.getTotalSum()).isEqualTo(2 * 2500 + 3 * 800);
        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(order.getItems())
                .extracting(item -> item.getItem().getId(), OrderItemEntity::getTitle,
                        OrderItemEntity::getPrice, OrderItemEntity::getCount)
                .containsExactly(tuple(1L, "Мяч", 2500L, 2), tuple(2L, "Скакалка", 800L, 3));
        assertThat(order.getItems()).allMatch(item -> item.getOrder() == order);
    }

    @Test
    void create_shouldFailForEmptyCart() {
        assertThatThrownBy(() -> orderService.create(List.of()))
                .isInstanceOf(EmptyCartException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void findById_shouldReturnOrder() {
        OrderEntity order = OrderEntity.builder().id(1L).build();
        when(repository.findWithItemsById(1L)).thenReturn(Optional.of(order));

        assertThat(orderService.findById(1)).isSameAs(order);
    }

    @Test
    void findById_shouldThrowWhenOrderNotFound() {
        when(repository.findWithItemsById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(1))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void findAll_shouldReturnOrdersFromRepository() {
        List<OrderEntity> orders = List.of(OrderEntity.builder().id(2L).build(), OrderEntity.builder().id(1L).build());
        when(repository.findAllByOrderByIdDesc()).thenReturn(orders);

        assertThat(orderService.findAll()).isEqualTo(orders);
    }

    private CartItemEntity cartItem(ItemEntity item, int count) {
        return CartItemEntity.builder()
                .item(item)
                .count(count)
                .build();
    }
}
