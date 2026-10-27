package ru.yandex.practicum.mymarket.business.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.mymarket.business.dto.request.ItemRequestDto;
import ru.yandex.practicum.mymarket.business.dto.request.ItemSort;
import ru.yandex.practicum.mymarket.business.dto.response.ItemPageResponseDto;
import ru.yandex.practicum.mymarket.business.dto.response.ItemResponseDto;
import ru.yandex.practicum.mymarket.business.dto.response.PagingResponseDto;
import ru.yandex.practicum.mymarket.business.exception.ItemNotFoundException;
import ru.yandex.practicum.mymarket.business.mapper.ItemMapper;
import ru.yandex.practicum.mymarket.dal.entities.ItemEntity;
import ru.yandex.practicum.mymarket.dal.repository.ItemRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {
    // TODO: брать количество из корзины, когда появится CartService
    private static final int NOT_IN_CART = 0;

    private final ItemRepository repository;
    private final ItemMapper mapper;
    private final ImageStorageService imageStorageService;

    @Transactional
    public ItemResponseDto create(ItemRequestDto dto) {
        String imgPath = imageStorageService.save(dto.getImage());
        ItemEntity saved = repository.save(mapper.toEntity(dto, imgPath));
        return mapper.toResponse(saved, NOT_IN_CART);
    }

    public ItemResponseDto findById(long id) {
        return repository.findById(id)
                .map(entity -> mapper.toResponse(entity, NOT_IN_CART))
                .orElseThrow(() -> new ItemNotFoundException(id));
    }

    public ItemPageResponseDto findAll(String search, ItemSort sort, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, toSort(sort));
        Page<ItemEntity> page = StringUtils.hasText(search)
                ? repository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search, pageable)
                : repository.findAll(pageable);

        return new ItemPageResponseDto(
                page.map(entity -> mapper.toResponse(entity, NOT_IN_CART)).getContent(),
                new PagingResponseDto(pageSize, pageNumber, page.hasPrevious(), page.hasNext())
        );
    }

    private Sort toSort(ItemSort sort) {
        return switch (sort) {
            case ALPHA -> Sort.by("title");
            case PRICE -> Sort.by("price");
            case NO -> Sort.by("id");
        };
    }
}
