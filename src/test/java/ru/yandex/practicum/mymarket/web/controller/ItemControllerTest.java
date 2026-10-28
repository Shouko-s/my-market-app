package ru.yandex.practicum.mymarket.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import ru.yandex.practicum.mymarket.business.dto.request.CartAction;
import ru.yandex.practicum.mymarket.business.dto.request.ItemSort;
import ru.yandex.practicum.mymarket.business.dto.response.ItemPageResponseDto;
import ru.yandex.practicum.mymarket.business.dto.response.ItemResponseDto;
import ru.yandex.practicum.mymarket.business.dto.response.PagingResponseDto;
import ru.yandex.practicum.mymarket.business.exception.ItemNotFoundException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ItemControllerTest extends AbstractControllerTest {

    private static final ItemResponseDto EMPTY_CELL = new ItemResponseDto(-1, null, null, null, 0, 0);

    @Test
    void getItems_shouldUseDefaultParamsAndSplitItemsIntoRowsOfThree() throws Exception {
        List<ItemResponseDto> items = List.of(item(1), item(2), item(3), item(4));
        PagingResponseDto paging = new PagingResponseDto(5, 1, false, false);
        when(itemFacade.findAll(null, ItemSort.NO, 1, 5)).thenReturn(new ItemPageResponseDto(items, paging));

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attribute("items", List.of(
                        List.of(item(1), item(2), item(3)),
                        List.of(item(4), EMPTY_CELL, EMPTY_CELL)
                )))
                .andExpect(model().attribute("sort", "NO"))
                .andExpect(model().attribute("paging", paging));
    }

    @Test
    void getItems_shouldBeAvailableFromRoot() throws Exception {
        when(itemFacade.findAll(null, ItemSort.NO, 1, 5))
                .thenReturn(new ItemPageResponseDto(List.of(), new PagingResponseDto(5, 1, false, false)));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attribute("items", List.of()));
    }

    @Test
    void getItems_shouldPassSearchSortAndPagingToFacade() throws Exception {
        PagingResponseDto paging = new PagingResponseDto(10, 2, true, false);
        when(itemFacade.findAll("мяч", ItemSort.PRICE, 2, 10))
                .thenReturn(new ItemPageResponseDto(List.of(item(1)), paging));

        mockMvc.perform(get("/items")
                        .param("search", "мяч")
                        .param("sort", "PRICE")
                        .param("pageNumber", "2")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("search", "мяч"))
                .andExpect(model().attribute("sort", "PRICE"))
                .andExpect(model().attribute("paging", paging));
    }

    @Test
    void getItems_shouldRejectInvalidParams() throws Exception {
        mockMvc.perform(get("/items").param("sort", "UNKNOWN")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/items").param("pageNumber", "0")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/items").param("pageSize", "0")).andExpect(status().isBadRequest());

        verifyNoInteractions(itemFacade);
    }

    @Test
    void changeCartCountFromItems_shouldRedirectBackWithSameParams() throws Exception {
        mockMvc.perform(post("/items")
                        .param("id", "1")
                        .param("action", "PLUS")
                        .param("search", "мяч")
                        .param("sort", "ALPHA")
                        .param("pageNumber", "2")
                        .param("pageSize", "10"))
                .andExpect(redirectedUrl("/items?search=%D0%BC%D1%8F%D1%87&sort=ALPHA&pageNumber=2&pageSize=10"));

        verify(itemFacade).changeCartCount(1L, CartAction.PLUS);
    }

    @Test
    void changeCartCountFromItems_shouldRedirectWithDefaultsWhenParamsAreMissing() throws Exception {
        mockMvc.perform(post("/items").param("id", "1").param("action", "MINUS"))
                .andExpect(redirectedUrl("/items?sort=NO&pageNumber=1&pageSize=5"));

        verify(itemFacade).changeCartCount(1L, CartAction.MINUS);
    }

    @Test
    void changeCartCountFromItems_shouldRequireIdAndAction() throws Exception {
        mockMvc.perform(post("/items").param("action", "PLUS")).andExpect(status().isBadRequest());
        mockMvc.perform(post("/items").param("id", "1")).andExpect(status().isBadRequest());

        verify(itemFacade, never()).changeCartCount(anyLong(), any());
    }

    @Test
    void getItem_shouldReturnItemPage() throws Exception {
        when(itemFacade.findById(1L)).thenReturn(item(1));

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attribute("item", item(1)));
    }

    @Test
    void getItem_shouldReturnNotFoundForUnknownItem() throws Exception {
        when(itemFacade.findById(99L)).thenThrow(new ItemNotFoundException(99L));

        mockMvc.perform(get("/items/99")).andExpect(status().isNotFound());
    }

    @Test
    void changeCartCountFromItem_shouldChangeCountAndReturnUpdatedItem() throws Exception {
        when(itemFacade.findById(1L)).thenReturn(item(1));

        mockMvc.perform(post("/items/1").param("action", "PLUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attribute("item", item(1)));

        InOrder inOrder = inOrder(itemFacade);
        inOrder.verify(itemFacade).changeCartCount(1L, CartAction.PLUS);
        inOrder.verify(itemFacade).findById(1L);
    }

    private ItemResponseDto item(long id) {
        return new ItemResponseDto(id, "Товар " + id, "Описание", "images/" + id + ".png", 100 * id, 0);
    }
}
