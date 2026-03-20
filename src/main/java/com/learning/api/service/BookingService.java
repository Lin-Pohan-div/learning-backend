package com.learning.api.service;

import com.learning.api.dto.BookingReq;
import com.learning.api.entity.Booking;
import com.learning.api.entity.Course;
import com.learning.api.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Service
public class BookingService {

    @Autowired private CourseRepo courseRepo;
    @Autowired private BookingRepo bookingRepo;
    @Autowired private OrderRepository orderRepo;

    // 什麼都不做，直接回傳 200 OK 或空內容
    @GetMapping("favicon.ico")
    @ResponseBody
    public void disableFavicon() {
    }
    public boolean sendBooking(BookingReq req){
        if (req == null || req.getUserId() == null || req.getCourseId() == null) return false;

        // 👉 防呆：DB 規定一定要有 order_id
        if (req.getOrderId() == null) {
            throw new IllegalArgumentException("預約必須關聯一筆有效訂單 (order_id 不可為空)");
        }

        Course course = courseRepo.findById(req.getCourseId()).orElse(null);
        if (course == null) return false;

        Booking booking = new Booking();
        booking.setStudentId(req.getUserId());
        booking.setTutorId(course.getTutor().getId());
        booking.setOrderId(req.getOrderId());
        booking.setDate(req.getDate());
        booking.setHour(req.getHour());
        booking.setStatus(1);
        booking.setSlotLocked(true); // 預約成功直接鎖定

        bookingRepo.save(booking);
        return true;
    }


    public List<Booking> getTutorBookings(Long tutorId) {
        return bookingRepo.findByTutorId(tutorId);
    }
}
