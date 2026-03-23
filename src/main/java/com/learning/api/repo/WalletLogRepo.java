package com.learning.api.repo;

import com.learning.api.entity.WalletLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletLogRepo extends JpaRepository<WalletLog, Long> {
}