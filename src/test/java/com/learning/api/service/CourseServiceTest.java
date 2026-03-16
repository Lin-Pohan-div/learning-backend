package com.learning.api.service;

import com.learning.api.dto.course.CourseReq;
import com.learning.api.dto.course.CourseResp;
import com.learning.api.entity.Course;
import com.learning.api.entity.User;
import com.learning.api.repo.CourseRepo;
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
class CourseServiceTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CourseRepo courseRepo;

    private Long tutorUserId;
    private Long studentUserId;
    private Long savedCourseId;

    @BeforeEach
    void setUp() {
        User tutor = new User();
        tutor.setName("Course Svc Tutor");
        tutor.setEmail("coursesvc.tutor@example.com");
        tutor.setPassword("hashed");
        tutor.setRole(UserRole.TUTOR);
        tutor.setWallet(0L);
        tutor = userRepo.save(tutor);
        tutorUserId = tutor.getId();

        User student = new User();
        student.setName("Course Svc Student");
        student.setEmail("coursesvc.student@example.com");
        student.setPassword("hashed");
        student.setRole(UserRole.STUDENT);
        student.setWallet(0L);
        student = userRepo.save(student);
        studentUserId = student.getId();

        Course course = new Course();
        course.setTutorId(tutorUserId);
        course.setName("Existing Course");
        course.setSubject(11);
        course.setPrice(500);
        course.setActive(true);
        course = courseRepo.save(course);
        savedCourseId = course.getId();
    }

    private CourseReq buildReq(Long tutorId, String name, int subject, int price, boolean active) {
        CourseReq req = new CourseReq();
        req.setTutorId(tutorId);
        req.setName(name);
        req.setSubject(subject);
        req.setPrice(price);
        req.setActive(active);
        return req;
    }

    // ===================== sendCourses =====================

    @Test
    void sendCourses_validRequest_returnsTrue() {
        CourseReq req = buildReq(tutorUserId, "New Course", 21, 800, true);
        assertTrue(courseService.sendCourses(req));
    }

    @Test
    void sendCourses_nullTutorId_returnsFalse() {
        CourseReq req = buildReq(null, "Course", 11, 500, true);
        assertFalse(courseService.sendCourses(req));
    }

    @Test
    void sendCourses_invalidSubject_returnsFalse() {
        CourseReq req = buildReq(tutorUserId, "Course", 99, 500, true);
        assertFalse(courseService.sendCourses(req));
    }

    @Test
    void sendCourses_zeroPrice_returnsFalse() {
        CourseReq req = buildReq(tutorUserId, "Course", 11, 0, true);
        assertFalse(courseService.sendCourses(req));
    }

    @Test
    void sendCourses_nonTutorUser_returnsFalse() {
        CourseReq req = buildReq(studentUserId, "Course", 11, 500, true);
        assertFalse(courseService.sendCourses(req));
    }

    // ===================== getCourseById =====================

    @Test
    void getCourseById_existingId_returnsCourseResp() {
        CourseResp resp = courseService.getCourseById(savedCourseId);
        assertNotNull(resp);
        assertEquals(savedCourseId, resp.getId());
        assertEquals("Existing Course", resp.getName());
    }

    @Test
    void getCourseById_nonExistingId_returnsNull() {
        CourseResp resp = courseService.getCourseById(999999L);
        assertNull(resp);
    }

    // ===================== deleteById =====================

    @Test
    void deleteById_existingId_returnsTrue() {
        assertTrue(courseService.deleteById(savedCourseId));
        assertFalse(courseRepo.existsById(savedCourseId));
    }

    @Test
    void deleteById_nonExistingId_returnsFalse() {
        assertFalse(courseService.deleteById(999999L));
    }
}
