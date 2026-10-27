package ru.yandex.practicum.mymarket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.mymarket.business.dto.response.ItemResponseDto;
import ru.yandex.practicum.mymarket.business.dto.response.OrderItemResponseDto;
import ru.yandex.practicum.mymarket.business.dto.response.OrderResponseDto;
import ru.yandex.practicum.mymarket.dal.entities.ItemEntity;
import ru.yandex.practicum.mymarket.dal.entities.OrderEntity;
import ru.yandex.practicum.mymarket.dal.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.dal.repository.ItemRepository;
import ru.yandex.practicum.mymarket.dal.repository.OrderRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PurchaseFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    private ItemEntity ball;
    private ItemEntity rope;

    @BeforeEach
    void setUp() {
        ball = itemRepository.save(ItemEntity.builder().title("Мяч").description("Кожаный мяч").price(2500).build());
        rope = itemRepository.save(ItemEntity.builder().title("Скакалка").description("Со счётчиком").price(800).build());
    }

    @Test
    void shouldAddItemsToCartAndBuyThem() throws Exception {
        mockMvc.perform(post("/items").param("id", ball.getId().toString()).param("action", "PLUS"))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/items/" + ball.getId()).param("action", "PLUS"))
                .andExpect(model().attribute("item", itemWithCount(ball, 2)));
        mockMvc.perform(post("/cart/items").param("id", rope.getId().toString()).param("action", "PLUS"))
                .andExpect(model().attribute("total", 2 * 2500L + 800L));

        MvcResult buy = mockMvc.perform(post("/buy"))
                .andExpect(redirectedUrlPattern("/orders/*?newOrder=true"))
                .andReturn();

        assertThat(cartItemRepository.count()).isZero();
        long orderId = Long.parseLong(buy.getResponse().getRedirectedUrl().replaceAll("\\D", ""));
        OrderEntity order = orderRepository.findWithItemsById(orderId).orElseThrow();
        assertThat(order.getTotalSum()).isEqualTo(5800);
        assertThat(order.getItems())
                .extracting(item -> item.getItem().getId(), item -> item.getCount())
                .containsExactly(tuple(ball.getId(), 2), tuple(rope.getId(), 1));

        MvcResult orderPage = mockMvc.perform(get("/orders/" + orderId).param("newOrder", "true"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("newOrder", true))
                .andReturn();
        OrderResponseDto orderDto = (OrderResponseDto) orderPage.getModelAndView().getModel().get("order");
        assertThat(orderDto.totalSum()).isEqualTo(5800);
        assertThat(orderDto.items()).hasSize(2);

        mockMvc.perform(get("/cart/items"))
                .andExpect(model().attribute("items", List.of()))
                .andExpect(model().attribute("total", 0L));
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("orders", List.of(orderDto)));
    }

    @Test
    void shouldShowCartCountsOnItemsPage() throws Exception {
        mockMvc.perform(post("/cart/items").param("id", rope.getId().toString()).param("action", "PLUS"));
        mockMvc.perform(post("/cart/items").param("id", rope.getId().toString()).param("action", "PLUS"));

        mockMvc.perform(get("/items").param("sort", "PRICE"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("items", List.of(List.of(
                        itemWithCount(rope, 2),
                        itemWithCount(ball, 0),
                        new ItemResponseDto(-1, null, null, null, 0, 0)
                ))));
    }

    @Test
    void shouldKeepOrderPricesWhenItemPriceChanges() throws Exception {
        mockMvc.perform(post("/cart/items").param("id", ball.getId().toString()).param("action", "PLUS"));
        mockMvc.perform(post("/buy"));
        ball.setPrice(9999);
        itemRepository.saveAndFlush(ball);

        mockMvc.perform(get("/orders"))
                .andExpect(model().attribute("orders", List.of(new OrderResponseDto(
                        orderRepository.findAllByOrderByIdDesc().getFirst().getId(),
                        List.of(new OrderItemResponseDto(
                                ball.getId(), "Мяч", 2500, 1)),
                        2500
                ))));
    }

    @Test
    void shouldNotCreateOrderFromEmptyCart() throws Exception {
        mockMvc.perform(post("/buy"))
                .andExpect(redirectedUrl("/cart/items"));

        assertThat(orderRepository.count()).isZero();
    }

    private ItemResponseDto itemWithCount(ItemEntity item, int count) {
        return new ItemResponseDto(item.getId(), item.getTitle(), item.getDescription(), item.getImgPath(),
                item.getPrice(), count);
    }
}
