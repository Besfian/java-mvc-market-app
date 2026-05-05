package ru.yandex.practicum.mymarket.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

@Service
public class ItemService {
    private static final Logger log = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepository itemRepository;
    private final ItemCacheService itemCacheService;

    public ItemService(ItemRepository itemRepository, ItemCacheService itemCacheService) {
        this.itemRepository = itemRepository;
        this.itemCacheService = itemCacheService;
    }

    public Mono<Item> getItemById(Long id) {
        return itemCacheService.getItem(id)
                .switchIfEmpty(
                        itemRepository.findById(id)
                                .doOnNext(item -> log.debug("Item found in DB: {}", id))
                                .flatMap(itemCacheService::cacheItem)
                )
                .doOnNext(item -> log.debug("Retrieved item: {}", id));
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

    public Mono<Boolean> evictCache(Long id) {
        return itemCacheService.evictItem(id);
    }
}