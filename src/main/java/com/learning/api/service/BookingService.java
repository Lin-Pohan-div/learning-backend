package com.learning.api.service;

import com.learning.api.dto.BookingReq;
import com.learning.api.entity.Booking;
import com.learning.api.entity.Course;
import com.learning.api.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {

    @Autowired private UserRepo memberRepo;
    @Autowired private CourseRepo courseRepo;
    @Autowired private BookingRepo bookingRepo;
    @Autowired private OrderRepo orderRepo;

    /**
     * 建立預約紀錄 (已修正紅線)
     */
    public boolean sendBooking(BookingReq req){
        if (req == null || req.getUserId() == null || req.getCourseId() == null) return false;

        // 檢查課程是否存在
        Course course = courseRepo.findById(req.getCourseId()).orElse(null);
        if (course == null) return false;

        // 建立預約實體
        Booking booking = new Booking();

        // 對齊你的 Booking.java 欄位
        booking.setStudentId(req.getUserId());
        booking.setTutorId(course.getTutorId());
        booking.setOrderId(req.getOrderId()); // 如果是直接預約，這會由 CheckoutService 傳入
        booking.setDate(req.getDate());
        booking.setHour(req.getHour());
        booking.setStatus(1); // 1: scheduled
        booking.setSlotLocked(false);

        // 以下是原本報錯的折扣邏輯，因為 Entity 沒欄位，我們先不存
        /*
        Integer originalPrice = course.getPrice();
        Integer discount = afterDiscPrice(originalPrice, req.getLessonCount());
        // booking.setUnitPrice(originalPrice);    // Entity 沒這欄位 -> 註解
        // booking.setDiscountPrice(discount);    // Entity 沒這欄位 -> 註解
        // booking.setLessonCount(req.getLessonCount()); // Entity 沒這欄位 -> 註解
        */

        bookingRepo.save(booking);
        return true;
    }

    /**
     * 計算折扣 (保留邏輯供未來參考)
     */
    private Integer afterDiscPrice(Integer originalPrice, Integer lessonCount){
        if (lessonCount != null && lessonCount >= 10) return (int) (originalPrice * 0.95);
        return originalPrice;
    }

    /**
     * 取得老師的所有預約 (供 test-schedule.html 顯示紅色格子)
     */
    public List<Booking> getTutorBookings(Long tutorId) {
        return bookingRepo.findByTutorId(tutorId);
    }
}
