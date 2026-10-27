package ru.yandex.practicum.mymarket.dal.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.mymarket.dal.entities.OrderEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @EntityGraph(attributePaths = "items")
    List<OrderEntity> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = "items")
    Optional<OrderEntity> findWithItemsById(long id);
}
