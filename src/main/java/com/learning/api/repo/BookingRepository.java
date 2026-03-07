package com.learning.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.learning.api.entity.Booking;


public interface BookingRepository extends JpaRepository<Booking, Long> {
}
