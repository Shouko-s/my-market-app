package ru.yandex.practicum.mymarket.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import ru.yandex.practicum.mymarket.business.dto.request.ItemRequestDto;
import ru.yandex.practicum.mymarket.business.dto.response.ItemResponseDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminItemControllerTest extends AbstractControllerTest {

    @Test
    void newItemForm_shouldReturnEmptyForm() throws Exception {
        mockMvc.perform(get("/admin/items/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/item-form"))
                .andExpect(model().attributeExists("item"));
    }

    @Test
    void createItem_shouldCreateItemAndRedirectToItemPage() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "ball.png", "image/png", new byte[]{1, 2});
        when(itemFacade.create(any(ItemRequestDto.class)))
                .thenReturn(new ItemResponseDto(5L, "Мяч", "Кожаный мяч", "images/ball.png", 2500, 0));

        mockMvc.perform(multipart("/admin/items")
                        .file(image)
                        .param("title", "Мяч")
                        .param("description", "Кожаный мяч")
                        .param("price", "2500"))
                .andExpect(redirectedUrl("/items/5"));

        ArgumentCaptor<ItemRequestDto> dto = ArgumentCaptor.forClass(ItemRequestDto.class);
        verify(itemFacade).create(dto.capture());
        assertThat(dto.getValue().getTitle()).isEqualTo("Мяч");
        assertThat(dto.getValue().getDescription()).isEqualTo("Кожаный мяч");
        assertThat(dto.getValue().getPrice()).isEqualTo(2500);
        assertThat(dto.getValue().getImage().getOriginalFilename()).isEqualTo("ball.png");
    }

    @Test
    void createItem_shouldReturnFormWithErrorsForInvalidItem() throws Exception {
        mockMvc.perform(multipart("/admin/items")
                        .param("title", " ")
                        .param("price", "-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/item-form"))
                .andExpect(model().attributeHasFieldErrors("item", "title", "price"));

        verifyNoInteractions(itemFacade);
    }

    @Test
    void createItem_shouldRequirePrice() throws Exception {
        mockMvc.perform(multipart("/admin/items").param("title", "Мяч"))
                .andExpect(view().name("admin/item-form"))
                .andExpect(model().attributeHasFieldErrors("item", "price"));

        verifyNoInteractions(itemFacade);
    }
}
