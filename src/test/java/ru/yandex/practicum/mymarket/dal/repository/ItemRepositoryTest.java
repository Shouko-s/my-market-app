package ru.yandex.practicum.mymarket.dal.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.yandex.practicum.mymarket.dal.entities.ItemEntity;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private ItemRepository repository;

    @BeforeEach
    void setUp() {
        persistItem("Мяч футбольный", "Кожаный мяч", 2500);
        persistItem("Бита", "Бейсбольная бита", 4000);
        persistItem("Кроссовки", "Беговые, МЯЧ в подарок", 6500);
        persistItem("Скакалка", "Со счётчиком", 800);
    }

    @Test
    void findByTitleOrDescription_shouldMatchBothFieldsIgnoringCase() {
        Page<ItemEntity> page = repository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "мяч", "мяч", PageRequest.of(0, 10, Sort.by("title")));

        assertThat(page.getContent())
                .extracting(ItemEntity::getTitle)
                .containsExactly("Кроссовки", "Мяч футбольный");
    }

    @Test
    void findByTitleOrDescription_shouldReturnEmptyPageWhenNothingMatches() {
        Page<ItemEntity> page = repository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "ракетка", "ракетка", PageRequest.of(0, 10));

        assertThat(page).isEmpty();
    }

    @Test
    void findAll_shouldSortByPriceAndPaginate() {
        Page<ItemEntity> page = repository.findAll(PageRequest.of(1, 2, Sort.by("price")));

        assertThat(page.getContent())
                .extracting(ItemEntity::getPrice)
                .containsExactly(4000L, 6500L);
        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.hasPrevious()).isTrue();
        assertThat(page.hasNext()).isFalse();
    }
}
