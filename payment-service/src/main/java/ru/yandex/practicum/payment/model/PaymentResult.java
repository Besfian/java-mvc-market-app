package ru.yandex.practicum.payment.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResult {
    private boolean success;
    private Long orderId;
    private String message;
    private Long newBalance;
}