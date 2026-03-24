package com.learning.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "wallet_logs")
@Getter
@Setter
public class WalletLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "transaction_type", nullable = false)
    private Integer transactionType;

    @Column(nullable = false)
    private Long amount;

    @Column(name = "related_type")
    private Integer relatedType; // DB 裡這裡沒有寫 NOT NULL

    @Column(name = "related_id")
    private Long relatedId;

    @Column(name = "merchant_trade_no", unique = true, length = 100)
    private String merchantTradeNo;

    // 👉 修正：補上漏掉的 d_type 和 payment_amount
    @Column(name = "d_type")
    private Integer dType;

    @Column(name = "payment_amount")
    private Integer paymentAmount;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
