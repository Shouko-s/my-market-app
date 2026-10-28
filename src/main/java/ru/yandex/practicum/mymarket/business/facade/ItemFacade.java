package ru.yandex.practicum.mymarket.business.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.business.dto.request.CartAction;
import ru.yandex.practicum.mymarket.business.dto.request.ItemRequestDto;
import ru.yandex.practicum.mymarket.business.dto.request.ItemSort;
import ru.yandex.practicum.mymarket.business.dto.response.ItemPageResponseDto;
import ru.yandex.practicum.mymarket.business.dto.response.ItemResponseDto;
import ru.yandex.practicum.mymarket.business.dto.response.PagingResponseDto;
import ru.yandex.practicum.mymarket.business.mapper.ItemMapper;
import ru.yandex.practicum.mymarket.business.service.CartService;
import ru.yandex.practicum.mymarket.business.service.ItemService;
import ru.yandex.practicum.mymarket.dal.entities.ItemEntity;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemFacade {

    private static final int NOT_IN_CART = 0;

    private final ItemService itemService;
    private final CartService cartService;
    private final ItemMapper mapper;

    @Transactional
    public ItemResponseDto create(ItemRequestDto dto) {
        return mapper.toResponse(itemService.create(dto), NOT_IN_CART);
    }

    public ItemResponseDto findById(long id) {
        return mapper.toResponse(itemService.findById(id), cartService.getCount(id));
    }

    public ItemPageResponseDto findAll(String search, ItemSort sort, int pageNumber, int pageSize) {
        Page<ItemEntity> page = itemService.findAll(search, sort, pageNumber, pageSize);
        Map<Long, Integer> counts = cartService.getCounts(page.map(ItemEntity::getId).getContent());

        return new ItemPageResponseDto(
                page.map(entity -> mapper.toResponse(entity, counts.getOrDefault(entity.getId(), NOT_IN_CART))).getContent(),
                new PagingResponseDto(pageSize, pageNumber, page.hasPrevious(), page.hasNext())
        );
    }

    @Transactional
    public void changeCartCount(long id, CartAction action) {
        cartService.changeCount(id, action);
    }
}
