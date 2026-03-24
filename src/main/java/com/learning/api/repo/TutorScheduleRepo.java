package com.learning.api.repo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.learning.api.entity.Booking;
import com.learning.api.entity.TutorSchedule;
import java.util.Optional;

@Repository
public interface TutorScheduleRepo extends JpaRepository<TutorSchedule, Long> {
    // 透過 Spring Data 的命名慣例自動生成查詢：找特定老師、按星期排序、再按小時排序
    List<TutorSchedule> findByTutorIdOrderByWeekdayAscHourAsc(Long tutorId);
    
	// 方法名必須對應實體變數名 user，Spring 會自動提取其 ID 進行查詢
    List<TutorSchedule> findByTutorId(Long tutorId);

    // 精準尋找老師在「星期幾的幾點」的紀錄 (用來檢查要 Update 還是 Insert)
    Optional<TutorSchedule> findByTutorIdAndWeekdayAndHour(Long tutorId, Integer weekday, Integer hour);

    @Query("""
        SELECT b FROM Booking b
        WHERE b.studentId = :studentId
        AND b.slotLocked = true
        AND (
            (b.date > :startDate AND b.date < :endDate)
            OR (b.date = :startDate AND b.hour >= :startHour)
            OR (b.date = :endDate AND b.hour <= :endHour)
        )
        ORDER BY b.date, b.hour
        """)
        List<Booking> findStudentFutureBookings(
            Long studentId,
            LocalDate startDate,
            LocalDate endDate,
            int startHour,
            int endHour
        );
        @Query("""
        SELECT b FROM Booking b
        WHERE b.tutorId = :tutorId
        AND b.slotLocked = true
        AND (
            (b.date > :startDate AND b.date < :endDate)
            OR (b.date = :startDate AND b.hour >= :startHour)
            OR (b.date = :endDate AND b.hour <= :endHour)
        )
        ORDER BY b.date, b.hour
        """)
        List<Booking> findTutorFutureBookings(
            Long tutorId,
            LocalDate startDate,
            LocalDate endDate,
            int startHour,
            int endHour
        );
}
