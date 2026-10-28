package ru.yandex.practicum.mymarket.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mymarket.business.facade.ItemFacade;
import ru.yandex.practicum.mymarket.business.facade.OrderFacade;
import ru.yandex.practicum.mymarket.business.service.CartService;

@WebMvcTest
@ActiveProfiles("test")
abstract class AbstractControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected ItemFacade itemFacade;

    @MockitoBean
    protected CartService cartService;

    @MockitoBean
    protected OrderFacade orderFacade;
}
