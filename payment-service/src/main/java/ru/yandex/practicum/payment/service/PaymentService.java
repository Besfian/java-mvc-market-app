package ru.yandex.practicum.payment.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.model.PaymentResult;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final AtomicLong balance;
    private final long initialBalance;

    public PaymentService(@Value("${payment.initial-balance:100000}") long initialBalance) {
        this.initialBalance = initialBalance;
        this.balance = new AtomicLong(initialBalance);
        log.info("Payment service initialized with balance: {}", initialBalance);
    }

    public Mono<Long> getBalance() {
        return Mono.fromSupplier(() -> {
            long currentBalance = balance.get();
            log.info("Current balance: {}", currentBalance);
            return currentBalance;
        });
    }

    public Mono<PaymentResult> processPayment(Long orderId, Long amount) {
        return Mono.fromSupplier(() -> {
            long currentBalance = balance.get();
            if (currentBalance < amount) {
                log.warn("Insufficient funds: balance={}, required={}", currentBalance, amount);
                return new PaymentResult(false, orderId, "Insufficient funds", currentBalance);
            }
            long newBalance = balance.addAndGet(-amount);
            log.info("Payment successful: orderId={}, amount={}, newBalance={}", orderId, amount, newBalance);
            return new PaymentResult(true, orderId, "Payment successful", newBalance);
        });
    }
}
