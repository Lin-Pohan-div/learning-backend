package com.learning.api.service;

import com.learning.api.dto.*;
import com.learning.api.entity.*;
import com.learning.api.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    @Autowired
    private MemberRepo memberRepo;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private OrderRepo orderRepo;

    // 之後 JWT 做完 改掉 OrderReq.getUserId() -> 這是前端送 id
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
        if (!course.getActive()) return false;

        // buildOrder
        Order order = buildOrder(bookingReq, course);
        orderRepo.save(order);

        return true;
    }

    private Order buildOrder(BookingReq bookingReq, Course course){
        Order order = new Order();

        order.setUserId(bookingReq.getUserId());
        order.setCourseId(bookingReq.getCourseId());

        // price unitPrice discountPrice
        Integer originalPrice = course.getPrice();
        Integer discount = afterDiscPrice(originalPrice, bookingReq.getLessonCount());

        order.setUnitPrice(originalPrice);
        order.setDiscountPrice(discount);

        // lessonCount
        order.setLessonCount(bookingReq.getLessonCount());
        order.setLessonUsed(0);
        // status first send -> 1
        order.setStatus(1);

        return order;
    }

    private Integer afterDiscPrice(Integer originalPrice, Integer lessonCount){
        // 95% 10 堂
        if (lessonCount >= 10) return ((int) (originalPrice*0.95));


        // 0%
        return originalPrice;
    }
}