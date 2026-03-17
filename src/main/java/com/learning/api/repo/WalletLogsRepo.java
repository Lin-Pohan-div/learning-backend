package com.learning.api.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.learning.api.entity.WalletLog;

public interface WalletLogsRepo extends JpaRepository<WalletLog, Long> {
    // 按時間倒序查詢使用者的所有錢包變動
    List<WalletLog> findByUserIdOrderByCreatedAtDesc(Long userId);
}
