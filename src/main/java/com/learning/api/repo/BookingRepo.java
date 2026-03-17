package com.learning.api.repo;

import com.learning.api.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepo extends JpaRepository<Booking, Long> {

    /**
     * 防超賣核心查詢：
     * 根據 老師ID、日期、小時，檢查是否已經存在「且仍在鎖定中(slotLocked=true)」的預約紀錄。
     * 若被取消 (slotLocked=null 或 false)，則不列入計算，允許新學生預約。
     */
    Optional<Booking> findByTutorIdAndDateAndHourAndSlotLockedTrue(Long tutorId, LocalDate date, Integer hour);

    List<Booking> findByTutorId(Long tutorId);
}