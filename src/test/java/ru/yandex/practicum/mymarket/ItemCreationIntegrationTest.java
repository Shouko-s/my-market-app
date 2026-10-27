package ru.yandex.practicum.mymarket;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import ru.yandex.practicum.mymarket.dal.entities.ItemEntity;
import ru.yandex.practicum.mymarket.dal.repository.ItemRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ItemCreationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void shouldCreateItemWithImageAndServeImage() throws Exception {
        byte[] imageContent = {1, 2, 3};

        mockMvc.perform(multipart("/admin/items")
                        .file(new MockMultipartFile("image", "ball.png", "image/png", imageContent))
                        .param("title", "Мяч")
                        .param("description", "Кожаный мяч")
                        .param("price", "2500"))
                .andExpect(status().is3xxRedirection());

        List<ItemEntity> items = itemRepository.findAll();
        assertThat(items).singleElement().satisfies(item -> {
            assertThat(item.getTitle()).isEqualTo("Мяч");
            assertThat(item.getPrice()).isEqualTo(2500);
            assertThat(item.getImgPath()).startsWith("images/").endsWith(".png");
        });

        ItemEntity item = items.getFirst();
        mockMvc.perform(get("/" + item.getImgPath()))
                .andExpect(status().isOk())
                .andExpect(content().bytes(imageContent));
    }

    @Test
    void shouldRedirectToCreatedItemPage() throws Exception {
        String redirect = mockMvc.perform(multipart("/admin/items")
                        .param("title", "Бита")
                        .param("price", "4000"))
                .andReturn().getResponse().getRedirectedUrl();

        ItemEntity item = itemRepository.findAll().getFirst();
        assertThat(redirect).isEqualTo("/items/" + item.getId());
        assertThat(item.getImgPath()).isNull();
    }
}
