package com.learning.api.service;

import com.learning.api.dto.OrderDto;
import com.learning.api.dto.booking.BookingReq;
import com.learning.api.entity.Bookings;
import com.learning.api.entity.Course;
import com.learning.api.repo.BookingRepository;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * BookingService 負責預約入口驗證；
 * 實際建立訂單的邏輯（含折扣計算）委派給 OrderService，避免重複實作。
 */
@Service
@RequiredArgsConstructor
public class BookingService {
    private final UserRepository userRepository;
    private final CourseRepo courseRepo;
    private final BookingRepository bookingRepo;

    // bookingReq.getUserId() 僅供開發測試使用，正式版改由登入資訊取得
    public boolean sendBooking(BookingReq bookingReq) {
        if (bookingReq == null) return false;

        OrderDto.Req req = new OrderDto.Req();
        req.setUserId(bookingReq.getUserId());
        req.setCourseId(bookingReq.getCourseId());
        req.setLessonCount(bookingReq.getLessonCount());

        // lessonCount > 0
        if (bookingReq.getLessonCount() <= 0) return false;

        // member existsById
        if(!userRepository.existsById(bookingReq.getUserId())) return false;

        // course findById
        Course course = courseRepo.findById(bookingReq.getCourseId()).orElse(null);
        if (course == null) return false;

        // check courseId isActive
//        if (!course.isActive()) return false;

        // buildBooking
        Bookings booking = buildBooking(bookingReq, course);
        bookingRepo.save(booking);

        return true;
    }

    private Bookings buildBooking(BookingReq req, Course course) {
        Bookings booking = new Bookings();
        booking.setTutorId(course.getTutorId());
        booking.setStudentId(req.getUserId());
        booking.setDate(req.getDate());
        booking.setHour(req.getHour());
        booking.setStatus((byte) 0);
        return booking;
    }
}
