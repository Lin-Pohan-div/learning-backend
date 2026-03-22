package com.learning.api.controller;

import java.util.ArrayList;
import java.util.List;

import com.learning.api.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.learning.api.entity.Booking;
import com.learning.api.entity.Review;
import com.learning.api.entity.WalletLog;
import com.learning.api.service.BookingService;
import com.learning.api.service.WalletLogsService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.stream.Collectors;

import com.learning.api.dto.ReviewDTO;
import com.learning.api.dto.TutorProfileDTO;
import com.learning.api.dto.TutorScheduleDTO;
import com.learning.api.service.TutorService;

@RestController
@RequestMapping("api/tutor")
@CrossOrigin(origins = "http://localhost:5173")
public class TutorController {

    @Autowired
    private TutorService tutorService;
    @Autowired private BookingService bookingService;
    @Autowired private WalletLogsService walletLogsService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getTutorProfile(
            @PathVariable Long id,
            @RequestParam(required = false) Long courseId) {

        // 1. 取得老師核心資料
        Tutor tutor = tutorService.findTutorById(id);
        if (tutor == null) {
            return ResponseEntity.notFound().build();
        }

        // 2. 取得課表與課程列表
        List<TutorSchedule> schedules = tutorService.findSchedulesByTutorId(id);
        List<Course> courses = tutorService.findCoursesByTutorId(id);

        // 3. 決定顯示哪堂課的評價
        Course selectedCourse = null;
        if (courseId != null) {
            selectedCourse = tutorService.findCourseById(courseId);
        } else if (!courses.isEmpty()) {
            selectedCourse = courses.get(0);
        }

        List<Review> reviews = (selectedCourse != null)
                ? tutorService.findReviewsByCourseId(selectedCourse.getId())
                : new ArrayList<>();

        // 4. 計算平均評分
        double avgRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        // 5. 組裝 DTO
        TutorProfileDTO dto = new TutorProfileDTO();

        // 基本資料
        dto.setName(tutor.getUser().getName());
        dto.setHeadline(tutor.getTitle());
        dto.setAvatar(tutor.getAvatar());
        dto.setIntro(tutor.getIntro());

        // 證照
        dto.setCertificate1(tutor.getCertificate1());
        dto.setCertificateName1(tutor.getCertificateName1());
        dto.setCertificate2(tutor.getCertificate2());
        dto.setCertificateName2(tutor.getCertificateName2());

        // 影片
        dto.setVideoUrl1(tutor.getVideoUrl1());
        dto.setVideoUrl2(tutor.getVideoUrl2());

        // 課表：Entity → DTO，避免 LAZY 與循環引用
        List<TutorScheduleDTO> scheduleDTOs = schedules.stream()
                .map(s -> new TutorScheduleDTO(s.getWeekday(), s.getHour()))
                .toList();
        dto.setSchedules(scheduleDTOs);

        // 評價：Entity → DTO，避免 LAZY 與敏感資料外洩
        List<ReviewDTO> reviewDTOs = reviews.stream()
                .map(r -> new ReviewDTO(
                        r.getStudent().getName(),
                        r.getRating(),
                        r.getComment(),
                        r.getUpdatedAt()))
                .toList();
        dto.setReviews(reviewDTOs);

        dto.setAverageRating(Double.parseDouble(String.format("%.1f", avgRating)));

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<?> getTutorStats(@PathVariable Long id) {

        // 1. 取得老師所有預約
        List<Booking> bookings = bookingService.getTutorBookings(id);

        // 2. 取得老師所有課程（用來查單價）
        List<Course> courses = tutorService.findCoursesByTutorId(id);
        Map<Long, Integer> coursePriceMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, Course::getPrice));

        // 3. 本週範圍
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);

        // 4. 本月範圍
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        // 5. 計算本週堂數（status=1 排程中 或 status=2 已完成，且 slotLocked=true）
        long weekCount = bookings.stream()
                .filter(b -> b.getSlotLocked() != null && b.getSlotLocked())
                .filter(b -> !b.getDate().isBefore(weekStart) && !b.getDate().isAfter(weekEnd))
                .count();

        // 6. 計算本月收入（只算 status=2 已完成的）
        // 需要從 Order 查單價，這裡用 bookingService 取得對應 order
        // 簡化做法：從 wallet_logs 查 transactionType=3（授課收入）的本月金額
        List<WalletLog> walletLogs = walletLogsService.getLogsByUserId(id);

        long monthIncome = walletLogs.stream()
                .filter(w -> w.getTransactionType() == 3) // 3 = 授課收入
                .filter(w -> {
                    LocalDate logDate = w.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
                    return !logDate.isBefore(monthStart) && !logDate.isAfter(monthEnd);
                })
                .mapToLong(WalletLog::getAmount)
                .sum();

        // 7. 取得平均評分（複用現有邏輯）
        Tutor tutor = tutorService.findTutorById(id);
        List<Course> allCourses = tutorService.findCoursesByTutorId(id);
        double avgRating = 0.0;
        if (!allCourses.isEmpty()) {

            List<Review> allReviews = allCourses.stream()
                    .flatMap(c -> tutorService.findReviewsByCourseId(c.getId()).stream())
                    .collect(Collectors.toList());
            avgRating = allReviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
        }

        // 8. 今日課程（用來顯示歡迎卡片的今日堂數）
        long todayCount = bookings.stream()
                .filter(b -> b.getSlotLocked() != null && b.getSlotLocked())
                .filter(b -> b.getDate().equals(today))
                .count();

        // 9. 組裝回傳
        return ResponseEntity.ok(Map.of(
                "weekCount",   weekCount,
                "monthIncome", monthIncome,
                "avgRating",   Double.parseDouble(String.format("%.1f", avgRating)),
                "todayCount",  todayCount
        ));
    }
}