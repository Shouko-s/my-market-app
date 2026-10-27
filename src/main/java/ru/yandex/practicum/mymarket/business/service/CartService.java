package ru.yandex.practicum.mymarket.business.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.business.dto.request.CartAction;
import ru.yandex.practicum.mymarket.business.dto.response.CartResponseDto;
import ru.yandex.practicum.mymarket.business.dto.response.ItemResponseDto;
import ru.yandex.practicum.mymarket.business.mapper.ItemMapper;
import ru.yandex.practicum.mymarket.dal.entities.CartItemEntity;
import ru.yandex.practicum.mymarket.dal.repository.CartItemRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ItemService itemService;
    private final ItemMapper mapper;

    @Transactional
    public void changeCount(long itemId, CartAction action) {
        switch (action) {
            case PLUS -> increment(itemId);
            case MINUS -> decrement(itemId);
            case DELETE -> cartItemRepository.findByItemId(itemId).ifPresent(cartItemRepository::delete);
        }
    }

    @Transactional
    public void clear() {
        cartItemRepository.deleteAllInBatch();
    }

    public List<CartItemEntity> getCartItems() {
        return cartItemRepository.findAllByOrderByIdAsc();
    }

    public CartResponseDto getCart() {
        List<ItemResponseDto> items = getCartItems().stream()
                .map(cartItem -> mapper.toResponse(cartItem.getItem(), cartItem.getCount()))
                .toList();
        long total = items.stream()
                .mapToLong(item -> item.price() * item.count())
                .sum();
        return new CartResponseDto(items, total);
    }

    public int getCount(long itemId) {
        return cartItemRepository.findByItemId(itemId)
                .map(CartItemEntity::getCount)
                .orElse(0);
    }

    public Map<Long, Integer> getCounts(Collection<Long> itemIds) {
        return cartItemRepository.findByItemIdIn(itemIds).stream()
                .collect(Collectors.toMap(cartItem -> cartItem.getItem().getId(), CartItemEntity::getCount));
    }

    private void increment(long itemId) {
        cartItemRepository.findByItemId(itemId)
                .ifPresentOrElse(
                        cartItem -> cartItem.setCount(cartItem.getCount() + 1),
                        () -> cartItemRepository.save(newCartItem(itemId))
                );
    }

    private void decrement(long itemId) {
        cartItemRepository.findByItemId(itemId).ifPresent(cartItem -> {
            if (cartItem.getCount() > 1) {
                cartItem.setCount(cartItem.getCount() - 1);
            } else {
                cartItemRepository.delete(cartItem);
            }
        });
    }

    private CartItemEntity newCartItem(long itemId) {
        return CartItemEntity.builder()
                .item(itemService.findById(itemId))
                .count(1)
                .build();
    }
}
