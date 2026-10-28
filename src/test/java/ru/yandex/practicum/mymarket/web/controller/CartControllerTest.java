package ru.yandex.practicum.mymarket.web.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.mymarket.business.dto.request.CartAction;
import ru.yandex.practicum.mymarket.business.dto.response.CartResponseDto;
import ru.yandex.practicum.mymarket.business.dto.response.ItemResponseDto;
import ru.yandex.practicum.mymarket.business.exception.ItemNotFoundException;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CartControllerTest extends AbstractControllerTest {

    private static final List<ItemResponseDto> ITEMS = List.of(
            new ItemResponseDto(1L, "Мяч", "Кожаный мяч", "images/1.png", 2500, 2),
            new ItemResponseDto(2L, "Скакалка", "Со счётчиком", "images/2.png", 800, 1)
    );

    @Test
    void getCart_shouldReturnItemsAndTotal() throws Exception {
        when(cartService.getCart()).thenReturn(new CartResponseDto(ITEMS, 5800));

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", ITEMS))
                .andExpect(model().attribute("total", 5800L));
    }

    @Test
    void getCart_shouldRenderEmptyCart() throws Exception {
        when(cartService.getCart()).thenReturn(new CartResponseDto(List.of(), 0));

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("items", List.of()))
                .andExpect(model().attribute("total", 0L));
    }

    @Test
    void changeCartCount_shouldApplyActionAndReturnUpdatedCart() throws Exception {
        when(cartService.getCart()).thenReturn(new CartResponseDto(ITEMS, 5800));

        for (CartAction action : CartAction.values()) {
            mockMvc.perform(post("/cart/items").param("id", "1").param("action", action.name()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("cart"))
                    .andExpect(model().attribute("items", ITEMS))
                    .andExpect(model().attribute("total", 5800L));

            verify(cartService).changeCount(1L, action);
        }
    }

    @Test
    void changeCartCount_shouldRejectUnknownAction() throws Exception {
        mockMvc.perform(post("/cart/items").param("id", "1").param("action", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeCartCount_shouldReturnNotFoundForUnknownItem() throws Exception {
        doThrow(new ItemNotFoundException(99L)).when(cartService).changeCount(99L, CartAction.PLUS);

        mockMvc.perform(post("/cart/items").param("id", "99").param("action", "PLUS"))
                .andExpect(status().isNotFound());
    }
}
