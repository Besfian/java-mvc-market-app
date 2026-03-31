package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @MockBean
    private CartService cartService;

    @Test
    void testGetItemsShouldReturnItemsPage() throws Exception {
        Item item = Item.builder()
                .id(1L)
                .title("Тестовый товар")
                .description("Описание")
                .imgPath("/images/test.jpg")
                .price(1000L)
                .build();

        Page<Item> page = new PageImpl<>(List.of(item), PageRequest.of(0, 5), 1);

        when(itemService.getItems(null, "NO", 0, 5)).thenReturn(page);
        when(cartService.getItemCount(1L)).thenReturn(0);

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attributeExists("items", "search", "sort", "paging"));
    }

    @Test
    void testGetItemsWithSearchAndSort() throws Exception {
        Page<Item> page = new PageImpl<>(List.of(), PageRequest.of(0, 5), 0);
        when(itemService.getItems("тест", "ALPHA", 0, 5)).thenReturn(page);

        mockMvc.perform(get("/items")
                        .param("search", "тест")
                        .param("sort", "ALPHA"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attribute("search", "тест"))
                .andExpect(model().attribute("sort", "ALPHA"));
    }

    @Test
    void testGetItemShouldReturnItemPage() throws Exception {
        Item item = Item.builder()
                .id(1L)
                .title("Тестовый товар")
                .description("Описание")
                .imgPath("/images/test.jpg")
                .price(1000L)
                .build();

        when(itemService.getItemById(1L)).thenReturn(item);
        when(cartService.getItemCount(1L)).thenReturn(2);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attributeExists("item"))
                .andExpect(model().attribute("item", item));
    }

    @Test
    void testUpdateCartFromItemsShouldRedirect() throws Exception {
        doNothing().when(cartService).increaseItem(1L);

        mockMvc.perform(post("/items")
                        .param("id", "1")
                        .param("action", "PLUS")
                        .param("search", "test")
                        .param("sort", "NO")
                        .param("pageNumber", "1")
                        .param("pageSize", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?search=test&sort=NO&pageNumber=1&pageSize=5"));
    }

    @Test
    void testUpdateCartFromItemShouldReturnItemPage() throws Exception {
        Item item = Item.builder()
                .id(1L)
                .title("Тестовый товар")
                .description("Описание")
                .imgPath("/images/test.jpg")
                .price(1000L)
                .build();

        when(itemService.getItemById(1L)).thenReturn(item);
        when(cartService.getItemCount(1L)).thenReturn(1);
        doNothing().when(cartService).increaseItem(1L);

        mockMvc.perform(post("/items/1")
                        .param("action", "PLUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attributeExists("item"));
    }
}