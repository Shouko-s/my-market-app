package ru.yandex.practicum.mymarket.dal.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.mymarket.dal.entities.CartItemEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {

    Optional<CartItemEntity> findByItemId(long itemId);

    List<CartItemEntity> findByItemIdIn(Collection<Long> itemIds);

    @EntityGraph(attributePaths = "item")
    List<CartItemEntity> findAllByOrderByIdAsc();
}
