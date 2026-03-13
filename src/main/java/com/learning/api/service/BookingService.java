<<<<<<< HEAD
package com.learning.api.service;

import com.learning.api.dto.*;
import com.learning.api.entity.*;
import com.learning.api.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    @Autowired
    private UserRepo userRepo;

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
        if(!userRepo.existsById(bookingReq.getUserId())) return false;

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
=======
//package com.learning.api.service;
//
//import com.learning.api.dto.*;
//import com.learning.api.entity.*;
//import com.learning.api.repo.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
////Test功能 暫時註解
////@Service
//public class BookingService {
//
//    //@Autowired
//    private UserRepo memberRepo;
//
//    //@Autowired
//    private CourseRepo courseRepo;
//
//    //@Autowired
//    private BookingRepo bookingRepo;
//
//    //@Autowired
//    private OrderRepo orderRepo;
//
//    // bookingReq.getUserId() 僅供開發測試使用，正式版改由登入資訊取得
//    public boolean sendBooking(BookingReq bookingReq){
//
//        if (bookingReq == null) return false;
//
//        // check null
//        if (bookingReq.getUserId() == null || bookingReq.getCourseId() == null || bookingReq.getLessonCount() == null) return false;
//
//        // lessonCount > 0
//        if (bookingReq.getLessonCount() <= 0) return false;
//
//        // member existsById
//        if(!memberRepo.existsById(bookingReq.getUserId())) return false;
//
//        // course findById
//        Course course = courseRepo.findById(bookingReq.getCourseId()).orElse(null);
//        if (course == null) return false;
//
//        // check courseId isActive
//        if (!course.isActive()) return false;
//
//        // buildBooking
//        Booking booking = buildBooking(bookingReq, course);
//        bookingRepo.save(booking);
//
//        return true;
//    }
//
//    private Booking buildBooking(BookingReq bookingReq, Course course){
//        Booking booking = new Booking();
//
//        // set & save
//        booking.setOrderId(null);
//        //booking.setCourseId(bookingReq.getCourseId());
//
//        // price unitPrice discountPrice
//        Integer originalPrice = course.getPrice();
//        Integer discount = afterDiscPrice(originalPrice, bookingReq.getLessonCount());
//
//        //booking.setUnitPrice(originalPrice);
//        //booking.setDiscountPrice(discount);
//
//        // lessonCount
//        //booking.setLessonCount(bookingReq.getLessonCount());
//        // status first send -> 1
//        booking.setStatus(1);
//
//        return booking;
//    }
//
//    private Integer afterDiscPrice(Integer originalPrice, Integer lessonCount){
//        // 95% 10 堂
//        if (lessonCount >= 10) return ((int) (originalPrice*0.95));
//
//
//        // 0%
//        return originalPrice;
//    }
//}
>>>>>>> feature/teacher-dashboard
