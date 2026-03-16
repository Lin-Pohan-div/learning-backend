package com.learning.api.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.learning.api.Spec.CourseSpec;
import com.learning.api.dto.CourseSearchDTO;
import com.learning.api.entity.Course;
import com.learning.api.entity.TutorSchedule;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.TutorScheduleRepo;

@Controller
public class CourseViewController {

    @Autowired
    private CourseRepo courseRepo;

    @GetMapping("/view/courses")
    public String searchCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) Integer subjectCategory, // 大類別
            @RequestParam(required = false) Integer subject,         // 具體科目
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) Integer weekday, // 新增
            @RequestParam(required = false) String timeSlot,    // 新增
            Model model) {

        Pageable pageable = PageRequest.of(page, 10);

        // 1. 執行帶條件的查詢
        Page<Course> coursePage = courseRepo.findAll(
            CourseSpec.filterCourses(teacherName, courseName, subjectCategory, subject, priceRange, weekday, timeSlot),
            pageable
        );

        // 2. 將 Entity 轉換為 DTO
        Page<CourseSearchDTO> dtoPage = coursePage.map(course -> new CourseSearchDTO(
            course.getId(),
            course.getTutor().getId(),             // tutorId，用於跳轉老師頁面
            course.getTutor().getUser().getName(), // 從 Tutor -> User 拿姓名
            course.getTutor().getAvatar(),      // 拿頭像
            course.getTutor().getTitle(),       // 拿標題
            course.getName(),
            course.getSubject(),
            course.getDescription(),
            course.getPrice()
        ));

        // 3. 傳遞給前端
        model.addAttribute("courses", dtoPage.getContent());
        model.addAttribute("totalPages", dtoPage.getTotalPages());
        model.addAttribute("currentPage", page);

        // 保留搜尋狀態
        model.addAttribute("currentTeacherName", teacherName);
        model.addAttribute("currentCourseName", courseName);
        model.addAttribute("currentSubjectCategory", subjectCategory);
        model.addAttribute("currentSubject", subject);
        model.addAttribute("currentPriceRange", priceRange);
        model.addAttribute("currentWeekday", weekday);
        model.addAttribute("currentHour", timeSlot);

        return "courseList5";
    }
    @Autowired 
    private TutorScheduleRepo scheduleRepo;
    
    @GetMapping("/view/teacher_schedule/{teacherId}")
    @ResponseBody
    public Map<Integer, List<Integer>> getTeacherSchedule(@PathVariable Long teacherId) {
        // 抓取該老師所有排班：findByUserId (根據您 Repo 的命名)
        List<TutorSchedule> schedules = scheduleRepo.findByTutorId(teacherId);
        
        // 整理成 { 星期: [小時1, 小時2...] } 的格式回傳
        return schedules.stream()
            .collect(Collectors.groupingBy(
                TutorSchedule::getWeekday,
                Collectors.mapping(TutorSchedule::getHour, Collectors.toList())
            ));
    }
}

