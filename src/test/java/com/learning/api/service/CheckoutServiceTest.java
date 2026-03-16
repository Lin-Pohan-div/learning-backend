package com.learning.api.service;

import com.learning.api.dto.CheckoutReq;
import com.learning.api.entity.*;
import com.learning.api.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CheckoutServiceTest {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private CourseRepo courseRepo;
    @Autowired
    private TutorScheduleRepo scheduleRepo;
    @Autowired
    private BookingRepository bookingRepo;
    @Autowired
    private OrderRepository orderRepo;

    private Long studentId;
    private Long poorStudentId;
    private Long tutorId;
    private Long courseId;
    private LocalDate nextMonday;
    private static final int TEST_HOUR = 10;

    @BeforeEach
    void setUp() {
        User student = new User();
        student.setName("Checkout Student");
        student.setEmail("checkoutsvc.student@example.com");
        student.setPassword("hashed");
        student.setRole(1);
        student.setWallet(10000L);
        student = userRepo.save(student);
        studentId = student.getId();

        User poorStudent = new User();
        poorStudent.setName("Poor Checkout Student");
        poorStudent.setEmail("checkoutsvc.poor@example.com");
        poorStudent.setPassword("hashed");
        poorStudent.setRole(1);
        poorStudent.setWallet(0L);
        poorStudent = userRepo.save(poorStudent);
        poorStudentId = poorStudent.getId();

        User tutor = new User();
        tutor.setName("Checkout Tutor");
        tutor.setEmail("checkoutsvc.tutor@example.com");
        tutor.setPassword("hashed");
        tutor.setRole(2);
        tutor.setWallet(0L);
        tutor = userRepo.save(tutor);
        tutorId = tutor.getId();

        Course course = new Course();
        course.setTutorId(tutorId);
        course.setName("Checkout Test Course");
        course.setSubject(11);
        course.setPrice(500);
        course.setActive(true);
        course = courseRepo.save(course);
        courseId = course.getId();

        nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        TutorSchedule schedule = new TutorSchedule();
        schedule.setTutorId(tutorId);
        schedule.setWeekday(1); // 星期一
        schedule.setHour(TEST_HOUR);
        schedule.setStatus("available");
        scheduleRepo.save(schedule);

    }

    private CheckoutReq buildReq(Long studentId, Long courseId, LocalDate date, int hour) {
        CheckoutReq.Slot slot = new CheckoutReq.Slot();
        slot.setDate(date);
        slot.setHour(hour);

        CheckoutReq req = new CheckoutReq();
        req.setStudentId(studentId);
        req.setCourseId(courseId);
        req.setSelectedSlots(List.of(slot));
        return req;
    }

    @Test
    void processPurchase_validSlot_returnsSuccess() {
        String result = checkoutService.processPurchase(buildReq(studentId, courseId, nextMonday, TEST_HOUR));
        assertEquals("success", result);
    }

    @Test
    void processPurchase_insufficientBalance_returns餘額不足() {
        String result = checkoutService.processPurchase(buildReq(poorStudentId, courseId, nextMonday, TEST_HOUR));
        assertEquals("餘額不足", result);
    }

    @Test
    void processPurchase_noSchedule_returnsErrorMessage() {
        // 使用沒有課表的時段 (23:00)
        String result = checkoutService.processPurchase(buildReq(studentId, courseId, nextMonday, 23));
        assertNotNull(result);
        assertNotEquals("success", result);
        assertNotEquals("餘額不足", result);
    }

    @Test
    void processPurchase_alreadyBooked_returnsErrorMessage() {
        Order order = new Order();
        order.setUserId(studentId);
        order.setCourseId(courseId);
        order.setUnitPrice(500);
        order.setDiscountPrice(500);
        order.setLessonCount(1);
        order.setLessonUsed(0);
        order.setStatus(2);
        order = orderRepo.save(order);

        Bookings existing = new Bookings();
        existing.setOrderId(order.getId());
        existing.setTutorId(tutorId);
        existing.setStudentId(studentId);
        existing.setDate(nextMonday);
        existing.setHour(TEST_HOUR);
        existing.setStatus((byte) 1);
        bookingRepo.save(existing);

        String result = checkoutService.processPurchase(buildReq(studentId, courseId, nextMonday, TEST_HOUR));
        assertNotNull(result);
        assertNotEquals("success", result);
    }

    @Test
    void processPurchase_success_deductsWallet() {
        checkoutService.processPurchase(buildReq(studentId, courseId, nextMonday, TEST_HOUR));

        User updated = userRepo.findById(studentId).orElseThrow();
        assertEquals(9500L, updated.getWallet()); // 10000 - 500
    }
}
