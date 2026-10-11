
        package com.example.mock_order_service.controller;

import com.example.mock_order_service.dto.PaymentRequest;
import com.example.mock_order_service.model.FailureMode;
import com.example.mock_order_service.service.MockOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final MockOrderService mockOrderService;

    public OrderController(MockOrderService mockOrderService) {
        this.mockOrderService = mockOrderService;
    }

    @PostMapping("/payments")
    public ResponseEntity<String> processPayment(
            @RequestBody PaymentRequest request) {

        log.info(
                "Received payment request. paymentId={}",
                request.paymentId()
        );

        FailureMode mode =
                mockOrderService.processPayment(request.paymentId());

        if (mode == FailureMode.SUCCESS) {
            log.info(
                    "Returning HTTP 200 for paymentId={}",
                    request.paymentId()
            );

            return ResponseEntity.ok("Payment accepted");
        }

        if (mode == FailureMode.HTTP_500) {
            log.warn(
                    "Returning HTTP 500 for paymentId={}",
                    request.paymentId()
            );

            return ResponseEntity.status(500)
                    .body("Simulated HTTP 500");
        }

        if (mode == FailureMode.HTTP_503) {
            log.warn(
                    "Returning HTTP 503 for paymentId={}",
                    request.paymentId()
            );

            return ResponseEntity.status(503)
                    .body("Simulated HTTP 503");
        }

        if (mode == FailureMode.TIMEOUT) {
            log.warn(
                    "Simulating timeout for paymentId={}",
                    request.paymentId()
            );

            try {
                Thread.sleep(30_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

                return ResponseEntity.internalServerError()
                        .body("Timeout simulation interrupted");
            }

            return ResponseEntity.status(504)
                    .body("Simulated timeout");
        }

        if (mode == FailureMode.CONNECTION_FAILURE) {
            log.warn(
                    "Simulating connection failure for paymentId={}",
                    request.paymentId()
            );

            return ResponseEntity.internalServerError()
                    .body("Simulated connection failure");
        }

        log.error(
                "Unexpected failure mode for paymentId={}",
                request.paymentId()
        );

        return ResponseEntity.internalServerError()
                .body("Unexpected failure mode");
    }
}
