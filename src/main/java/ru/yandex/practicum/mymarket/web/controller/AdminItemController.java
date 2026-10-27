package ru.yandex.practicum.mymarket.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.practicum.mymarket.business.dto.request.ItemRequestDto;
import ru.yandex.practicum.mymarket.business.dto.response.ItemResponseDto;
import ru.yandex.practicum.mymarket.business.service.ItemService;

@Controller
@RequestMapping("/admin/items")
@RequiredArgsConstructor
public class AdminItemController {

    private static final String FORM_VIEW = "admin/item-form";

    private final ItemService itemService;

    @GetMapping("/new")
    public String newItemForm(Model model) {
        model.addAttribute("item", new ItemRequestDto());
        return FORM_VIEW;
    }

    @PostMapping
    public String createItem(@Valid @ModelAttribute("item") ItemRequestDto item, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return FORM_VIEW;
        }
        ItemResponseDto created = itemService.create(item);
        return "redirect:/items/" + created.id();
    }
}
