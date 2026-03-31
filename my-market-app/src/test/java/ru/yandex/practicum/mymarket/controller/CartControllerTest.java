package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.service.CartService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Test
    void testGetCartShouldReturnCartPage() throws Exception {
        Item item = Item.builder()
                .id(1L)
                .title("Тестовый товар")
                .description("Описание")
                .imgPath("/images/test.jpg")
                .price(1000L)
                .build();
        item.setCount(2);

        when(cartService.getCartItems()).thenReturn(List.of(item));
        when(cartService.getTotalSum()).thenReturn(2000L);

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("items", "total"))
                .andExpect(model().attribute("total", 2000L));
    }

    @Test
    void testGetCartEmptyCartShouldReturnEmptyCartPage() throws Exception {
        when(cartService.getCartItems()).thenReturn(List.of());
        when(cartService.getTotalSum()).thenReturn(0L);

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", List.of()))
                .andExpect(model().attribute("total", 0L));
    }

    @Test
    void testUpdateCartWithPlusActionShouldIncreaseItem() throws Exception {
        // given
        Item item = Item.builder()
                .id(1L)
                .title("Тестовый товар")
                .price(1000L)
                .build();
        item.setCount(2);

        doNothing().when(cartService).increaseItem(1L);
        when(cartService.getCartItems()).thenReturn(List.of(item));
        when(cartService.getTotalSum()).thenReturn(2000L);

        // when & then
        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "PLUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));
    }

    @Test
    void testUpdateCartWithMinusActionShouldDecreaseItem() throws Exception {
        Item item = Item.builder()
                .id(1L)
                .title("Тестовый товар")
                .price(1000L)
                .build();
        item.setCount(1);

        doNothing().when(cartService).decreaseItem(1L);
        when(cartService.getCartItems()).thenReturn(List.of(item));
        when(cartService.getTotalSum()).thenReturn(1000L);

        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "MINUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));
    }

    @Test
    void testUpdateCartWithDeleteActionShouldRemoveItem() throws Exception {
        doNothing().when(cartService).removeItem(1L);
        when(cartService.getCartItems()).thenReturn(List.of());
        when(cartService.getTotalSum()).thenReturn(0L);

        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));
    }
}