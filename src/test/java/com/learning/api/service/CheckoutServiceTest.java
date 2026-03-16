package com.learning.api.service;

import com.learning.api.dto.CheckoutReq;
import com.learning.api.entity.*;
import com.learning.api.repo.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock private UserRepository userRepo;
    @Mock private CourseRepo courseRepo;
    @Mock private OrderRepository orderRepo;
    @Mock private BookingRepository bookingRepo;
    @Mock private TutorScheduleRepo scheduleRepo;

    @InjectMocks
    private CheckoutService checkoutService;

    

    private User makeStudent(Long id, long wallet) {
        User user = new User();
        user.setId(id);
        user.setName("Student");
        user.setEmail("student@test.com");
        user.setPassword("hashedpw");
        user.setWallet(wallet);
        return user;
    }

    private Course makeCourse(Long id, Long tutorId, int price) {
        Course course = new Course();
        course.setId(id);
        course.setTutorId(tutorId);
        course.setName("Test Course");
        course.setSubject(21);
        course.setDescription("Desc");
        course.setPrice(price);
        course.setActive(true);
        return course;
    }

    private TutorSchedule makeAvailableSchedule(Long tutorId, int weekday, int hour) {
        TutorSchedule s = new TutorSchedule();
        s.setId(1L);
        s.setTutorId(tutorId);
        s.setWeekday(weekday);
        s.setHour(hour);
        s.setStatus("available");
        return s;
    }

    /** 建立含一個時段的 CheckoutReq（2026-03-16 星期一 = weekday 1） */
    private CheckoutReq makeReq(Long studentId, Long courseId, LocalDate date, int hour) {
        CheckoutReq req = new CheckoutReq();
        req.setStudentId(studentId);
        req.setCourseId(courseId);
        CheckoutReq.Slot slot = new CheckoutReq.Slot();
        slot.setDate(date);
        slot.setHour(hour);
        req.setSelectedSlots(List.of(slot));
        return req;
    }

    // ── processPurchase ───────────────────────────────────────────────────────

    @Test
    void processPurchase_validRequest_returnsSuccess() {
        LocalDate date = LocalDate.of(2026, 3, 16); // 星期一 (weekday=1)
        User student = makeStudent(1L, 1000L);
        Course course = makeCourse(1L, 2L, 500);
        Order savedOrder = new Order();
        savedOrder.setId(10L);

        when(userRepo.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepo.findById(1L)).thenReturn(Optional.of(course));
        when(scheduleRepo.findByTutorIdAndWeekdayAndHour(2L, 1, 10))
                .thenReturn(Optional.of(makeAvailableSchedule(2L, 1, 10)));
        when(bookingRepo.findByTutorIdAndDateAndHour(2L, date, 10)).thenReturn(Optional.empty());
        when(orderRepo.save(any(Order.class))).thenReturn(savedOrder);

        String result = checkoutService.processPurchase(makeReq(1L, 1L, date, 10));

        assertThat(result).isEqualTo("success");
    }

    @Test
    void processPurchase_insufficientWallet_returnsBalanceError() {
        LocalDate date = LocalDate.of(2026, 3, 16);
        User student = makeStudent(1L, 0L);   // 錢包為 0
        Course course = makeCourse(1L, 2L, 500); // 課程 500 元

        when(userRepo.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepo.findById(1L)).thenReturn(Optional.of(course));

        String result = checkoutService.processPurchase(makeReq(1L, 1L, date, 10));

        assertThat(result).isEqualTo("餘額不足");
    }

    @Test
    void processPurchase_scheduleNotFound_returnsSlotError() {
        LocalDate date = LocalDate.of(2026, 3, 16);
        User student = makeStudent(1L, 1000L);
        Course course = makeCourse(1L, 2L, 500);

        when(userRepo.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepo.findById(1L)).thenReturn(Optional.of(course));
        when(scheduleRepo.findByTutorIdAndWeekdayAndHour(2L, 1, 10)).thenReturn(Optional.empty());

        String result = checkoutService.processPurchase(makeReq(1L, 1L, date, 10));

        assertThat(result).isNotEqualTo("success").isNotEqualTo("餘額不足");
    }

    @Test
    void processPurchase_scheduleStatusInactive_returnsSlotError() {
        LocalDate date = LocalDate.of(2026, 3, 16);
        User student = makeStudent(1L, 1000L);
        Course course = makeCourse(1L, 2L, 500);
        TutorSchedule inactive = makeAvailableSchedule(2L, 1, 10);
        inactive.setStatus("inactive");

        when(userRepo.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepo.findById(1L)).thenReturn(Optional.of(course));
        when(scheduleRepo.findByTutorIdAndWeekdayAndHour(2L, 1, 10)).thenReturn(Optional.of(inactive));

        String result = checkoutService.processPurchase(makeReq(1L, 1L, date, 10));

        assertThat(result).isNotEqualTo("success").isNotEqualTo("餘額不足");
    }

    @Test
    void processPurchase_slotAlreadyBooked_returnsBookedError() {
        LocalDate date = LocalDate.of(2026, 3, 16);
        User student = makeStudent(1L, 1000L);
        Course course = makeCourse(1L, 2L, 500);
        Bookings existingBooking = new Bookings();

        when(userRepo.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepo.findById(1L)).thenReturn(Optional.of(course));
        when(scheduleRepo.findByTutorIdAndWeekdayAndHour(2L, 1, 10))
                .thenReturn(Optional.of(makeAvailableSchedule(2L, 1, 10)));
        when(bookingRepo.findByTutorIdAndDateAndHour(2L, date, 10)).thenReturn(Optional.of(existingBooking));

        String result = checkoutService.processPurchase(makeReq(1L, 1L, date, 10));

        assertThat(result).isNotEqualTo("success").isNotEqualTo("餘額不足");
    }

    @Test
    void processPurchase_studentNotFound_throwsNoSuchElement() {
        when(userRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                checkoutService.processPurchase(makeReq(999L, 1L, LocalDate.of(2026, 3, 16), 10)))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void processPurchase_courseNotFound_throwsNoSuchElement() {
        User student = makeStudent(1L, 1000L);
        when(userRepo.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                checkoutService.processPurchase(makeReq(1L, 999L, LocalDate.of(2026, 3, 16), 10)))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void processPurchase_success_deductsWalletAndSavesOrderAndBookings() {
        LocalDate date = LocalDate.of(2026, 3, 16);
        User student = makeStudent(1L, 1000L);
        Course course = makeCourse(1L, 2L, 500);
        Order savedOrder = new Order();
        savedOrder.setId(10L);

        when(userRepo.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepo.findById(1L)).thenReturn(Optional.of(course));
        when(scheduleRepo.findByTutorIdAndWeekdayAndHour(2L, 1, 10))
                .thenReturn(Optional.of(makeAvailableSchedule(2L, 1, 10)));
        when(bookingRepo.findByTutorIdAndDateAndHour(2L, date, 10)).thenReturn(Optional.empty());
        when(orderRepo.save(any(Order.class))).thenReturn(savedOrder);

        checkoutService.processPurchase(makeReq(1L, 1L, date, 10));

        verify(userRepo).save(student);
        verify(orderRepo).save(any(Order.class));
        verify(bookingRepo).saveAll(any());
        assertThat(student.getWallet()).isEqualTo(500L); // 1000 - 500
    }

    @Test
    void processPurchase_multipleSlots_checksEachSlotAndCreatesBookings() {
        LocalDate mon = LocalDate.of(2026, 3, 16); // weekday=1
        LocalDate tue = LocalDate.of(2026, 3, 17); // weekday=2
        User student = makeStudent(1L, 2000L);
        Course course = makeCourse(1L, 2L, 500);
        Order savedOrder = new Order();
        savedOrder.setId(10L);

        CheckoutReq req = new CheckoutReq();
        req.setStudentId(1L);
        req.setCourseId(1L);
        CheckoutReq.Slot s1 = new CheckoutReq.Slot(); s1.setDate(mon); s1.setHour(10);
        CheckoutReq.Slot s2 = new CheckoutReq.Slot(); s2.setDate(tue); s2.setHour(11);
        req.setSelectedSlots(List.of(s1, s2));

        when(userRepo.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepo.findById(1L)).thenReturn(Optional.of(course));
        when(scheduleRepo.findByTutorIdAndWeekdayAndHour(2L, 1, 10))
                .thenReturn(Optional.of(makeAvailableSchedule(2L, 1, 10)));
        when(scheduleRepo.findByTutorIdAndWeekdayAndHour(2L, 2, 11))
                .thenReturn(Optional.of(makeAvailableSchedule(2L, 2, 11)));
        when(bookingRepo.findByTutorIdAndDateAndHour(any(), any(), any())).thenReturn(Optional.empty());
        when(orderRepo.save(any(Order.class))).thenReturn(savedOrder);

        String result = checkoutService.processPurchase(req);

        assertThat(result).isEqualTo("success");
        assertThat(student.getWallet()).isEqualTo(1000L); // 2000 - 500*2
    }
}
