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

import com.learning.api.dto.CourseSearchDTO;
import com.learning.api.entity.Course;
import com.learning.api.entity.TutorSchedule;
import com.learning.api.repo.CourseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

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
        course.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

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
        if (dto.getIsActive()      != null) course.setIsActive(dto.getIsActive());

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

    public List<CourseSearchDTO> getAllCourseCards() {
        // 1. 撈出所有課程，並篩選出 isActive 為 true 的
        // 注意：根據你的 Entity，欄位名稱是 isActive
        List<Course> courses = courseRepo.findAll().stream()
                .filter(c -> c.getIsActive() != null && c.getIsActive())
                .collect(Collectors.toList());

        // 2. 轉換為 DTO
        return courses.stream().map(course -> {
            CourseSearchDTO dto = new CourseSearchDTO();
            dto.setId(course.getId());
            dto.setTutorId(course.getTutor().getId());
            dto.setTeacherName(course.getTutor().getUser().getName());
            dto.setAvatarUrl(course.getTutor().getAvatar());
            dto.setTitle(course.getTutor().getTitle());
            dto.setCourseName(course.getName());
            dto.setSubject(course.getSubject());
            dto.setDescription(course.getDescription());
            dto.setPrice(course.getPrice());

            // 🌟 3. 處理時段：對齊你的 TutorSchedule 欄位
            if (course.getTutor().getSchedules() != null) {
                List<String> slots = course.getTutor().getSchedules().stream()
                        .filter(TutorSchedule::getIsAvailable) // 只抓開放的時段
                        .map(this::convertToSlotTag)
                        .collect(Collectors.toList());
                dto.setAvailableSlots(slots);
            }

            return dto;
        }).collect(Collectors.toList());
    }

    // 🌟 依照你的 Entity 修正轉換邏輯
    private String convertToSlotTag(TutorSchedule s) {
        String period = "morning";
        int hour = s.getHour(); // 取得 9-21 的數字

        // 對齊你前端 explore.html 的定義：
        // morning: 09:00 - 13:00 / afternoon: 13:00 - 17:00 / evening: 17:00 - 21:00
        if (hour >= 13 && hour < 17) period = "afternoon";
        else if (hour >= 17) period = "evening";

        // 格式範例: "1-morning" (星期一上午)
        return s.getWeekday() + "-" + period;
    }
}
