package com.learning.api.service;

import com.learning.api.entity.Order;
import com.learning.api.entity.User;
import com.learning.api.entity.WalletLog;
import com.learning.api.repo.OrderRepository;
import com.learning.api.repo.UserRepository;
import com.learning.api.repo.WalletLogRepository;

import jakarta.transaction.Transactional;

public class PaymentService {
    private OrderRepository orderRepository;
    private UserRepository userRepository;
    private WalletLogRepository walletLogRepository;

    public PaymentService(OrderRepository orderRepo, UserRepository userRepo, WalletLogRepository walletLogRepo) {
        this.orderRepository = orderRepo;
        this.userRepository = userRepo;
        this.walletLogRepository = walletLogRepo;
    }

    @Transactional
    public boolean pay(Long orderId){
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || order.getStatus()!=1)return false;// 只能付 pending 的單

        long totalCost = (long)order.getDiscountPrice()*order.getLessonCount();
        User user = userRepository.findById(order.getUserId()).orElse(null);
        if (user == null || user.getWallet() < totalCost) return false;// 餘額不足

        user.setWallet((user.getWallet()-totalCost));
        userRepository.save(user);

        WalletLog log =new WalletLog();
        log.setUserId(user.getId());
        log.setTransactionType(2);
        log.setAmount(-totalCost);
        log.setRelatedType(1);
        log.setRelatedId(orderId);
        walletLogRepository.save(log);

        order.setStatus(2);
        orderRepository.save(order);

        return true;
    }
}
