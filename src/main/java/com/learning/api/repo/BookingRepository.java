package com.learning.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.learning.api.entity.Bookings;

import java.util.List;

public interface BookingRepository extends JpaRepository<Bookings, Long> {
    List<Bookings> findByOrderIdIn(List<Long> orderIds);
}
