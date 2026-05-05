package ru.yandex.practicum.payment.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.service.PaymentService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/balance")
    public Mono<Map<String, Long>> getBalance() {
        log.info("GET /api/v1/balance");
        return paymentService.getBalance()
                .map(balance -> Map.of("balance", balance));
    }

    @PostMapping("/payment")
    public Mono<Map<String, Object>> processPayment(@RequestBody Map<String, Long> request) {
        Long orderId = request.get("orderId");
        Long amount = request.get("amount");
        log.info("POST /api/v1/payment - orderId: {}, amount: {}", orderId, amount);
        return paymentService.processPayment(orderId, amount)
                .map(result -> Map.of(
                        "success", result.isSuccess(),
                        "orderId", result.getOrderId(),
                        "message", result.getMessage(),
                        "newBalance", result.getNewBalance()
                ));
    }
}