package com.learning.api.service;

import com.learning.api.entity.Course;
import com.learning.api.entity.User;
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
class TeacherCourseServiceTest {

    @Autowired
    private TeacherCourseService teacherCourseService;

    @Autowired
    private UserRepository userRepo;

    private Long tutorUserId;

    @BeforeEach
    void setUp() {
        User tutor = new User();
        tutor.setName("Teacher Course Svc Tutor");
        tutor.setEmail("teachercoursesvc@example.com");
        tutor.setPassword("hashed");
        tutor.setRole(UserRole.TUTOR);
        tutor.setWallet(0L);
        tutor = userRepo.save(tutor);
        tutorUserId = tutor.getId();
    }

    private Course buildCourse(Long tutorId, Integer subject, Integer price, String description) {
        Course course = new Course();
        course.setTutorId(tutorId);
        course.setSubject(subject);
        course.setPrice(price);
        course.setName("Test Course");
        course.setActive(true);
        if (description != null) {
            course.setDescription(description);
        }
        return course;
    }

    @Test
    void addCourse_validRequest_returnsTrue() {
        Course course = buildCourse(tutorUserId, 11, 500, null);
        assertTrue(teacherCourseService.addCourse(course));
    }

    @Test
    void addCourse_nullTutorId_returnsFalse() {
        Course course = buildCourse(null, 11, 500, null);
        assertFalse(teacherCourseService.addCourse(course));
    }

    @Test
    void addCourse_nullSubject_returnsFalse() {
        Course course = buildCourse(tutorUserId, null, 500, null);
        assertFalse(teacherCourseService.addCourse(course));
    }

    @Test
    void addCourse_zeroPrice_returnsFalse() {
        Course course = buildCourse(tutorUserId, 11, 0, null);
        assertFalse(teacherCourseService.addCourse(course));
    }

    @Test
    void addCourse_longDescription_returnsFalse() {
        String longDesc = "A".repeat(1001);
        Course course = buildCourse(tutorUserId, 11, 500, longDesc);
        assertFalse(teacherCourseService.addCourse(course));
    }
}
