package ru.yandex.practicum.mymarket.dal.repository;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.mymarket.dal.entities.ItemEntity;
import ru.yandex.practicum.mymarket.dal.entities.OrderEntity;
import ru.yandex.practicum.mymarket.dal.entities.OrderItemEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class OrderRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private OrderRepository repository;

    private ItemEntity ball;
    private ItemEntity bat;

    @BeforeEach
    void setUp() {
        ball = persistItem("Мяч", "Кожаный мяч", 2500);
        bat = persistItem("Бита", "Бейсбольная бита", 4000);
    }

    @Test
    void save_shouldCascadeOrderItems() {
        OrderEntity saved = repository.save(order(orderItem(ball, 2), orderItem(bat, 1)));
        entityManager.flush();
        entityManager.clear();

        OrderEntity found = entityManager.find(OrderEntity.class, saved.getId());

        assertThat(found.getTotalSum()).isEqualTo(9000);
        assertThat(found.getItems())
                .extracting(OrderItemEntity::getTitle, OrderItemEntity::getPrice, OrderItemEntity::getCount)
                .containsExactly(
                        tuple("Мяч", 2500L, 2),
                        tuple("Бита", 4000L, 1)
                );
    }

    @Test
    void findWithItemsById_shouldLoadOrderItems() {
        long id = repository.save(order(orderItem(ball, 1))).getId();
        entityManager.flush();
        entityManager.clear();

        OrderEntity found = repository.findWithItemsById(id).orElseThrow();

        assertThat(Hibernate.isInitialized(found.getItems())).isTrue();
        assertThat(found.getItems()).hasSize(1);
    }

    @Test
    void findWithItemsById_shouldReturnEmptyForUnknownId() {
        assertThat(repository.findWithItemsById(Long.MAX_VALUE)).isEmpty();
    }

    @Test
    void findAllByOrderByIdDesc_shouldReturnNewestOrdersFirst() {
        long first = repository.save(order(orderItem(ball, 1))).getId();
        long second = repository.save(order(orderItem(bat, 1))).getId();
        entityManager.flush();
        entityManager.clear();

        List<OrderEntity> orders = repository.findAllByOrderByIdDesc();

        assertThat(orders).extracting(OrderEntity::getId).containsExactly(second, first);
        assertThat(orders).allMatch(order -> Hibernate.isInitialized(order.getItems()));
    }

    private OrderEntity order(OrderItemEntity... items) {
        OrderEntity order = OrderEntity.builder()
                .createdAt(LocalDateTime.now())
                .build();
        for (OrderItemEntity item : items) {
            order.addItem(item);
        }
        order.setTotalSum(order.getItems().stream()
                .mapToLong(item -> item.getPrice() * item.getCount())
                .sum());
        return order;
    }

    private OrderItemEntity orderItem(ItemEntity item, int count) {
        return OrderItemEntity.builder()
                .item(item)
                .title(item.getTitle())
                .price(item.getPrice())
                .count(count)
                .build();
    }
}
