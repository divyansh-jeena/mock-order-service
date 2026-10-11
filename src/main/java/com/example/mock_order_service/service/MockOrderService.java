
        package com.example.mock_order_service.service;

import com.example.mock_order_service.model.FailureMode;
import com.example.mock_order_service.model.OrderPayment;
import com.example.mock_order_service.repository.OrderPaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MockOrderService {

    private final OrderPaymentRepository orderPaymentRepository;

    private final Map<String, FailureMode> paymentModes =
            new HashMap<>();

    private final Map<String, Integer> requestCounts =
            new ConcurrentHashMap<>();

    public MockOrderService(
            OrderPaymentRepository orderPaymentRepository) {

        this.orderPaymentRepository = orderPaymentRepository;

        paymentModes.put("PAY-SUCCESS", FailureMode.SUCCESS);
        paymentModes.put("PAY-500", FailureMode.HTTP_500);
        paymentModes.put("PAY-503", FailureMode.HTTP_503);
        paymentModes.put("PAY-TIMEOUT", FailureMode.TIMEOUT);
        paymentModes.put(
                "PAY-CONNECTION",
                FailureMode.CONNECTION_FAILURE
        );
        paymentModes.put("PAY-007", FailureMode.HTTP_500);
    }

    @Transactional
    public FailureMode processPayment(String paymentId) {

        // 1. Check whether this payment already succeeded.
        OrderPayment existing = orderPaymentRepository
                .findByPaymentId(paymentId)
                .orElse(null);

        if (existing != null
                && "SUCCESS".equals(existing.getStatus())) {

            log.info(
                    "Payment already succeeded. Returning saved result: {}",
                    paymentId
            );

            return FailureMode.SUCCESS;
        }

        // 2. Count requests for failure simulation.
        int attempt = requestCounts.merge(
                paymentId, 1, Integer::sum
        );

        log.info(
                "Received payment request. paymentId={}, attempt={}",
                paymentId, attempt
        );

        // 3. PAY-007 fails twice and succeeds on attempt 3.
        FailureMode mode;

        if ("PAY-007".equals(paymentId)) {
            mode = attempt >= 3
                    ? FailureMode.SUCCESS
                    : FailureMode.HTTP_500;
        } else {
            mode = paymentModes.getOrDefault(
                    paymentId, FailureMode.SUCCESS
            );
        }

        log.info(
                "Selected response mode. paymentId={}, mode={}",
                paymentId, mode
        );

        if (mode == FailureMode.SUCCESS) {

            int inserted =
                    orderPaymentRepository.insertSuccessfulIfAbsent(
                            paymentId,
                            "Payment accepted"
                    );

            if (inserted == 1) {
                log.info(
                        "Successful payment saved: {}",
                        paymentId
                );
            } else {
                log.info(
                        "Payment already exists; insert skipped: {}",
                        paymentId
                );
            }
        }

        return mode;
    }
}
