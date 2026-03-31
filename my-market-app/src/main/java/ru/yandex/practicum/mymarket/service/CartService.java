package ru.yandex.practicum.mymarket.service;



import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@SessionScope
public class CartService {

    private final Map<Long, Integer> cartItems = new HashMap<>();
    private final ItemRepository itemRepository;

    public CartService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public void addItem(Long itemId) {
        cartItems.merge(itemId, 1, Integer::sum);
    }

    public void removeItem(Long itemId) {
        cartItems.remove(itemId);
    }

    public void decreaseItem(Long itemId) {
        cartItems.computeIfPresent(itemId, (id, count) -> {
            if (count <= 1) {
                return null;
            }
            return count - 1;
        });
    }

    public void increaseItem(Long itemId) {
        cartItems.merge(itemId, 1, Integer::sum);
    }

    public List<Item> getCartItems() {
        return cartItems.entrySet().stream()
                .map(entry -> {
                    Item item = itemRepository.findById(entry.getKey()).orElse(null);
                    if (item != null) {
                        item.setCount(entry.getValue());
                    }
                    return item;
                })
                .filter(item -> item != null)
                .collect(Collectors.toList());
    }

    public long getTotalSum() {
        return getCartItems().stream()
                .mapToLong(item -> item.getPrice() * item.getCount())
                .sum();
    }

    public void clear() {
        cartItems.clear();
    }

    public int getItemCount(Long itemId) {
        return cartItems.getOrDefault(itemId, 0);
    }
}
