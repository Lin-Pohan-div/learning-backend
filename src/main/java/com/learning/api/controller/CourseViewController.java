package com.learning.api.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learning.api.Spec.CourseSpec;
import com.learning.api.dto.CourseSearchDTO;
import com.learning.api.entity.Course;
import com.learning.api.entity.TutorSchedule;
import com.learning.api.repo.CourseRepo;
/* import com.learning.api.repo.TutorScheduleRepo; */

// 1. 改為 @RestController 以便直接回傳 JSON 數據
// 2. 加上 @CrossOrigin 解決跨域問題
@RestController
@CrossOrigin(origins = "*")
public class CourseViewController {

    @Autowired
    private CourseRepo courseRepo;

/*     @Autowired 
    private TutorScheduleRepo scheduleRepo; */

    /**
     * 修改為回傳 ResponseEntity，以便前端獲取分頁 JSON 數據
     */
    @GetMapping("/api/view/courses")
    public ResponseEntity<Page<CourseSearchDTO>> searchCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) Integer subjectCategory,
            @RequestParam(required = false) Integer subject,
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) Integer weekday,
            @RequestParam(required = false) String timeSlot) {

        // 設定分頁大小，此處維持 10
        Pageable pageable = PageRequest.of(page, 10);

        // 1. 執行查詢
        Page<Course> coursePage = courseRepo.findAll(
            CourseSpec.filterCourses(teacherName, courseName, subjectCategory, subject, priceRange, weekday, timeSlot),
            pageable
        );

        // 2. 轉換為 DTO (這部分邏輯不變)
        Page<CourseSearchDTO> dtoPage = coursePage.map(course -> new CourseSearchDTO(
            course.getId(),
            course.getTutor().getId(),
            course.getTutor().getUser().getName(),
            course.getTutor().getAvatar(),
            course.getTutor().getTitle(),
            course.getName(),
            course.getSubject(),
            course.getDescription(),
            course.getPrice()
        ));

        // 3. 直接回傳 DTO 分頁物件
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * 獲取老師課表，同樣支援跨域
     */
/*     @GetMapping("/api/view/teacher_schedule/{teacherId}")
    public Map<Integer, List<Integer>> getTeacherSchedule(@PathVariable Long teacherId) {
        List<TutorSchedule> schedules = scheduleRepo.findByTutorId(teacherId);
        
        return schedules.stream()
            .collect(Collectors.groupingBy(
                TutorSchedule::getWeekday,
                Collectors.mapping(TutorSchedule::getHour, Collectors.toList())
            ));
    } */
}