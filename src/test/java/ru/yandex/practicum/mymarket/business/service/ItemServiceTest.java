package ru.yandex.practicum.mymarket.business.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import ru.yandex.practicum.mymarket.business.dto.request.ItemRequestDto;
import ru.yandex.practicum.mymarket.business.dto.request.ItemSort;
import ru.yandex.practicum.mymarket.business.exception.ItemNotFoundException;
import ru.yandex.practicum.mymarket.business.mapper.ItemMapper;
import ru.yandex.practicum.mymarket.dal.entities.ItemEntity;
import ru.yandex.practicum.mymarket.dal.repository.ItemRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository repository;

    @Mock
    private ImageStorageService imageStorageService;

    @Spy
    private ItemMapper mapper;

    @InjectMocks
    private ItemService itemService;

    @Test
    void create_shouldSaveImageAndItem() {
        MockMultipartFile image = new MockMultipartFile("image", "ball.png", "image/png", new byte[]{1});
        ItemRequestDto dto = new ItemRequestDto();
        dto.setTitle("Мяч");
        dto.setDescription("Кожаный мяч");
        dto.setPrice(2500L);
        dto.setImage(image);
        when(imageStorageService.save(image)).thenReturn("images/ball.png");
        when(repository.save(any(ItemEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemEntity created = itemService.create(dto);

        assertThat(created.getTitle()).isEqualTo("Мяч");
        assertThat(created.getDescription()).isEqualTo("Кожаный мяч");
        assertThat(created.getPrice()).isEqualTo(2500);
        assertThat(created.getImgPath()).isEqualTo("images/ball.png");
    }

    @Test
    void findById_shouldReturnItem() {
        ItemEntity item = ItemEntity.builder().id(1L).title("Мяч").build();
        when(repository.findById(1L)).thenReturn(Optional.of(item));

        assertThat(itemService.findById(1)).isSameAs(item);
    }

    @Test
    void findById_shouldThrowWhenItemNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.findById(1))
                .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void findAll_shouldSearchByTitleAndDescriptionWhenSearchIsGiven() {
        Page<ItemEntity> page = new PageImpl<>(List.of());
        when(repository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                any(), any(), any(Pageable.class))).thenReturn(page);

        Page<ItemEntity> result = itemService.findAll("мяч", ItemSort.NO, 2, 10);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                eq("мяч"), eq("мяч"), pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(10);
        assertThat(result).isSameAs(page);
    }

    @Test
    void findAll_shouldReturnAllItemsWhenSearchIsBlank() {
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        itemService.findAll("  ", ItemSort.NO, 1, 5);

        verify(repository).findAll(any(Pageable.class));
        verify(repository, never()).findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                anyString(), anyString(), any(Pageable.class));
    }

    @ParameterizedTest
    @CsvSource({"NO, id", "ALPHA, title", "PRICE, price"})
    void findAll_shouldSortByFieldMatchingSortType(ItemSort sort, String field) {
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        itemService.findAll(null, sort, 1, 5);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(pageable.capture());
        assertThat(pageable.getValue().getSort()).isEqualTo(Sort.by(field));
    }
}
