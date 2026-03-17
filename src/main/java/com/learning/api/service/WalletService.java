package com.learning.api.service;

import com.learning.api.entity.User;
import com.learning.api.entity.WalletLog;
import com.learning.api.repo.TutorRepository;
import com.learning.api.repo.UserRepository;
import com.learning.api.repo.WalletLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final UserRepository userRepo;
    private final TutorRepository tutorRepo;
    private final WalletLogRepository walletLogRepo;

    /**
     * 儲值：增加用戶錢包餘額並記錄 WalletLog (type=1)。
     */
    @Transactional
    public String topUp(Long userId, long amount) {
        if (amount <= 0) return "儲值金額必須大於 0";

        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return "用戶不存在";

        user.setWallet(user.getWallet() + amount);
        userRepo.save(user);

        WalletLog log = new WalletLog();
        log.setUserId(userId);
        log.setTransactionType(1); // 1: 儲值
        log.setAmount(amount);
        log.setRelatedType(3); // 3: Bank
        walletLogRepo.save(log);

        return "success";
    }

    /**
     * 提領：教師從錢包提領金額至銀行帳戶，記錄 WalletLog (type=5)。
     */
    @Transactional
    public String withdraw(Long tutorId, long amount) {
        if (amount <= 0) return "提領金額必須大於 0";

        var tutor = tutorRepo.findById(tutorId).orElse(null);
        if (tutor == null) return "教師資料不存在";
        if (tutor.getBankAccount() == null || tutor.getBankAccount().isBlank()) {
            return "請先設定銀行帳戶資訊";
        }

        User user = userRepo.findById(tutorId).orElse(null);
        if (user == null) return "用戶不存在";
        if (user.getWallet() < amount) return "錢包餘額不足";

        user.setWallet(user.getWallet() - amount);
        userRepo.save(user);

        WalletLog log = new WalletLog();
        log.setUserId(tutorId);
        log.setTransactionType(5); // 5: 提現
        log.setAmount(-amount);
        log.setRelatedType(3); // 3: Bank
        walletLogRepo.save(log);

        return "success";
    }

    /**
     * 增加用戶錢包餘額並記錄 WalletLog（正數金額）。
     */
    @Transactional
    public void credit(Long userId, long amount, int txType, int relatedType, Long relatedId) {
        userRepo.findById(userId).ifPresent(user -> {
            user.setWallet(user.getWallet() + amount);
            userRepo.save(user);

            WalletLog log = new WalletLog();
            log.setUserId(userId);
            log.setTransactionType(txType);
            log.setAmount(amount);
            log.setRelatedType(relatedType);
            log.setRelatedId(relatedId);
            walletLogRepo.save(log);
        });
    }

    /**
     * 扣除用戶錢包餘額並記錄 WalletLog（負數金額）。
     */
    @Transactional
    public void debit(Long userId, long amount, int txType, int relatedType, Long relatedId) {
        User user = userRepo.findById(userId).orElseThrow();
        user.setWallet(user.getWallet() - amount);
        userRepo.save(user);

        WalletLog log = new WalletLog();
        log.setUserId(userId);
        log.setTransactionType(txType);
        log.setAmount(-amount);
        log.setRelatedType(relatedType);
        log.setRelatedId(relatedId);
        walletLogRepo.save(log);
    }

    /**
     * 查詢用戶的交易紀錄。
     */
    public List<WalletLog> getLogs(Long userId) {
        return walletLogRepo.findByUserId(userId);
    }
}
