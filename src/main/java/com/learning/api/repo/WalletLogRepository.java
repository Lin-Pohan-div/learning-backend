package com.learning.api.repo;
import com.learning.api.entity.WalletLog;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletLogRepository extends JpaRepository<WalletLog, Long> {

    List<WalletLog> findByUserId(Long userId);
}
