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

    /**
     * 1: 儲值
     * 2: 購課
     * 3: 授課收入
     * 4: 退款
     * 5: 提現
     * 6: 平台初始贈點
     */
    @Column(name = "transaction_type", nullable = false)
    private Integer transactionType;

    /**
     * 正數增加 / 負數減少
     */
    @Column(nullable = false)
    private Long amount;

    /**
     * 1 booking
     * 2 lesson
     * 3 bank
     */
    @Column(name = "related_type",  nullable = false )
    private Integer relatedType;

    @Column(name = "related_id")
    private Long relatedId;

    @Column(name = "merchant_trade_no", unique = true, length = 100)
    private String merchantTradeNo;

    /**
     * DB 自動產生
     */
    @Column(name = "created_at")
    private Instant createdAt;

}