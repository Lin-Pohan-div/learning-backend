package com.learning.api.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.learning.api.entity.Course;
import com.learning.api.entity.Review;
import com.learning.api.entity.Tutor;
import com.learning.api.entity.TutorSchedule;
import com.learning.api.service.TutorService;

@Controller
@RequestMapping("/tutor")
public class TutorController {

    @Autowired
    private TutorService tutorService; // 假設你已寫好對應的 Service

    /**
     * 展示老師個人頁面
     * @param id 老師的 User ID
     */
    @GetMapping("/{id}")
    public String getTutorProfile(@PathVariable Long id, 
    							  @RequestParam(required = false) Long courseId, 
    							  Model model) {
        
        // 1. 取得老師核心資料 (包含 user 資料)
        Tutor tutor = tutorService.findTutorById(id);
        
        if (tutor == null) {
            return "error/404"; // 若找不到老師則導向錯誤頁面
        }

        // 2. 取得老師的課表 (tutor_schedules)
        List<TutorSchedule> schedules = tutorService.findSchedulesByTutorId(id);
        
        //
        List<Course> courses = tutorService.findCoursesByTutorId(id);
        
        // 預設選擇第一堂課
        Course selectedCourse = null;
        if (courseId != null) {
            selectedCourse = tutorService.findCourseById(courseId);
        } else if (!courses.isEmpty()) {
            selectedCourse = courses.get(0);
        }

        // 取得該課程的評價
        List<Review> reviews = (selectedCourse != null) ? 
                               tutorService.findReviewsByCourseId(selectedCourse.getId()) : 
                               new ArrayList<>();

        // 4. 計算平均評分
        double avgRating = reviews.stream()
                                  .mapToInt(Review::getRating)
                                  .average()
                                  .orElse(0.0);

        // 將資料放入 Model 傳遞給前端 Thymeleaf 模板
        model.addAttribute("tutor", tutor);
        model.addAttribute("courses", courses);
        model.addAttribute("selectedCourse", selectedCourse);
        model.addAttribute("reviews", reviews);
        model.addAttribute("schedules", schedules);
        model.addAttribute("avgRating", String.format("%.1f", avgRating));

        return "tutorProfile3"; // 回傳 templates/tutor_profile.html
    }
//    // AJAX 非同步更新
//    @GetMapping("/api/course/{courseId}")
//    @ResponseBody // 確保回傳 JSON 而非 HTML 頁面
//    public Map<String, Object> getCourseDetailsApi(@PathVariable Long courseId) {
//        Map<String, Object> response = new HashMap<>();
//        
//        Course course = tutorService.findCourseById(courseId);
//        List<Review> reviews = tutorService.findReviewsByCourseId(courseId);
//        
//        response.put("course", course);
//        response.put("reviews", reviews);
//        
//        return response;
//    }
}

