package com.learning.api.service;

import com.learning.api.dto.*;
import com.learning.api.entity.*;
import com.learning.api.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    @Autowired
    private UserRepo memberRepo;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private OrderRepo orderRepo;

    // bookingReq.getUserId() 僅供開發測試使用，正式版改由登入資訊取得
    public boolean sendBooking(BookingReq bookingReq){

        if (bookingReq == null) return false;

        // check null
        if (bookingReq.getUserId() == null || bookingReq.getCourseId() == null || bookingReq.getLessonCount() == null) return false;

        // lessonCount > 0
        if (bookingReq.getLessonCount() <= 0) return false;

        // member existsById
        if(!memberRepo.existsById(bookingReq.getUserId())) return false;

        // course findById
        Course course = courseRepo.findById(bookingReq.getCourseId()).orElse(null);
        if (course == null) return false;

        // check courseId isActive
        if (!course.isActive()) return false;

        // buildBooking
        Bookings booking = buildBooking(bookingReq, course);
        bookingRepo.save(booking);

        return true;
    }

    private Bookings buildBooking(BookingReq bookingReq, Course course){
        Bookings booking = new Bookings();

        // Create Order
        Order order = new Order();
        order.setUserId(bookingReq.getUserId());
        order.setCourseId(bookingReq.getCourseId());
        
        // Price calculation
        Integer originalPrice = course.getPrice();
        Integer discountPrice = afterDiscPrice(originalPrice, bookingReq.getLessonCount());
        
        order.setUnitPrice(originalPrice);
        order.setDiscountPrice(discountPrice);
        order.setLessonCount(bookingReq.getLessonCount());
        order.setStatus(1);

        // Save order and set its id to booking
        Order savedOrder = orderRepo.save(order);
        booking.setOrderId(savedOrder.getId());
        
        return booking;
    }

    private Integer afterDiscPrice(Integer originalPrice, Integer lessonCount){
        // 95% 10 堂
        if (lessonCount >= 10) return ((int) (originalPrice*0.95));


        // 0%
        return originalPrice;
    }
}