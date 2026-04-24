package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    private Item createTestItem() {
        return Item.builder()
                .id(1L)
                .title("Тестовый товар")
                .description("Описание")
                .imgPath("/images/test.jpg")
                .price(1000L)
                .build();
    }
}