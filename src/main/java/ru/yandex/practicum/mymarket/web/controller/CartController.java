package ru.yandex.practicum.mymarket.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.mymarket.business.dto.request.CartAction;
import ru.yandex.practicum.mymarket.business.dto.response.CartResponseDto;
import ru.yandex.practicum.mymarket.business.service.CartService;

@Controller
@RequestMapping("/cart/items")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public String getCart(Model model) {
        CartResponseDto cart = cartService.getCart();

        model.addAttribute("items", cart.items());
        model.addAttribute("total", cart.total());
        return "cart";
    }

    @PostMapping
    public String changeCartCount(@RequestParam long id, @RequestParam CartAction action, Model model) {
        cartService.changeCount(id, action);
        return getCart(model);
    }
}
