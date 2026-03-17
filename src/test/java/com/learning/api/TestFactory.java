package com.learning.api;

import com.learning.api.dto.OrderDto;
import com.learning.api.dto.booking.BookingReq;
import com.learning.api.dto.CheckoutReq;
import com.learning.api.entity.Course;
import com.learning.api.entity.Order;
import com.learning.api.entity.User;

import java.time.LocalDate;
import java.util.List;

/**
 * Shared factory methods for test data across all test classes.
 */
public class TestFactory {

    public static Course makeCourse(Long id, Long tutorId, int price, boolean active) {
        Course course = new Course();
        course.setId(id);
        course.setTutorId(tutorId);
        course.setName("Test Course");
        course.setSubject(21);
        course.setDescription("Test Description");
        course.setPrice(price);
        course.setActive(active);
        return course;
    }

    public static Course makeCourse(Long id, Long tutorId) {
        return makeCourse(id, tutorId, 500, true);
    }

    public static Order makeOrder(Long id, Long userId, Long courseId, int status) {
        Order order = new Order();
        order.setId(id);
        order.setUserId(userId);
        order.setCourseId(courseId);
        order.setUnitPrice(500);
        order.setDiscountPrice(500);
        order.setLessonCount(10);
        order.setLessonUsed(0);
        order.setStatus(status);
        return order;
    }

    public static OrderDto.Req makeOrderReq(Long userId, Long courseId, int lessonCount) {
        OrderDto.Req req = new OrderDto.Req();
        req.setUserId(userId);
        req.setCourseId(courseId);
        req.setLessonCount(lessonCount);
        return req;
    }

    public static OrderDto.Resp makeOrderResp(Long id, Long userId, Long courseId, int status) {
        OrderDto.Resp resp = new OrderDto.Resp();
        resp.setId(id);
        resp.setUserId(userId);
        resp.setCourseId(courseId);
        resp.setUnitPrice(500);
        resp.setDiscountPrice(500);
        resp.setLessonCount(5);
        resp.setLessonUsed(0);
        resp.setStatus(status);
        return resp;
    }

    public static User makeUser(Long id, long wallet) {
        User user = new User();
        user.setId(id);
        user.setName("Test User");
        user.setEmail("user" + id + "@test.com");
        user.setPassword("hashedpw");
        user.setWallet(wallet);
        return user;
    }

    public static BookingReq makeBookingReq(Long userId, Long courseId, int lessonCount) {
        BookingReq req = new BookingReq();
        req.setUserId(userId);
        req.setCourseId(courseId);
        req.setLessonCount(lessonCount);
        req.setDate(LocalDate.of(2026, 3, 20));
        req.setHour(10);
        return req;
    }

    public static CheckoutReq makeCheckoutReq(Long studentId, Long courseId, LocalDate date, int hour) {
        CheckoutReq req = new CheckoutReq();
        req.setStudentId(studentId);
        req.setCourseId(courseId);
        CheckoutReq.Slot slot = new CheckoutReq.Slot();
        slot.setDate(date);
        slot.setHour(hour);
        req.setSelectedSlots(List.of(slot));
        return req;
    }
}
