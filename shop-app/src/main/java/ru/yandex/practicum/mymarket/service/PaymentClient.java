package ru.yandex.practicum.mymarket.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class PaymentClient {
    private static final Logger log = LoggerFactory.getLogger(PaymentClient.class);
    private final WebClient webClient;

    public PaymentClient(@Value("${payment.service.url:http://localhost:8081}") String paymentServiceUrl) {
        this.webClient = WebClient.builder().baseUrl(paymentServiceUrl).build();
        log.info("PaymentClient initialized with URL: {}", paymentServiceUrl);
    }

    public Mono<Long> getBalance() {
        return webClient.get()
                .uri("/api/v1/balance")
                .retrieve()
                .bodyToMono(BalanceResponse.class)
                .map(BalanceResponse::balance)
                .doOnSuccess(balance -> log.info("Balance received: {}", balance))
                .doOnError(error -> log.error("Error getting balance: {}", error.getMessage()));
    }

    public Mono<PaymentResult> processPayment(Long orderId, Long amount) {
        PaymentRequest request = new PaymentRequest(orderId, amount);
        return webClient.post()
                .uri("/api/v1/payment")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResult.class)
                .doOnSuccess(result -> log.info("Payment result: orderId={}, success={}", orderId, result.success()))
                .doOnError(error -> log.error("Payment error: {}", error.getMessage()));
    }

    record BalanceResponse(Long balance) {}
    record PaymentRequest(Long orderId, Long amount) {}
    record PaymentResult(boolean success, Long orderId, String message, Long newBalance) {}
}
