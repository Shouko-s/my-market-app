package ru.yandex.practicum.mymarket.business.facade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.mymarket.business.dto.response.OrderItemResponseDto;
import ru.yandex.practicum.mymarket.business.dto.response.OrderResponseDto;
import ru.yandex.practicum.mymarket.business.exception.EmptyCartException;
import ru.yandex.practicum.mymarket.business.mapper.OrderMapper;
import ru.yandex.practicum.mymarket.business.service.CartService;
import ru.yandex.practicum.mymarket.business.service.OrderService;
import ru.yandex.practicum.mymarket.dal.entities.CartItemEntity;
import ru.yandex.practicum.mymarket.dal.entities.ItemEntity;
import ru.yandex.practicum.mymarket.dal.entities.OrderEntity;
import ru.yandex.practicum.mymarket.dal.entities.OrderItemEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {

    @Mock
    private OrderService orderService;

    @Mock
    private CartService cartService;

    @Spy
    private OrderMapper mapper;

    @InjectMocks
    private OrderFacade orderFacade;

    @Test
    void buy_shouldCreateOrderFromCartAndThenClearCart() {
        List<CartItemEntity> cartItems = List.of(CartItemEntity.builder().count(1).build());
        when(cartService.getCartItems()).thenReturn(cartItems);
        when(orderService.create(cartItems)).thenReturn(OrderEntity.builder().id(7L).build());

        long orderId = orderFacade.buy();

        assertThat(orderId).isEqualTo(7);
        InOrder inOrder = inOrder(orderService, cartService);
        inOrder.verify(orderService).create(cartItems);
        inOrder.verify(cartService).clear();
    }

    @Test
    void buy_shouldNotClearCartWhenOrderIsNotCreated() {
        when(cartService.getCartItems()).thenReturn(List.of());
        when(orderService.create(List.of())).thenThrow(new EmptyCartException());

        assertThatThrownBy(() -> orderFacade.buy()).isInstanceOf(EmptyCartException.class);
        verify(cartService, never()).clear();
    }

    @Test
    void findById_shouldMapOrderWithItems() {
        when(orderService.findById(1L)).thenReturn(order(1L, 5000));

        assertThat(orderFacade.findById(1L)).isEqualTo(new OrderResponseDto(
                1L,
                List.of(new OrderItemResponseDto(10L, "Мяч", 2500, 2)),
                5000
        ));
    }

    @Test
    void findAll_shouldMapAllOrders() {
        when(orderService.findAll()).thenReturn(List.of(order(2L, 5000), order(1L, 5000)));

        assertThat(orderFacade.findAll())
                .extracting(OrderResponseDto::id)
                .containsExactly(2L, 1L);
    }

    private OrderEntity order(long id, long totalSum) {
        OrderEntity order = OrderEntity.builder()
                .id(id)
                .totalSum(totalSum)
                .build();
        order.addItem(OrderItemEntity.builder()
                .item(ItemEntity.builder().id(10L).build())
                .title("Мяч")
                .price(2500)
                .count(2)
                .build());
        return order;
    }
}
