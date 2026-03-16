package com.learning.api.service;

import com.learning.api.dto.course.CourseReq;
import com.learning.api.dto.course.CourseResp;
import com.learning.api.entity.Course;
import com.learning.api.entity.User;
import com.learning.api.enums.UserRole;
import com.learning.api.repo.*;
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
class CourseServiceTest {

    @Mock private UserRepository userRepo;
    @Mock private CourseRepo courseRepo;
    @Mock private OrderRepository orderRepo;
    @Mock private BookingRepository bookingRepo;
    @Mock private LessonFeedbackRepository feedbackRepo;

    @InjectMocks
    private CourseService courseService;

    private User makeUser(Long id, String email, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword("$2a$10$abcdefghijklmnopqrstuv");
        user.setRole(role);
        user.setWallet(0L);
        return user;
    }

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

    private CourseReq makeCourseReq(Long tutorId) {
        CourseReq req = new CourseReq();
        req.setTutorId(tutorId);
        req.setName("My Course");
        req.setSubject(21);
        req.setDescription("Description");
        req.setPrice(500);
        req.setActive(true);
        return req;
    }

    // ── sendCourses ───────────────────────────────────────────────────────────

    @Test
    void sendCourses_whenValid_savesAndReturnsTrue() {
        User tutor = makeUser(1L, "tutor@test.com", UserRole.TUTOR);
        when(userRepo.findById(1L)).thenReturn(Optional.of(tutor));

        boolean result = courseService.sendCourses(makeCourseReq(1L));

        assertThat(result).isTrue();
        verify(courseRepo).save(any(Course.class));
    }

    @Test
    void sendCourses_whenNull_returnsFalse() {
        assertThat(courseService.sendCourses(null)).isFalse();
    }

    @Test
    void sendCourses_whenInvalidSubject_returnsFalse() {
        CourseReq req = makeCourseReq(1L);
        req.setSubject(99); // invalid
        assertThat(courseService.sendCourses(req)).isFalse();
    }

    @Test
    void sendCourses_whenPriceZero_returnsFalse() {
        CourseReq req = makeCourseReq(1L);
        req.setPrice(0);
        assertThat(courseService.sendCourses(req)).isFalse();
    }

    @Test
    void sendCourses_whenTutorNotFound_returnsFalse() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());
        assertThat(courseService.sendCourses(makeCourseReq(99L))).isFalse();
    }

    @Test
    void sendCourses_whenUserIsNotTutor_returnsFalse() {
        User student = makeUser(1L, "student@test.com", UserRole.STUDENT);
        when(userRepo.findById(1L)).thenReturn(Optional.of(student));
        assertThat(courseService.sendCourses(makeCourseReq(1L))).isFalse();
    }

    // ── getAllCourses ─────────────────────────────────────────────────────────

    @Test
    void getAllCourses_whenNoCourses_returnsEmptyList() {
        when(courseRepo.findAll()).thenReturn(List.of());
        assertThat(courseService.getAllCourses()).isEmpty();
    }

    @Test
    void getAllCourses_whenCoursesExist_returnsList() {
        Course course = makeCourse(1L, 2L, 500, true);
        when(courseRepo.findAll()).thenReturn(List.of(course));
        when(orderRepo.findByCourseIdIn(List.of(1L))).thenReturn(List.of());

        List<CourseResp> result = courseService.getAllCourses();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    // ── getCourseById ─────────────────────────────────────────────────────────

    @Test
    void getCourseById_whenExists_returnsCourseResp() {
        Course course = makeCourse(1L, 2L, 500, true);
        when(courseRepo.findById(1L)).thenReturn(Optional.of(course));
        when(orderRepo.findByCourseId(1L)).thenReturn(List.of());

        CourseResp resp = courseService.getCourseById(1L);

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isEqualTo(1L);
    }

    @Test
    void getCourseById_whenNotFound_returnsNull() {
        when(courseRepo.findById(99L)).thenReturn(Optional.empty());
        assertThat(courseService.getCourseById(99L)).isNull();
    }

    // ── findByTutorId ─────────────────────────────────────────────────────────

    @Test
    void findByTutorId_returnsListFromRepo() {
        Course course = makeCourse(1L, 5L, 500, true);
        when(courseRepo.findByTutorId(5L)).thenReturn(List.of(course));

        assertThat(courseService.findByTutorId(5L)).hasSize(1);
    }

    @Test
    void findByTutorIdActive_returnsOnlyActiveCourses() {
        Course active = makeCourse(1L, 5L, 500, true);
        when(courseRepo.findByTutorIdAndActive(5L, true)).thenReturn(List.of(active));

        assertThat(courseService.findByTutorIdActive(5L)).hasSize(1);
    }

    // ── updateCourse ──────────────────────────────────────────────────────────

    @Test
    void updateCourse_whenExists_returnsUpdatedCourse() {
        Course course = makeCourse(1L, 2L, 500, true);
        when(courseRepo.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepo.save(any())).thenReturn(course);

        CourseReq req = makeCourseReq(2L);
        req.setName("Updated");
        req.setSubject(21);
        req.setPrice(600);
        req.setActive(true);

        var result = courseService.updateCourse(1L, req);

        assertThat(result).isPresent();
    }

    @Test
    void updateCourse_whenNotFound_returnsEmptyOptional() {
        when(courseRepo.findById(99L)).thenReturn(Optional.empty());

        Optional<Course> result = courseService.updateCourse(99L, makeCourseReq(1L));

        assertThat(result).isEmpty();
    }

    @Test
    void updateCourse_whenInvalidSubject_throwsIllegalArgument() {
        Course course = makeCourse(1L, 2L, 500, true);
        when(courseRepo.findById(1L)).thenReturn(Optional.of(course));

        CourseReq req = makeCourseReq(2L);
        req.setSubject(99); // invalid

        assertThatThrownBy(() -> courseService.updateCourse(1L, req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── deleteById ────────────────────────────────────────────────────────────

    @Test
    void deleteById_whenExists_returnsTrue() {
        when(courseRepo.existsById(1L)).thenReturn(true);

        assertThat(courseService.deleteById(1L)).isTrue();
        verify(courseRepo).deleteById(1L);
    }

    @Test
    void deleteById_whenNotFound_returnsFalse() {
        when(courseRepo.existsById(99L)).thenReturn(false);
        assertThat(courseService.deleteById(99L)).isFalse();
    }
}
