package com.learning.api.repo;

import com.learning.api.dto.CheckoutReq;
import com.learning.api.entity.Booking;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    Optional<Booking> findByStudentIdAndDateAndHourAndSlotLockedTrue(Long studentId, LocalDate date, Integer hour);

    List<Booking> findByTutorId(Long tutorId);

    @Query("""
        SELECT new com.learning.api.dto.CheckoutReq$Slot(b.date, b.hour)
        FROM Booking b
        WHERE b.studentId = :studentId
        AND b.slotLocked = true
        AND (
            (b.date > :startDate AND b.date < :endDate)
            OR (b.date = :startDate AND b.hour >= :startHour)
            OR (b.date = :endDate AND b.hour <= :endHour)
        )
        ORDER BY b.date, b.hour
        """)
    List<CheckoutReq.Slot> findStudentFutureBookings(
        @Param("studentId") Long studentId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("startHour") int startHour,
        @Param("endHour") int endHour
    );


    @Query("""
        SELECT new com.learning.api.dto.CheckoutReq$Slot(b.date, b.hour)
        FROM Booking b
        WHERE b.tutorId = :tutorId
        AND b.slotLocked = true
        AND (
            (b.date > :startDate AND b.date < :endDate)
            OR (b.date = :startDate AND b.hour >= :startHour)
            OR (b.date = :endDate AND b.hour <= :endHour)
        )
        ORDER BY b.date, b.hour
        """)
    List<CheckoutReq.Slot> findTutorFutureBookings(
        @Param("tutorId") Long tutorId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("startHour") int startHour,
        @Param("endHour") int endHour
    );



    List<Booking> findByStudentId(Long studentId);
    List<Booking> findByStudentIdAndDateOrderByHourAsc(Long studentId, LocalDate date);
    List<Booking> findByOrderId(Long orderId);
    Optional<Booking> findByIdAndStudentId(Long id, Long studentId);
 // 使用 Spring Data JPA 的命名規範自動生成 SQL
    List<Booking> findByStudentIdAndDateGreaterThanEqualOrderByDateAscHourAsc(Long studentId, LocalDate date);

}