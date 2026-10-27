package ru.yandex.practicum.mymarket.web.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yandex.practicum.mymarket.business.dto.request.CartAction;
import ru.yandex.practicum.mymarket.business.dto.request.ItemSort;
import ru.yandex.practicum.mymarket.business.dto.response.ItemPageResponseDto;
import ru.yandex.practicum.mymarket.business.dto.response.ItemResponseDto;
import ru.yandex.practicum.mymarket.business.facade.ItemFacade;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private static final int ITEMS_PER_ROW = 3;
    private static final ItemResponseDto EMPTY_CELL = new ItemResponseDto(-1, null, null, null, 0, 0);

    private final ItemFacade itemFacade;

    @GetMapping({"/", "/items"})
    public String getItems(@RequestParam(required = false) String search,
                           @RequestParam(defaultValue = "NO") ItemSort sort,
                           @RequestParam(defaultValue = "1") @Min(1) int pageNumber,
                           @RequestParam(defaultValue = "5") @Min(1) @Max(100) int pageSize,
                           Model model) {
        ItemPageResponseDto page = itemFacade.findAll(search, sort, pageNumber, pageSize);

        model.addAttribute("items", toRows(page.items()));
        model.addAttribute("search", search);
        model.addAttribute("sort", sort.name());
        model.addAttribute("paging", page.paging());
        return "items";
    }

    @PostMapping("/items")
    public String changeCartCountFromItems(@RequestParam long id,
                                           @RequestParam(required = false) String search,
                                           @RequestParam(defaultValue = "NO") ItemSort sort,
                                           @RequestParam(defaultValue = "1") int pageNumber,
                                           @RequestParam(defaultValue = "5") int pageSize,
                                           @RequestParam CartAction action,
                                           RedirectAttributes redirectAttributes) {
        itemFacade.changeCartCount(id, action);

        if (StringUtils.hasText(search)) {
            redirectAttributes.addAttribute("search", search);
        }
        redirectAttributes.addAttribute("sort", sort.name());
        redirectAttributes.addAttribute("pageNumber", pageNumber);
        redirectAttributes.addAttribute("pageSize", pageSize);
        return "redirect:/items";
    }

    @GetMapping("/items/{id}")
    public String getItem(@PathVariable long id, Model model) {
        model.addAttribute("item", itemFacade.findById(id));
        return "item";
    }

    @PostMapping("/items/{id}")
    public String changeCartCountFromItem(@PathVariable long id, @RequestParam CartAction action, Model model) {
        itemFacade.changeCartCount(id, action);
        return getItem(id, model);
    }

    private List<List<ItemResponseDto>> toRows(List<ItemResponseDto> items) {
        List<List<ItemResponseDto>> rows = new ArrayList<>();
        for (int i = 0; i < items.size(); i += ITEMS_PER_ROW) {
            List<ItemResponseDto> row = new ArrayList<>(items.subList(i, Math.min(i + ITEMS_PER_ROW, items.size())));
            while (row.size() < ITEMS_PER_ROW) {
                row.add(EMPTY_CELL);
            }
            rows.add(row);
        }
        return rows;
    }
}
