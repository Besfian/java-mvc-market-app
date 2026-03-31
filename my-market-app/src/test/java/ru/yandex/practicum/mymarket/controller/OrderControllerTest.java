package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.OrderService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private CartService cartService;

    @Test
    void testGetOrdersShouldReturnOrdersPage() throws Exception {
        OrderItem orderItem = new OrderItem();
        orderItem.setTitle("Тестовый товар");
        orderItem.setPrice(1000L);
        orderItem.setCount(2);

        Order order = new Order();
        order.setId(1L);
        order.setItems(List.of(orderItem));

        when(orderService.getAllOrders()).thenReturn(List.of(order));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    void testGetOrderShouldReturnOrderPage() throws Exception {
        OrderItem orderItem = new OrderItem();
        orderItem.setTitle("Тестовый товар");
        orderItem.setPrice(1000L);
        orderItem.setCount(2);

        Order order = new Order();
        order.setId(1L);
        order.setItems(List.of(orderItem));

        when(orderService.getOrderById(1L)).thenReturn(order);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attributeExists("order", "newOrder"));
    }

    @Test
    void testGetOrderWithNewOrderFlag() throws Exception {
        Order order = new Order();
        order.setId(1L);

        when(orderService.getOrderById(1L)).thenReturn(order);

        mockMvc.perform(get("/orders/1")
                        .param("newOrder", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("newOrder", true));
    }

    @Test
    void testBuyShouldCreateOrderAndRedirect() throws Exception {
        Item item = Item.builder()
                .id(1L)
                .title("Тестовый товар")
                .price(1000L)
                .build();
        item.setCount(2);

        Order order = new Order();
        order.setId(1L);

        when(cartService.getCartItems()).thenReturn(List.of(item));
        when(orderService.createOrder(anyList())).thenReturn(order);
        doNothing().when(cartService).clear();

        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/1?newOrder=true"));
    }

    @Test
    void testBuyEmptyCartShouldRedirectToCart() throws Exception {
        when(cartService.getCartItems()).thenReturn(List.of());

        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"));
    }
}
