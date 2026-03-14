package com.learning.api.service;


import com.learning.api.dto.booking.BookingReq;
import com.learning.api.entity.Course;
import com.learning.api.entity.Order;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.OrderRepository;
import com.learning.api.repo.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private OrderRepository orderRepo;

    // bookingReq.getUserId() 僅供開發測試使用，正式版改由登入資訊取得
    public boolean sendBooking(BookingReq bookingReq){

        if (bookingReq == null) return false;

        // check null
        if (bookingReq.getUserId() == null || bookingReq.getCourseId() == null || bookingReq.getLessonCount() == null) return false;

        // lessonCount > 0
        if (bookingReq.getLessonCount() <= 0) return false;

        // member existsById
        if(!userRepo.existsById(bookingReq.getUserId())) return false;

        // course findById
        Course course = courseRepo.findById(bookingReq.getCourseId()).orElse(null);
        if (course == null) return false;

        // check courseId isActive
        if (!Boolean.TRUE.equals(course.getActive())) return false;

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

        // status first send -> 1 (pending)
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
