package ru.yandex.practicum.mymarket.dal.repository;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import ru.yandex.practicum.mymarket.dal.entities.CartItemEntity;
import ru.yandex.practicum.mymarket.dal.entities.ItemEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartItemRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CartItemRepository repository;

    private ItemEntity ball;
    private ItemEntity bat;
    private ItemEntity rope;

    @BeforeEach
    void setUp() {
        ball = persistItem("Мяч", "Кожаный мяч", 2500);
        bat = persistItem("Бита", "Бейсбольная бита", 4000);
        rope = persistItem("Скакалка", "Со счётчиком", 800);
        persistCartItem(bat, 1);
        persistCartItem(ball, 3);
        entityManager.clear();
    }

    @Test
    void findByItemId_shouldReturnCartItemOfThatItem() {
        assertThat(repository.findByItemId(ball.getId()))
                .get()
                .extracting(CartItemEntity::getCount)
                .isEqualTo(3);
    }

    @Test
    void findByItemId_shouldReturnEmptyWhenItemIsNotInCart() {
        assertThat(repository.findByItemId(rope.getId())).isEmpty();
    }

    @Test
    void findByItemIdIn_shouldReturnOnlyItemsFromCart() {
        List<CartItemEntity> cartItems = repository.findByItemIdIn(List.of(ball.getId(), rope.getId()));

        assertThat(cartItems)
                .singleElement()
                .extracting(CartItemEntity::getCount)
                .isEqualTo(3);
    }

    @Test
    void findAllByOrderByIdAsc_shouldReturnCartItemsInAddingOrderWithLoadedItems() {
        List<CartItemEntity> cartItems = repository.findAllByOrderByIdAsc();

        assertThat(cartItems)
                .extracting(cartItem -> cartItem.getItem().getTitle())
                .containsExactly("Бита", "Мяч");
        assertThat(cartItems).allMatch(cartItem -> Hibernate.isInitialized(cartItem.getItem()));
    }

    @Test
    void save_shouldNotAllowSameItemTwice() {
        CartItemEntity duplicate = CartItemEntity.builder()
                .item(entityManager.find(ItemEntity.class, ball.getId()))
                .count(1)
                .build();

        assertThatThrownBy(() -> repository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void persistCartItem(ItemEntity item, int count) {
        entityManager.persist(CartItemEntity.builder()
                .item(item)
                .count(count)
                .build());
    }
}
