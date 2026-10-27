package ru.yandex.practicum.mymarket.business.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.mymarket.business.dto.request.CartAction;
import ru.yandex.practicum.mymarket.business.dto.response.CartResponseDto;
import ru.yandex.practicum.mymarket.business.exception.ItemNotFoundException;
import ru.yandex.practicum.mymarket.business.mapper.ItemMapper;
import ru.yandex.practicum.mymarket.dal.entities.CartItemEntity;
import ru.yandex.practicum.mymarket.dal.entities.ItemEntity;
import ru.yandex.practicum.mymarket.dal.repository.CartItemRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    private static final long ITEM_ID = 1L;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemService itemService;

    @Spy
    private ItemMapper mapper;

    @InjectMocks
    private CartService cartService;

    @Test
    void changeCount_plusShouldAddNewItemToCart() {
        ItemEntity item = item(ITEM_ID, 2500);
        when(cartItemRepository.findByItemId(ITEM_ID)).thenReturn(Optional.empty());
        when(itemService.findById(ITEM_ID)).thenReturn(item);

        cartService.changeCount(ITEM_ID, CartAction.PLUS);

        ArgumentCaptor<CartItemEntity> saved = ArgumentCaptor.forClass(CartItemEntity.class);
        verify(cartItemRepository).save(saved.capture());
        assertThat(saved.getValue().getItem()).isSameAs(item);
        assertThat(saved.getValue().getCount()).isEqualTo(1);
    }

    @Test
    void changeCount_plusShouldIncrementExistingItem() {
        CartItemEntity cartItem = cartItem(item(ITEM_ID, 2500), 2);
        when(cartItemRepository.findByItemId(ITEM_ID)).thenReturn(Optional.of(cartItem));

        cartService.changeCount(ITEM_ID, CartAction.PLUS);

        assertThat(cartItem.getCount()).isEqualTo(3);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void changeCount_plusShouldFailForUnknownItem() {
        when(cartItemRepository.findByItemId(ITEM_ID)).thenReturn(Optional.empty());
        when(itemService.findById(ITEM_ID)).thenThrow(new ItemNotFoundException(ITEM_ID));

        assertThatThrownBy(() -> cartService.changeCount(ITEM_ID, CartAction.PLUS))
                .isInstanceOf(ItemNotFoundException.class);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void changeCount_minusShouldDecrementItem() {
        CartItemEntity cartItem = cartItem(item(ITEM_ID, 2500), 2);
        when(cartItemRepository.findByItemId(ITEM_ID)).thenReturn(Optional.of(cartItem));

        cartService.changeCount(ITEM_ID, CartAction.MINUS);

        assertThat(cartItem.getCount()).isEqualTo(1);
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void changeCount_minusShouldRemoveLastItem() {
        CartItemEntity cartItem = cartItem(item(ITEM_ID, 2500), 1);
        when(cartItemRepository.findByItemId(ITEM_ID)).thenReturn(Optional.of(cartItem));

        cartService.changeCount(ITEM_ID, CartAction.MINUS);

        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void changeCount_minusShouldDoNothingWhenItemIsNotInCart() {
        when(cartItemRepository.findByItemId(ITEM_ID)).thenReturn(Optional.empty());

        cartService.changeCount(ITEM_ID, CartAction.MINUS);

        verify(cartItemRepository, never()).delete(any());
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void changeCount_deleteShouldRemoveItemRegardlessOfCount() {
        CartItemEntity cartItem = cartItem(item(ITEM_ID, 2500), 5);
        when(cartItemRepository.findByItemId(ITEM_ID)).thenReturn(Optional.of(cartItem));

        cartService.changeCount(ITEM_ID, CartAction.DELETE);

        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void getCart_shouldReturnItemsWithCountsAndTotal() {
        when(cartItemRepository.findAllByOrderByIdAsc()).thenReturn(List.of(
                cartItem(item(1L, 2500), 2),
                cartItem(item(2L, 800), 3)
        ));

        CartResponseDto cart = cartService.getCart();

        assertThat(cart.items())
                .extracting(item -> item.id() + ":" + item.count())
                .containsExactly("1:2", "2:3");
        assertThat(cart.total()).isEqualTo(2 * 2500 + 3 * 800);
    }

    @Test
    void getCart_shouldReturnZeroTotalForEmptyCart() {
        when(cartItemRepository.findAllByOrderByIdAsc()).thenReturn(List.of());

        CartResponseDto cart = cartService.getCart();

        assertThat(cart.items()).isEmpty();
        assertThat(cart.total()).isZero();
    }

    @Test
    void getCount_shouldReturnZeroWhenItemIsNotInCart() {
        when(cartItemRepository.findByItemId(ITEM_ID)).thenReturn(Optional.empty());

        assertThat(cartService.getCount(ITEM_ID)).isZero();
    }

    @Test
    void getCounts_shouldMapItemIdToCount() {
        List<Long> ids = List.of(1L, 2L, 3L);
        when(cartItemRepository.findByItemIdIn(ids)).thenReturn(List.of(
                cartItem(item(1L, 2500), 2),
                cartItem(item(3L, 800), 1)
        ));

        assertThat(cartService.getCounts(ids)).isEqualTo(Map.of(1L, 2, 3L, 1));
    }

    @Test
    void clear_shouldDeleteAllCartItems() {
        cartService.clear();

        verify(cartItemRepository).deleteAllInBatch();
    }

    private ItemEntity item(long id, long price) {
        return ItemEntity.builder()
                .id(id)
                .title("Товар " + id)
                .price(price)
                .build();
    }

    private CartItemEntity cartItem(ItemEntity item, int count) {
        return CartItemEntity.builder()
                .item(item)
                .count(count)
                .build();
    }
}
