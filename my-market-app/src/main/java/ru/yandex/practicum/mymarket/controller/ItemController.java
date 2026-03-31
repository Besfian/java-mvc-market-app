package ru.yandex.practicum.mymarket.controller;


import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.mymarket.dto.PagingDto;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ItemController {

    private final ItemService itemService;
    private final CartService cartService;

    public ItemController(ItemService itemService, CartService cartService) {
        this.itemService = itemService;
        this.cartService = cartService;
    }

    @GetMapping({"/", "/items"})
    public String getItems(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "NO") String sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize,
            Model model) {

        Page<Item> itemsPage = itemService.getItems(search, sort, pageNumber - 1, pageSize);

        List<Item> itemsList = itemsPage.getContent();

        itemsList.forEach(item -> {
            item.setCount(cartService.getItemCount(item.getId()));
        });

        List<List<Item>> itemsGrid = new ArrayList<>();
        for (int i = 0; i < itemsList.size(); i += 3) {
            List<Item> row = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                if (i + j < itemsList.size()) {
                    row.add(itemsList.get(i + j));
                } else {
                    Item placeholder = new Item();
                    placeholder.setId(-1L);
                    row.add(placeholder);
                }
            }
            itemsGrid.add(row);
        }

        PagingDto paging = new PagingDto(
                pageSize,
                pageNumber,
                pageNumber > 1,
                itemsPage.hasNext()
        );

        model.addAttribute("items", itemsGrid);
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("sort", sort);
        model.addAttribute("paging", paging);

        return "items";
    }

    @PostMapping("/items")
    public String updateCartFromItems(
            @RequestParam Long id,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "NO") String sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam String action) {

        switch (action) {
            case "PLUS" -> cartService.increaseItem(id);
            case "MINUS" -> cartService.decreaseItem(id);
        }

        return String.format("redirect:/items?search=%s&sort=%s&pageNumber=%d&pageSize=%d",
                search != null ? search : "", sort, pageNumber, pageSize);
    }

    @GetMapping("/items/{id}")
    public String getItem(@PathVariable Long id, Model model) {
        Item item = itemService.getItemById(id);
        item.setCount(cartService.getItemCount(id));
        model.addAttribute("item", item);
        return "item";
    }

    @PostMapping("/items/{id}")
    public String updateCartFromItem(
            @PathVariable Long id,
            @RequestParam String action,
            Model model) {

        switch (action) {
            case "PLUS" -> cartService.increaseItem(id);
            case "MINUS" -> cartService.decreaseItem(id);
        }

        Item item = itemService.getItemById(id);
        item.setCount(cartService.getItemCount(id));
        model.addAttribute("item", item);
        return "item";
    }
}
