package ru.yandex.practicum.mymarket.dal.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mymarket.dal.entities.ItemEntity;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
abstract class AbstractRepositoryTest {

    @Autowired
    protected TestEntityManager entityManager;

    protected ItemEntity persistItem(String title, String description, long price) {
        return entityManager.persist(ItemEntity.builder()
                .title(title)
                .description(description)
                .price(price)
                .build());
    }
}
