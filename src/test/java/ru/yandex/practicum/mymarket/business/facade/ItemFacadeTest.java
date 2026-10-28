package ru.yandex.practicum.mymarket.business.facade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemFacadeTest {

    @Mock
    private ItemService itemService;

    @Mock
    private CartService cartService;

    @Spy
    private ItemMapper mapper;

    @InjectMocks
    private ItemFacade itemFacade;

    @Test
    void findAll_shouldAddCartCountsAndPaging() {
        ItemEntity ball = item(1L, "Мяч", 2500);
        ItemEntity bat = item(2L, "Бита", 4000);
        when(itemService.findAll("б", ItemSort.PRICE, 2, 2))
                .thenReturn(new PageImpl<>(List.of(ball, bat), PageRequest.of(1, 2), 5));
        when(cartService.getCounts(List.of(1L, 2L))).thenReturn(Map.of(1L, 3));

        ItemPageResponseDto page = itemFacade.findAll("б", ItemSort.PRICE, 2, 2);

        assertThat(page.items()).containsExactly(
                new ItemResponseDto(1L, "Мяч", null, "images/1.png", 2500, 3),
                new ItemResponseDto(2L, "Бита", null, "images/2.png", 4000, 0)
        );
        assertThat(page.paging()).isEqualTo(new PagingResponseDto(2, 2, true, true));
    }

    @Test
    void findAll_shouldMarkLastPage() {
        when(itemService.findAll(null, ItemSort.NO, 1, 5))
                .thenReturn(new PageImpl<>(List.of(item(1L, "Мяч", 2500)), PageRequest.of(0, 5), 1));
        when(cartService.getCounts(List.of(1L))).thenReturn(Map.of());

        ItemPageResponseDto page = itemFacade.findAll(null, ItemSort.NO, 1, 5);

        assertThat(page.paging()).isEqualTo(new PagingResponseDto(5, 1, false, false));
    }

    @Test
    void findById_shouldReturnItemWithCartCount() {
        when(itemService.findById(1L)).thenReturn(item(1L, "Мяч", 2500));
        when(cartService.getCount(1L)).thenReturn(2);

        assertThat(itemFacade.findById(1L))
                .isEqualTo(new ItemResponseDto(1L, "Мяч", null, "images/1.png", 2500, 2));
    }

    @Test
    void create_shouldReturnItemThatIsNotInCart() {
        ItemRequestDto dto = new ItemRequestDto();
        when(itemService.create(dto)).thenReturn(item(1L, "Мяч", 2500));

        assertThat(itemFacade.create(dto).count()).isZero();
        verifyNoInteractions(cartService);
    }

    @Test
    void changeCartCount_shouldDelegateToCartService() {
        itemFacade.changeCartCount(1L, CartAction.PLUS);

        verify(cartService).changeCount(1L, CartAction.PLUS);
    }

    private ItemEntity item(long id, String title, long price) {
        return ItemEntity.builder()
                .id(id)
                .title(title)
                .imgPath("images/" + id + ".png")
                .price(price)
                .build();
    }
}
