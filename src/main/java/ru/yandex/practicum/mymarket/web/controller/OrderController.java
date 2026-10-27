package ru.yandex.practicum.mymarket.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.mymarket.business.facade.OrderFacade;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderFacade orderFacade;

    @GetMapping("/orders")
    public String getOrders(Model model) {
        model.addAttribute("orders", orderFacade.findAll());
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String getOrder(@PathVariable long id,
                           @RequestParam(defaultValue = "false") boolean newOrder,
                           Model model) {
        model.addAttribute("order", orderFacade.findById(id));
        model.addAttribute("newOrder", newOrder);
        return "order";
    }

    @PostMapping("/buy")
    public String buy() {
        long orderId = orderFacade.buy();
        return "redirect:/orders/" + orderId + "?newOrder=true";
    }
}
