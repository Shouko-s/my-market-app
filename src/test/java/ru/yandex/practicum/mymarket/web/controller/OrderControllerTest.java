package ru.yandex.practicum.mymarket.web.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.mymarket.business.dto.response.OrderItemResponseDto;
import ru.yandex.practicum.mymarket.business.dto.response.OrderResponseDto;
import ru.yandex.practicum.mymarket.business.exception.EmptyCartException;
import ru.yandex.practicum.mymarket.business.exception.OrderNotFoundException;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerTest extends AbstractControllerTest {

    private static final OrderResponseDto ORDER = new OrderResponseDto(
            1L,
            List.of(new OrderItemResponseDto(1L, "Мяч", 2500, 2)),
            5000
    );

    @Test
    void getOrders_shouldReturnAllOrders() throws Exception {
        when(orderFacade.findAll()).thenReturn(List.of(ORDER));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attribute("orders", List.of(ORDER)));
    }

    @Test
    void getOrder_shouldNotMarkOrderAsNewByDefault() throws Exception {
        when(orderFacade.findById(1L)).thenReturn(ORDER);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("order", ORDER))
                .andExpect(model().attribute("newOrder", false));
    }

    @Test
    void getOrder_shouldPassNewOrderFlag() throws Exception {
        when(orderFacade.findById(1L)).thenReturn(ORDER);

        mockMvc.perform(get("/orders/1").param("newOrder", "true"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("newOrder", true));
    }

    @Test
    void getOrder_shouldReturnNotFoundForUnknownOrder() throws Exception {
        when(orderFacade.findById(99L)).thenThrow(new OrderNotFoundException(99L));

        mockMvc.perform(get("/orders/99")).andExpect(status().isNotFound());
    }

    @Test
    void buy_shouldRedirectToCreatedOrder() throws Exception {
        when(orderFacade.buy()).thenReturn(7L);

        mockMvc.perform(post("/buy"))
                .andExpect(redirectedUrl("/orders/7?newOrder=true"));
    }

    @Test
    void buy_shouldRedirectToCartWhenCartIsEmpty() throws Exception {
        when(orderFacade.buy()).thenThrow(new EmptyCartException());

        mockMvc.perform(post("/buy"))
                .andExpect(redirectedUrl("/cart/items"));
    }
}
