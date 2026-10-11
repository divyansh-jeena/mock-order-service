
        package com.example.mock_order_service.repository;

import com.example.mock_order_service.model.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderPaymentRepository
        extends JpaRepository<OrderPayment, Long> {

    Optional<OrderPayment> findByPaymentId(String paymentId);

    @Modifying
    @Query(value = """
        INSERT INTO order_payments
            (payment_id, status, response_message, created_at)
        VALUES
            (:paymentId, 'SUCCESS', :message, CURRENT_TIMESTAMP)
        ON CONFLICT (payment_id) DO NOTHING
        """, nativeQuery = true)
    int insertSuccessfulIfAbsent(
            @Param("paymentId") String paymentId,
            @Param("message") String message
    );


}
