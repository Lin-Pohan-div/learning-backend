package com.learning.api.service;

import com.learning.api.dto.OrderDto;
import com.learning.api.entity.Course;
import com.learning.api.entity.Order;
import com.learning.api.entity.User;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.OrderRepository;
import com.learning.api.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.learning.api.enums.UserRole;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired private UserRepository userRepo;
    @Autowired private CourseRepo courseRepo;
    @Autowired private OrderRepository orderRepo;

    private Long userId;
    private Long courseId;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setName("Order Test User");
        user.setEmail("ordersvc@example.com");
        user.setPassword("hashed");
        user.setRole(UserRole.STUDENT);
        user.setWallet(0L);
        user = userRepo.save(user);
        userId = user.getId();

        Course course = new Course();
        course.setTutorId(userId);
        course.setName("Order Test Course");
        course.setSubject(11);
        course.setPrice(500);
        course.setActive(true);
        course = courseRepo.save(course);
        courseId = course.getId();
    }

    private Long createPendingOrder(int lessonCount) {
        OrderDto.Req req = new OrderDto.Req();
        req.setUserId(userId);
        req.setCourseId(courseId);
        req.setLessonCount(lessonCount);
        orderService.createOrder(req);
        return orderRepo.findByUserId(userId).get(0).getId();
    }

    // ===================== createOrder =====================

    @Test
    void createOrder_validRequest_returnsTrue() {
        OrderDto.Req req = new OrderDto.Req();
        req.setUserId(userId);
        req.setCourseId(courseId);
        req.setLessonCount(5);

        assertTrue(orderService.createOrder(req));
    }

    @Test
    void createOrder_nullUserId_returnsFalse() {
        OrderDto.Req req = new OrderDto.Req();
        req.setUserId(null);
        req.setCourseId(courseId);
        req.setLessonCount(5);

        assertFalse(orderService.createOrder(req));
    }

    @Test
    void createOrder_zeroLessonCount_returnsFalse() {
        OrderDto.Req req = new OrderDto.Req();
        req.setUserId(userId);
        req.setCourseId(courseId);
        req.setLessonCount(0);

        assertFalse(orderService.createOrder(req));
    }

    @Test
    void createOrder_10Lessons_applies95PercentDiscount() {
        OrderDto.Req req = new OrderDto.Req();
        req.setUserId(userId);
        req.setCourseId(courseId);
        req.setLessonCount(10);
        orderService.createOrder(req);

        Order order = orderRepo.findByUserId(userId).get(0);
        assertEquals(500, order.getUnitPrice());
        assertEquals((int)(500 * 0.95), order.getDiscountPrice());
    }

    // ===================== updateOrder =====================

    @Test
    void updateOrder_validRequest_returnsTrue() {
        Long orderId = createPendingOrder(5);

        OrderDto.UpdateReq req = new OrderDto.UpdateReq();
        req.setLessonCount(8);

        assertTrue(orderService.updateOrder(orderId, req));
    }

    @Test
    void updateOrder_completeOrder_returnsFalse() {
        Long orderId = createPendingOrder(5);
        Order order = orderRepo.findById(orderId).orElseThrow();
        order.setStatus(3);
        orderRepo.save(order);

        OrderDto.UpdateReq req = new OrderDto.UpdateReq();
        req.setLessonCount(8);

        assertFalse(orderService.updateOrder(orderId, req));
    }

    // ===================== updateStatus =====================

    @Test
    void updateStatus_forward_returnsTrue() {
        Long orderId = createPendingOrder(5); // status=1

        OrderDto.StatusReq req = new OrderDto.StatusReq();
        req.setStatus(2);

        assertTrue(orderService.updateStatus(orderId, req));
    }

    @Test
    void updateStatus_backward_returnsFalse() {
        Long orderId = createPendingOrder(5); // status=1
        Order order = orderRepo.findById(orderId).orElseThrow();
        order.setStatus(2);
        orderRepo.save(order);

        OrderDto.StatusReq req = new OrderDto.StatusReq();
        req.setStatus(1); // 退回到 1

        assertFalse(orderService.updateStatus(orderId, req));
    }

    // ===================== cancelOrder =====================

    @Test
    void cancelOrder_pendingOrder_returnsTrue() {
        Long orderId = createPendingOrder(5); // status=1
        assertTrue(orderService.cancelOrder(orderId));
        assertFalse(orderRepo.existsById(orderId));
    }

    @Test
    void cancelOrder_paidOrder_returnsFalse() {
        Long orderId = createPendingOrder(5);
        Order order = orderRepo.findById(orderId).orElseThrow();
        order.setStatus(2);
        orderRepo.save(order);

        assertFalse(orderService.cancelOrder(orderId));
    }

    // ===================== payOrder =====================

    @Test
    void payOrder_pendingOrder_returnsTrue() {
        Long orderId = createPendingOrder(5); // status=1
        assertTrue(orderService.payOrder(orderId));

        Order order = orderRepo.findById(orderId).orElseThrow();
        assertEquals(2, order.getStatus());
    }

    @Test
    void payOrder_paidOrder_returnsFalse() {
        Long orderId = createPendingOrder(5);
        Order order = orderRepo.findById(orderId).orElseThrow();
        order.setStatus(2);
        orderRepo.save(order);

        assertFalse(orderService.payOrder(orderId));
    }
}
