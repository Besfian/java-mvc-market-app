package ru.yandex.practicum.mymarket.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

@Service
public class ItemService {
    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Mono<Item> getItemById(Long id) {
        return itemRepository.findById(id);
    }

    public Mono<Page<Item>> getItems(String search, String sort, int page, int size) {
        long offset = (long) page * size;
        if (search != null && !search.trim().isEmpty()) {
            return itemRepository.searchItems(search, sort, size, offset)
                    .collectList()
                    .zipWith(itemRepository.countBySearch(search))
                    .map(tuple -> new PageImpl<>(tuple.getT1(), PageRequest.of(page, size), tuple.getT2()));
        } else {
            return itemRepository.findAllWithSort(sort, size, offset)
                    .collectList()
                    .zipWith(itemRepository.count())
                    .map(tuple -> new PageImpl<>(tuple.getT1(), PageRequest.of(page, size), tuple.getT2()));
        }
    }
}