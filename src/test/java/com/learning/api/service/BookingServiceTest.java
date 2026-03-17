package com.learning.api.service;

import com.learning.api.TestFactory;
import com.learning.api.dto.booking.BookingReq;
import com.learning.api.entity.Bookings;
import com.learning.api.entity.Course;
import com.learning.api.repo.BookingRepository;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CourseRepo courseRepo;
    @Mock private BookingRepository bookingRepo;

    @InjectMocks
    private BookingService bookingService;

    

    private Course makeCourse(Long id, Long tutorId) {
        return TestFactory.makeCourse(id, tutorId);
    }

    private BookingReq makeReq(Long userId, Long courseId, int lessonCount) {
        return TestFactory.makeBookingReq(userId, courseId, lessonCount);
    }

    // ── sendBooking ───────────────────────────────────────────────────────────

    @Test
    void sendBooking_validRequest_returnsTrue() {
        Course course = makeCourse(1L, 2L);
        when(userRepository.existsById(3L)).thenReturn(true);
        when(courseRepo.findById(1L)).thenReturn(Optional.of(course));

        boolean result = bookingService.sendBooking(makeReq(3L, 1L, 5));

        assertThat(result).isTrue();
        verify(bookingRepo).save(any(Bookings.class));
    }

    @Test
    void sendBooking_nullRequest_returnsFalse() {
        assertThat(bookingService.sendBooking(null)).isFalse();
        verifyNoInteractions(bookingRepo);
    }

    @Test
    void sendBooking_lessonCountZero_returnsFalse() {
        boolean result = bookingService.sendBooking(makeReq(1L, 1L, 0));
        assertThat(result).isFalse();
        verifyNoInteractions(bookingRepo);
    }

    @Test
    void sendBooking_lessonCountNegative_returnsFalse() {
        boolean result = bookingService.sendBooking(makeReq(1L, 1L, -1));
        assertThat(result).isFalse();
    }

    @Test
    void sendBooking_userNotFound_returnsFalse() {
        when(userRepository.existsById(999L)).thenReturn(false);

        boolean result = bookingService.sendBooking(makeReq(999L, 1L, 5));

        assertThat(result).isFalse();
        verifyNoInteractions(bookingRepo);
    }

    @Test
    void sendBooking_courseNotFound_returnsFalse() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(courseRepo.findById(999L)).thenReturn(Optional.empty());

        boolean result = bookingService.sendBooking(makeReq(1L, 999L, 5));

        assertThat(result).isFalse();
        verifyNoInteractions(bookingRepo);
    }

    @Test
    void sendBooking_savedBooking_hasTutorAndStudentIds() {
        Course course = makeCourse(1L, 2L);
        when(userRepository.existsById(3L)).thenReturn(true);
        when(courseRepo.findById(1L)).thenReturn(Optional.of(course));

        bookingService.sendBooking(makeReq(3L, 1L, 5));

        verify(bookingRepo).save(argThat(b ->
                b.getTutorId().equals(2L) && b.getStudentId().equals(3L)
        ));
    }

    @Test
    void sendBooking_savedBooking_hasDateAndHour() {
        Course course = makeCourse(1L, 2L);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(courseRepo.findById(1L)).thenReturn(Optional.of(course));

        BookingReq req = makeReq(1L, 1L, 3);
        req.setDate(LocalDate.of(2026, 4, 1));
        req.setHour(14);

        bookingService.sendBooking(req);

        verify(bookingRepo).save(argThat(b ->
                b.getDate().equals(LocalDate.of(2026, 4, 1)) && b.getHour().equals(14)
        ));
    }
}
