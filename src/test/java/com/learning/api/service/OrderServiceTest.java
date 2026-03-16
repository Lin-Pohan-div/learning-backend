package com.learning.api.service;

import com.learning.api.dto.OrderDto;
import com.learning.api.entity.Course;
import com.learning.api.entity.Order;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.OrderRepository;
import com.learning.api.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepo;
    @Mock private UserRepository userRepo;
    @Mock private CourseRepo courseRepo;

    @InjectMocks
    private OrderService orderService;

    private Course makeCourse(Long id, Long tutorId, int price, boolean active) {
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

    private OrderDto.Req makeOrderReq(Long userId, Long courseId, int lessonCount) {
        OrderDto.Req req = new OrderDto.Req();
        req.setUserId(userId);
        req.setCourseId(courseId);
        req.setLessonCount(lessonCount);
        return req;
    }

    private Order makeOrder(Long id, Long userId, Long courseId, int status) {
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

    // ── createOrder ───────────────────────────────────────────────────────────

    @Test
    void createOrder_whenValidReq_returnsTrue() {
        Course course = makeCourse(1L, 2L, 500, true);
        when(userRepo.existsById(3L)).thenReturn(true);
        when(courseRepo.findById(1L)).thenReturn(Optional.of(course));

        boolean result = orderService.createOrder(makeOrderReq(3L, 1L, 5));

        assertThat(result).isTrue();
        verify(orderRepo).save(any(Order.class));
    }

    @Test
    void createOrder_whenNullReq_returnsFalse() {
        assertThat(orderService.createOrder(null)).isFalse();
    }

    @Test
    void createOrder_whenNullUserId_returnsFalse() {
        OrderDto.Req req = new OrderDto.Req();
        req.setCourseId(1L);
        req.setLessonCount(5);
        assertThat(orderService.createOrder(req)).isFalse();
    }

    @Test
    void createOrder_whenLessonCountZero_returnsFalse() {
        assertThat(orderService.createOrder(makeOrderReq(1L, 1L, 0))).isFalse();
    }

    @Test
    void createOrder_whenUserNotFound_returnsFalse() {
        when(userRepo.existsById(99L)).thenReturn(false);
        assertThat(orderService.createOrder(makeOrderReq(99L, 1L, 5))).isFalse();
    }

    @Test
    void createOrder_whenCourseNotFound_returnsFalse() {
        when(userRepo.existsById(1L)).thenReturn(true);
        when(courseRepo.findById(99L)).thenReturn(Optional.empty());
        assertThat(orderService.createOrder(makeOrderReq(1L, 99L, 5))).isFalse();
    }

    @Test
    void createOrder_whenCourseInactive_returnsFalse() {
        Course course = makeCourse(1L, 2L, 500, false);
        when(userRepo.existsById(1L)).thenReturn(true);
        when(courseRepo.findById(1L)).thenReturn(Optional.of(course));
        assertThat(orderService.createOrder(makeOrderReq(1L, 1L, 5))).isFalse();
    }

    @Test
    void createOrder_withLessonCountUnder10_noDiscount() {
        Course course = makeCourse(1L, 2L, 500, true);
        when(userRepo.existsById(1L)).thenReturn(true);
        when(courseRepo.findById(1L)).thenReturn(Optional.of(course));

        orderService.createOrder(makeOrderReq(1L, 1L, 9));

        verify(orderRepo).save(argThat(order -> order.getDiscountPrice().equals(500)));
    }

    @Test
    void createOrder_withLessonCount10OrMore_applies95PercentDiscount() {
        Course course = makeCourse(1L, 2L, 500, true);
        when(userRepo.existsById(1L)).thenReturn(true);
        when(courseRepo.findById(1L)).thenReturn(Optional.of(course));

        orderService.createOrder(makeOrderReq(1L, 1L, 10));

        verify(orderRepo).save(argThat(order -> order.getDiscountPrice().equals(475))); // 500 * 0.95
    }

    // ── updateOrder ───────────────────────────────────────────────────────────

    @Test
    void updateOrder_whenValidReq_returnsTrue() {
        Order order = makeOrder(1L, 1L, 1L, 1);
        order.setLessonCount(5);
        order.setLessonUsed(2);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        OrderDto.UpdateReq req = new OrderDto.UpdateReq();
        req.setLessonUsed(3);

        assertThat(orderService.updateOrder(1L, req)).isTrue();
        verify(orderRepo).save(order);
    }

    @Test
    void updateOrder_whenNullReq_returnsFalse() {
        assertThat(orderService.updateOrder(1L, null)).isFalse();
    }

    @Test
    void updateOrder_whenOrderNotFound_returnsFalse() {
        when(orderRepo.findById(99L)).thenReturn(Optional.empty());
        OrderDto.UpdateReq req = new OrderDto.UpdateReq();
        req.setLessonUsed(1);
        assertThat(orderService.updateOrder(99L, req)).isFalse();
    }

    @Test
    void updateOrder_whenOrderIsComplete_returnsFalse() {
        Order order = makeOrder(1L, 1L, 1L, 3); // complete
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        OrderDto.UpdateReq req = new OrderDto.UpdateReq();
        req.setLessonUsed(1);
        assertThat(orderService.updateOrder(1L, req)).isFalse();
    }

    @Test
    void updateOrder_whenLessonUsedExceedsLessonCount_returnsFalse() {
        Order order = makeOrder(1L, 1L, 1L, 1);
        order.setLessonCount(5);
        order.setLessonUsed(2);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        OrderDto.UpdateReq req = new OrderDto.UpdateReq();
        req.setLessonUsed(10); // exceeds lessonCount=5

        assertThat(orderService.updateOrder(1L, req)).isFalse();
    }

    // ── getOrderById ──────────────────────────────────────────────────────────

    @Test
    void getOrderById_whenExists_returnsResp() {
        Order order = makeOrder(1L, 2L, 3L, 1);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        OrderDto.Resp resp = orderService.getOrderById(1L);

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getUserId()).isEqualTo(2L);
    }

    @Test
    void getOrderById_whenNotFound_returnsNull() {
        when(orderRepo.findById(99L)).thenReturn(Optional.empty());
        assertThat(orderService.getOrderById(99L)).isNull();
    }

    // ── getOrdersByUserId ─────────────────────────────────────────────────────

    @Test
    void getOrdersByUserId_returnsMappedList() {
        Order order = makeOrder(1L, 5L, 1L, 1);
        when(orderRepo.findByUserId(5L)).thenReturn(List.of(order));

        var list = orderService.getOrdersByUserId(5L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getUserId()).isEqualTo(5L);
    }

    // ── updateStatus ──────────────────────────────────────────────────────────

    @Test
    void updateStatus_whenForwardTransition_returnsTrue() {
        Order order = makeOrder(1L, 1L, 1L, 1); // pending
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        OrderDto.StatusReq req = new OrderDto.StatusReq();
        req.setStatus(2); // paid

        assertThat(orderService.updateStatus(1L, req)).isTrue();
        verify(orderRepo).save(order);
    }

    @Test
    void updateStatus_whenNullReq_returnsFalse() {
        assertThat(orderService.updateStatus(1L, null)).isFalse();
    }

    @Test
    void updateStatus_whenOrderNotFound_returnsFalse() {
        when(orderRepo.findById(99L)).thenReturn(Optional.empty());
        OrderDto.StatusReq req = new OrderDto.StatusReq();
        req.setStatus(2);
        assertThat(orderService.updateStatus(99L, req)).isFalse();
    }

    @Test
    void updateStatus_whenBackwardTransition_returnsFalse() {
        Order order = makeOrder(1L, 1L, 1L, 2); // paid
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        OrderDto.StatusReq req = new OrderDto.StatusReq();
        req.setStatus(1); // back to pending — not allowed

        assertThat(orderService.updateStatus(1L, req)).isFalse();
    }

    // ── cancelOrder ───────────────────────────────────────────────────────────

    @Test
    void cancelOrder_whenPendingOrder_returnsTrue() {
        Order order = makeOrder(1L, 1L, 1L, 1); // pending
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        assertThat(orderService.cancelOrder(1L)).isTrue();
        verify(orderRepo).deleteById(1L);
    }

    @Test
    void cancelOrder_whenNotPending_returnsFalse() {
        Order order = makeOrder(1L, 1L, 1L, 2); // paid
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        assertThat(orderService.cancelOrder(1L)).isFalse();
        verify(orderRepo, never()).deleteById(any());
    }

    @Test
    void cancelOrder_whenNotFound_returnsFalse() {
        when(orderRepo.findById(99L)).thenReturn(Optional.empty());
        assertThat(orderService.cancelOrder(99L)).isFalse();
    }

    // ── payOrder ──────────────────────────────────────────────────────────────

    @Test
    void payOrder_whenPendingOrder_setsStatusToPaid() {
        Order order = makeOrder(1L, 1L, 1L, 1); // pending
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        assertThat(orderService.payOrder(1L)).isTrue();
        assertThat(order.getStatus()).isEqualTo(2);
        verify(orderRepo).save(order);
    }

    @Test
    void payOrder_whenNotPending_returnsFalse() {
        Order order = makeOrder(1L, 1L, 1L, 2); // already paid
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        assertThat(orderService.payOrder(1L)).isFalse();
        verify(orderRepo, never()).save(any());
    }

    @Test
    void payOrder_whenNotFound_returnsFalse() {
        when(orderRepo.findById(99L)).thenReturn(Optional.empty());
        assertThat(orderService.payOrder(99L)).isFalse();
    }
}
