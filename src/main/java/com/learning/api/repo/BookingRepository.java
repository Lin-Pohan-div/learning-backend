package com.learning.api.repo;

import com.learning.api.entity.Bookings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Bookings, Long> {

    List<Bookings> findByOrderIdIn(List<Long> orderIds);

    /**
     * 防超賣核心查詢：
     * 根據 老師ID、日期、小時，檢查是否已經存在預約紀錄。
     */
    Optional<Bookings> findByTutorIdAndDateAndHour(Long tutorId, LocalDate date, Integer hour);
}
