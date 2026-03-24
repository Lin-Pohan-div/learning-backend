package com.learning.api.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
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
import com.learning.api.repo.TutorScheduleRepo;
import com.learning.api.service.CourseService;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CourseViewController {

    private final CourseService courseService;
    private final TutorScheduleRepo scheduleRepo;

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

        Pageable pageable = PageRequest.of(page, 10);

        Page<Course> coursePage = courseService.searchCourses(
                CourseSpec.filterCourses(teacherName, courseName, subjectCategory, subject, priceRange, weekday, timeSlot),
                pageable
        );

        Page<CourseSearchDTO> dtoPage = coursePage.map(course -> {
            List<String> slots = course.getTutor().getSchedules().stream()
                    .filter(s -> s.getIsAvailable() != null && s.getIsAvailable())
                    .map(s -> {
                        String period = "morning";
                        if (s.getHour() >= 13 && s.getHour() < 17) period = "afternoon";
                        else if (s.getHour() >= 17) period = "evening";
                        return s.getWeekday() + "-" + period;
                    })
                    .collect(Collectors.toList());

            return new CourseSearchDTO(
                    course.getId(),
                    course.getTutor().getId(),
                    course.getTutor().getUser().getName(),
                    course.getTutor().getAvatar(),
                    course.getTutor().getTitle(),
                    course.getName(),
                    course.getSubject(),
                    course.getDescription(),
                    course.getPrice(),
                    slots
            );
        });

        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/api/view/teacher_schedule/{teacherId}")
    public Map<Integer, List<Integer>> getTeacherSchedule(@PathVariable Long teacherId) {
        List<TutorSchedule> schedules = scheduleRepo.findByTutorId(teacherId);

        return schedules.stream()
            .collect(Collectors.groupingBy(
                TutorSchedule::getWeekday,
                Collectors.mapping(TutorSchedule::getHour, Collectors.toList())
            ));
    }
}
