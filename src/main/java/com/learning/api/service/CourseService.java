package com.learning.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learning.api.dto.CourseDTO;
import com.learning.api.dto.CourseReq;
import com.learning.api.entity.Course;
import com.learning.api.entity.Tutor;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.TutorRepo;

@Service
public class CourseService {

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private TutorRepo tutorRepo;

    // ── 查：所有課程 ──────────────────────────────────────────────────

    public List<CourseDTO> getCoursesByTutorId(Long tutorId) {
        validateTutorExists(tutorId);
        return courseRepo.findByTutorId(tutorId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ── 查：單一課程 ──────────────────────────────────────────────────

    public CourseDTO getCourse(Long tutorId, Long courseId) {
        Course course = findCourseOrThrow(courseId);
        validateCourseOwnership(course, tutorId);
        return toDTO(course);
    }

    // ── 增 ────────────────────────────────────────────────────────────

    @Transactional
    public CourseDTO createCourse(Long tutorId, CourseReq dto) {
        Tutor tutor = tutorRepo.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("找不到老師 id=" + tutorId));

        Course course = new Course();
        course.setTutor(tutor);
        course.setName(dto.getName());
        course.setSubject(dto.getSubject());
        course.setDescription(dto.getDescription());
        course.setPrice(dto.getPrice());
        course.setIsActive(dto.getActive() != null ? dto.getActive() : true);

        return toDTO(courseRepo.save(course));
    }

    // ── 修 ────────────────────────────────────────────────────────────

    @Transactional
    public CourseDTO updateCourse(Long tutorId, Long courseId, CourseReq dto) {
        Course course = findCourseOrThrow(courseId);
        validateCourseOwnership(course, tutorId);

        if (dto.getName()        != null) course.setName(dto.getName());
        if (dto.getSubject()     != null) course.setSubject(dto.getSubject());
        if (dto.getDescription() != null) course.setDescription(dto.getDescription());
        if (dto.getPrice()       != null) course.setPrice(dto.getPrice());
        if (dto.getActive()      != null) course.setIsActive(dto.getActive());

        return toDTO(courseRepo.save(course));
    }

    // ── 刪 ────────────────────────────────────────────────────────────

    @Transactional
    public void deleteCourse(Long tutorId, Long courseId) {
        Course course = findCourseOrThrow(courseId);
        validateCourseOwnership(course, tutorId);
        courseRepo.delete(course);
    }

    // ── 私有輔助方法 ──────────────────────────────────────────────────

    /** Entity → DTO，只取純資料欄位，切斷所有 entity 關聯 */
    private CourseDTO toDTO(Course course) {
        return new CourseDTO(
            course.getId(),
            course.getName(),
            course.getSubject(),
            course.getDescription(),
            course.getPrice(),
            course.getIsActive()
        );
    }

    private void validateTutorExists(Long tutorId) {
        if (!tutorRepo.existsById(tutorId)) {
            throw new RuntimeException("找不到老師 id=" + tutorId);
        }
    }

    private Course findCourseOrThrow(Long courseId) {
        return courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("找不到課程 id=" + courseId));
    }

    private void validateCourseOwnership(Course course, Long tutorId) {
        if (!course.getTutor().getId().equals(tutorId)) {
            throw new SecurityException("此課程不屬於老師 id=" + tutorId);
        }
    }
}