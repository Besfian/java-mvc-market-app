package ru.yandex.practicum.mymarket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.service.CartService;

import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/items")
    public String getCart(Model model) {
        List<Item> items = cartService.getCartItems();
        long total = cartService.getTotalSum();

        model.addAttribute("items", items);
        model.addAttribute("total", total);

        return "cart";
    }

    @PostMapping("/items")
    public String updateCart(
            @RequestParam Long id,
            @RequestParam String action,
            Model model) {

        switch (action) {
            case "PLUS" -> cartService.increaseItem(id);
            case "MINUS" -> cartService.decreaseItem(id);
            case "DELETE" -> cartService.removeItem(id);
        }

        List<Item> items = cartService.getCartItems();
        long total = cartService.getTotalSum();

        model.addAttribute("items", items);
        model.addAttribute("total", total);

        return "cart";
    }
}