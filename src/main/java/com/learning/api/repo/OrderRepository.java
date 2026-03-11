package com.learning.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.learning.api.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
