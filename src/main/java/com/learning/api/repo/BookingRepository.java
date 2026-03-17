package com.learning.api.repo;

import com.learning.api.entity.Bookings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Bookings, Long> {

    List<Bookings> findByOrderIdIn(List<Long> orderIds);

    List<Bookings> findByStudentId(Long studentId);

    /**
     * 防超賣核心查詢：
     * 根據 老師ID、日期、小時，檢查是否已經存在預約紀錄。
     */
    Optional<Bookings> findByTutorIdAndDateAndHour(Long tutorId, LocalDate date, Integer hour);

    /**
     * 防超賣查詢（排除已取消）：
     * 已取消 (status=3) 的預約不應阻擋同時段的重新預約。
     */
    Optional<Bookings> findByTutorIdAndDateAndHourAndStatusNot(Long tutorId, LocalDate date, Integer hour, Byte status);
}
