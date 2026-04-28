package ru.yandex.practicum.mymarket.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.PagingDto;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ItemController {
    private static final Logger log = LoggerFactory.getLogger(ItemController.class);

    private final ItemService itemService;
    private final CartService cartService;

    public ItemController(ItemService itemService, CartService cartService) {
        this.itemService = itemService;
        this.cartService = cartService;
    }

    @GetMapping({"/", "/items"})
    public Mono<String> getItems(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "NO") String sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize,
            Model model,
            WebSession session) {
        log.info("GET /items - search: {}, sort: {}, pageNumber: {}, pageSize: {}, session: {}",
                search, sort, pageNumber, pageSize, session.getId());
        return itemService.getItems(search, sort, pageNumber - 1, pageSize)
                .doOnNext(itemsPage -> log.debug("Fetched {} items, total: {}",
                        itemsPage.getContent().size(), itemsPage.getTotalElements()))
                .flatMap(itemsPage -> {
                    List<Item> itemsList = itemsPage.getContent();
                    return Flux.fromIterable(itemsList)
                            .flatMap(item -> cartService.getItemCount(item.getId(), session)
                                    .doOnNext(count -> log.debug("Item {} has count: {}", item.getId(), count))
                                    .doOnNext(item::setCount)
                                    .thenReturn(item))
                            .collectList()
                            .map(enrichedItems -> {
                                log.debug("Enriched {} items with cart counts", enrichedItems.size());
                                List<List<Item>> itemsGrid = new ArrayList<>();
                                for (int i = 0; i < enrichedItems.size(); i += 3) {
                                    List<Item> row = new ArrayList<>();
                                    for (int j = 0; j < 3; j++) {
                                        if (i + j < enrichedItems.size()) {
                                            row.add(enrichedItems.get(i + j));
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
                            });
                });
    }

    @PostMapping("/items")
    public Mono<String> updateCartFromItems(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "NO") String sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(required = false) String action,
            WebSession session) {
        String redirectUrl = String.format(
                "redirect:/items?search=%s&sort=%s&pageNumber=%d&pageSize=%d",
                search != null ? search : "", sort, pageNumber, pageSize);
        if (id == null || action == null) {
            log.warn("Missing required parameters - id: {}, action: {}", id, action);
            return Mono.just(redirectUrl);
        }
        log.info("POST /items - id: {}, action: {}, session: {}", id, action, session.getId());
        Mono<?> cartAction;
        switch (action) {
            case "PLUS" -> {
                log.debug("Increasing item {} in cart", id);
                cartAction = cartService.increaseItem(id, session);
            }
            case "MINUS" -> {
                log.debug("Decreasing item {} in cart", id);
                cartAction = cartService.decreaseItem(id, session);
            }
            default -> {
                log.warn("Unknown action: {}", action);
                return Mono.just(redirectUrl);
            }
        }
        return cartAction.then(Mono.just(redirectUrl));
    }

    @GetMapping("/items/{id}")
    public Mono<String> getItem(@PathVariable Long id, Model model, WebSession session) {
        log.info("GET /items/{} - session: {}", id, session.getId());
        return itemService.getItemById(id)
                .doOnNext(item -> log.debug("Found item: {}", item.getTitle()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Item with id {} not found", id);
                    return Mono.empty();
                }))
                .flatMap(item -> cartService.getItemCount(id, session)
                        .doOnNext(count -> log.debug("Item {} has count: {}", id, count))
                        .doOnNext(item::setCount)
                        .thenReturn(item))
                .doOnNext(item -> model.addAttribute("item", item))
                .thenReturn("item");
    }

    @PostMapping("/items/{id}")
    public Mono<String> updateCartFromItem(
            @PathVariable Long id,
            @RequestParam(name = "action") String action,
            Model model,
            WebSession session) {
        log.info("POST /items/{} - action: {}, session: {}", id, action, session.getId());
        Mono<?> cartAction;
        switch (action) {
            case "PLUS":
                cartAction = cartService.increaseItem(id, session);
                break;
            case "MINUS":
                cartAction = cartService.decreaseItem(id, session);
                break;
            default:
                cartAction = Mono.empty();
        }
        return cartAction
                .then(itemService.getItemById(id))
                .flatMap(item -> cartService.getItemCount(id, session)
                        .doOnNext(item::setCount)
                        .thenReturn(item))
                .doOnNext(item -> model.addAttribute("item", item))
                .thenReturn("item");
    }
}