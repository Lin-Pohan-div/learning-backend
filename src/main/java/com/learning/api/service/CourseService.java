package com.learning.api.service;

import com.learning.api.dto.*;
import com.learning.api.entity.*;
import com.learning.api.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CourseRepo courseRepo;

    private static final List<Integer> VALID_SUBJECTS = List.of(11, 12, 13, 21, 22, 23, 31);

    public boolean sendCourses(CourseReq courseReq){

        if (courseReq == null) {
            System.out.println("courseReq is null");
            return false;
        }

        // check null
        if (courseReq.getTutorId() == null || courseReq.getName() == null ||
            courseReq.getSubject() == null ||
                courseReq.getPrice() == null || courseReq.getActive() == null) return false;

        if (courseReq.getName().trim().isEmpty()) {
            System.out.println("name is empty");
            return false;
        }

        if (courseReq.getPrice() <= 0) {
            System.out.println("price is wrong");
            return false;
        }

        // 科目代碼：11低年級 12中年級 13高年級 21GEPT 22YLE 23國中先修 31其他
        if (!VALID_SUBJECTS.contains(courseReq.getSubject())) return false;

        // member existsById
        User tutor = userRepo.findById(courseReq.getTutorId()).orElse(null);
        if ( tutor == null ) {
            System.out.println("tutor is null");
            return false;
        }

        // 只有老師可以新增課程
        if (tutor.getRole() != 2) {
            System.out.println("user isn't tutor");
            return false;
        }

        courseRepo.save(buildCourses(courseReq));
        return true;
    }

    public List<Course> getAllCourses() {
        return courseRepo.findAll();
    }

    public List<Course> getCoursesByTutor(Long tutorId) {
        return courseRepo.findByTutorId(tutorId);
    }

    public boolean updateCourse(Long id, CourseReq courseReq) {
        Course course = courseRepo.findById(id).orElse(null);
        if (course == null) return false;

        if (courseReq.getName() != null && !courseReq.getName().trim().isEmpty())
            course.setName(courseReq.getName().trim());
        if (courseReq.getSubject() != null) {
            if (!VALID_SUBJECTS.contains(courseReq.getSubject())) return false;
            course.setSubject(courseReq.getSubject());
        }
        if (courseReq.getDescription() != null) course.setDescription(courseReq.getDescription());
        if (courseReq.getPrice() != null && courseReq.getPrice() > 0) course.setPrice(courseReq.getPrice());
        if (courseReq.getActive() != null) course.setActive(courseReq.getActive());

        courseRepo.save(course);
        return true;
    }

    public boolean deleteCourse(Long id) {
        if (!courseRepo.existsById(id)) return false;
        courseRepo.deleteById(id);
        return true;
    }

    private Course buildCourses(CourseReq courseReq){
        Course course = new Course();
        course.setTutorId(courseReq.getTutorId());
        course.setName(courseReq.getName().trim());
        course.setSubject(courseReq.getSubject());
        course.setDescription(courseReq.getDescription());
        course.setPrice(courseReq.getPrice());
        course.setActive(courseReq.getActive());
        return course;
    }
}
