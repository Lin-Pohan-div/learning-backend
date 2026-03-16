package com.learning.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.learning.api.enums.UserRole;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class OrderControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private Long testUserId;
    private Long noOrderUserId;
    private Long testCourseId;
    private Long inactiveCourseId;
    private Order pendingOrder;
    private Order dealOrder;
    private Order completeOrder;

    @BeforeEach
    void setUp() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        User user = new User();
        user.setName("Test Student");
        user.setEmail("student_order@example.com");
        user.setPassword("hashedpassword");
        user.setRole(UserRole.STUDENT);
        user.setWallet(0L);
        user = userRepository.save(user);
        testUserId = user.getId();

        User noOrderUser = new User();
        noOrderUser.setName("No Order Student");
        noOrderUser.setEmail("noorder_student@example.com");
        noOrderUser.setPassword("hashedpassword");
        noOrderUser.setRole(UserRole.STUDENT);
        noOrderUser.setWallet(0L);
        noOrderUser = userRepository.save(noOrderUser);
        noOrderUserId = noOrderUser.getId();

        Course activeCourse = new Course();
        activeCourse.setTutorId(user.getId());
        activeCourse.setName("Active Course");
        activeCourse.setSubject(11);
        /* activeCourse.setLevel(1); */
        activeCourse.setPrice(500);
        activeCourse.setActive(true);
        activeCourse = courseRepo.save(activeCourse);
        testCourseId = activeCourse.getId();

        Course inactiveCourse = new Course();
        inactiveCourse.setTutorId(user.getId());
        inactiveCourse.setName("Inactive Course");
        inactiveCourse.setSubject(11);
        /* inactiveCourse.setLevel(1); */
        inactiveCourse.setPrice(500);
        inactiveCourse.setActive(false);
        inactiveCourse = courseRepo.save(inactiveCourse);
        inactiveCourseId = inactiveCourse.getId();

        pendingOrder = new Order();
        pendingOrder.setUserId(testUserId);
        pendingOrder.setCourseId(testCourseId);
        pendingOrder.setUnitPrice(500);
        pendingOrder.setDiscountPrice(500);
        pendingOrder.setLessonCount(5);
        pendingOrder.setLessonUsed(0);
        pendingOrder.setStatus(1);
        pendingOrder = orderRepository.save(pendingOrder);

        dealOrder = new Order();
        dealOrder.setUserId(testUserId);
        dealOrder.setCourseId(testCourseId);
        dealOrder.setUnitPrice(500);
        dealOrder.setDiscountPrice(500);
        dealOrder.setLessonCount(5);
        dealOrder.setLessonUsed(0);
        dealOrder.setStatus(2);
        dealOrder = orderRepository.save(dealOrder);

        completeOrder = new Order();
        completeOrder.setUserId(testUserId);
        completeOrder.setCourseId(testCourseId);
        completeOrder.setUnitPrice(500);
        completeOrder.setDiscountPrice(500);
        completeOrder.setLessonCount(5);
        completeOrder.setLessonUsed(5);
        completeOrder.setStatus(3);
        completeOrder = orderRepository.save(completeOrder);
    }

    // ===================== POST /api/orders =====================

    @Test
    void createOrder_validRequest_shouldReturn200() throws Exception {
        Map<String, Object> body = Map.of(
                "userId", testUserId,
                "courseId", testCourseId,
                "lessonCount", 5
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("訂單建立成功"));
    }

    @Test
    void createOrder_10Lessons_shouldReturn200() throws Exception {
        Map<String, Object> body = Map.of(
                "userId", testUserId,
                "courseId", testCourseId,
                "lessonCount", 10
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("訂單建立成功"));
    }

    @Test
    void createOrder_nonExistentUser_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "userId", 999999L,
                "courseId", testCourseId,
                "lessonCount", 5
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("建立訂單失敗"));
    }

    @Test
    void createOrder_inactiveCourse_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "userId", testUserId,
                "courseId", inactiveCourseId,
                "lessonCount", 5
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("建立訂單失敗"));
    }

    // ===================== GET /api/orders/{id} =====================

    @Test
    void getOrder_existingId_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", pendingOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pendingOrder.getId()))
                .andExpect(jsonPath("$.userId").value(testUserId))
                .andExpect(jsonPath("$.courseId").value(testCourseId))
                .andExpect(jsonPath("$.status").value(1));
    }

    @Test
    void getOrder_nonExistentId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("訂單不存在"));
    }

    // ===================== GET /api/orders/user/{userId} =====================

    @Test
    void getOrdersByUser_existingUser_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/orders/user/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    void getOrdersByUser_noOrders_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/orders/user/{userId}", noOrderUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ===================== PUT /api/orders/{id} =====================

    @Test
    void updateOrder_lessonCount_shouldReturn200() throws Exception {
        Map<String, Object> body = Map.of("lessonCount", 8);

        mockMvc.perform(put("/api/orders/{id}", pendingOrder.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("訂單更新成功"));
    }

    @Test
    void updateOrder_completeStatus_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of("lessonCount", 8);

        mockMvc.perform(put("/api/orders/{id}", completeOrder.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("訂單更新失敗"));
    }

    @Test
    void updateOrder_nonExistentId_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of("lessonCount", 8);

        mockMvc.perform(put("/api/orders/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("訂單更新失敗"));
    }

    // ===================== PATCH /api/orders/{id}/status =====================

    @Test
    void updateStatus_pendingToDeal_shouldReturn200() throws Exception {
        Map<String, Object> body = Map.of("status", 2);

        mockMvc.perform(patch("/api/orders/{id}/status", pendingOrder.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("狀態更新成功"));
    }

    @Test
    void updateStatus_backward_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of("status", 1);

        mockMvc.perform(patch("/api/orders/{id}/status", dealOrder.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("狀態更新失敗"));
    }

    @Test
    void updateStatus_nonExistentId_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of("status", 2);

        mockMvc.perform(patch("/api/orders/{id}/status", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("狀態更新失敗"));
    }

    // ===================== DELETE /api/orders/{id} =====================

    @Test
    void cancelOrder_pendingStatus_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/orders/{id}", pendingOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("訂單已取消"));
    }

    @Test
    void cancelOrder_dealStatus_shouldReturn400() throws Exception {
        mockMvc.perform(delete("/api/orders/{id}", dealOrder.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("取消失敗，僅 pending 訂單可取消"));
    }

    @Test
    void cancelOrder_nonExistentId_shouldReturn400() throws Exception {
        mockMvc.perform(delete("/api/orders/{id}", 999999L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("取消失敗，僅 pending 訂單可取消"));
    }

    // ===================== POST /api/orders/{id}/pay =====================

    @Test
    void payOrder_pendingStatus_shouldReturn200() throws Exception {
        mockMvc.perform(post("/api/orders/{id}/pay", pendingOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("支付成功"));
    }

    @Test
    void payOrder_dealStatus_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/orders/{id}/pay", dealOrder.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("支付失敗"));
    }

    @Test
    void payOrder_nonExistentId_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/orders/{id}/pay", 999999L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("支付失敗"));
    }
}
