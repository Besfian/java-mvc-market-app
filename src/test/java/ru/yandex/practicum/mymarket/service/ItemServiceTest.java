package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @Test
    void getItemByIdWhenExistsShouldReturnItem() {
        Item expected = createTestItem();
        when(itemRepository.findById(1L)).thenReturn(Mono.just(expected));
        StepVerifier.create(itemService.getItemById(1L))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void getItemByIdWhenNotExistsShouldReturnEmpty() {
        when(itemRepository.findById(99L)).thenReturn(Mono.empty());
        StepVerifier.create(itemService.getItemById(99L))
                .verifyComplete();
    }

    @Test
    void getItemsWithoutSearchShouldReturnAllItemsSorted() {
        Item item1 = createTestItem(1L, "Apple", 2000L);
        Item item2 = createTestItem(2L, "Banana", 1000L);
        List<Item> items = List.of(item1, item2);

        when(itemRepository.findAllWithSort(eq("PRICE"), eq(10), eq(0L)))
                .thenReturn(Flux.fromIterable(items));
        when(itemRepository.count()).thenReturn(Mono.just(2L));

        StepVerifier.create(itemService.getItems(null, "PRICE", 0, 10))
                .expectNextMatches(page -> {
                    Page<Item> result = page;
                    return result.getContent().size() == 2
                            && result.getTotalElements() == 2L
                            && result.getNumber() == 0
                            && result.getSize() == 10;
                })
                .verifyComplete();
    }

    @Test
    void getItemsWithSearchShouldReturnFilteredItems() {
        String searchQuery = "test";
        String sort = "ALPHA";
        int page = 1;
        int size = 5;
        long offset = page * size;

        Item item = createTestItem(1L, "Test Item", 1000L);
        List<Item> items = List.of(item);

        when(itemRepository.searchItems(eq(searchQuery), eq(sort), eq(size), eq(offset)))
                .thenReturn(Flux.fromIterable(items));
        when(itemRepository.countBySearch(eq(searchQuery))).thenReturn(Mono.just(1L));

        StepVerifier.create(itemService.getItems(searchQuery, sort, page, size))
                .expectNextMatches(result -> {
                    Page<Item> pageResult = result;
                    return pageResult.getContent().size() == 1
                            && pageResult.getTotalElements() == 1L
                            && pageResult.getNumber() == 1
                            && pageResult.getSize() == 5;
                })
                .verifyComplete();
    }

    @Test
    void getItemsWithEmptySearchShouldFallbackToFindAll() {
        Item item = createTestItem(1L, "Test Item", 1000L);
        List<Item> items = List.of(item);

        when(itemRepository.findAllWithSort(eq("NO"), eq(5), eq(0L)))
                .thenReturn(Flux.fromIterable(items));
        when(itemRepository.count()).thenReturn(Mono.just(1L));

        StepVerifier.create(itemService.getItems("   ", "NO", 0, 5))
                .expectNextMatches(page -> page.getContent().size() == 1 && page.getTotalElements() == 1L)
                .verifyComplete();
    }

    @Test
    void getItemsWithPaginationShouldReturnCorrectPage() {
        long offset = 1L * 5; // page=1, size=5 -> offset=5

        Item item = createTestItem(6L, "Page 2 Item", 1000L);
        List<Item> items = List.of(item);

        when(itemRepository.findAllWithSort(eq("NO"), eq(5), eq(offset)))
                .thenReturn(Flux.fromIterable(items));
        when(itemRepository.count()).thenReturn(Mono.just(6L));

        StepVerifier.create(itemService.getItems(null, "NO", 1, 5))
                .expectNextMatches(page ->
                        page.getContent().size() == 1
                                && page.getTotalElements() == 6L
                                && page.getNumber() == 1
                                && page.getTotalPages() == 2
                )
                .verifyComplete();
    }

    private Item createTestItem() {
        return createTestItem(1L, "Тестовый товар", 1000L);
    }

    private Item createTestItem(Long id, String title, Long price) {
        return Item.builder()
                .id(id)
                .title(title)
                .description("Описание " + title)
                .imgPath("/images/test.jpg")
                .price(price)
                .build();
    }
}