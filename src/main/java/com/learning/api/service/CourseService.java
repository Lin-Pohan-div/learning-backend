package com.learning.api.service;

import com.learning.api.dto.*;
import com.learning.api.entity.*;
import com.learning.api.repo.UserRepository;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.OrderRepository;
import com.learning.api.repo.BookingRepository;
import com.learning.api.repo.LessonFeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
<<<<<<< HEAD
import java.util.Optional;
import java.util.Set;
=======
import java.util.stream.Collectors;
>>>>>>> 8192b919c2562565f031421a6f99a8b9395b67f2

@Service
public class CourseService {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CourseRepo courseRepo;

<<<<<<< HEAD
    private static final Set<Integer> VALID_SUBJECTS = Set.of(11, 12, 13, 21, 22, 23, 31);

    // POST 建立課程
    public boolean sendCourses(CourseReq courseReq) {
=======
    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private LessonFeedbackRepository feedbackRepo;

    // bookingReq.getUserId() -> 這是前端送 id
    public boolean sendCourses(CourseReq courseReq){
>>>>>>> 8192b919c2562565f031421a6f99a8b9395b67f2

        if (courseReq == null) {
            System.out.println("courseReq is null");
            return false;
        }

        // check null
        if (courseReq.getTutorId() == null || courseReq.getName() == null ||
            courseReq.getSubject() == null || courseReq.getPrice() == null ||
            courseReq.getActive() == null) return false;

        if (courseReq.getName().trim().isEmpty()) {
            System.out.println("name is empty");
            return false;
        }

        if (courseReq.getPrice() <= 0) {
            System.out.println("price is wrong");
            return false;
        }

        // subject: 11低年級 12中年級 13高年級 21GEPT 22YLE 23國中先修 31其他
        if (!VALID_SUBJECTS.contains(courseReq.getSubject())) return false;

        // level 若有填寫則需在 1-5 之間
        if (courseReq.getLevel() != null && (courseReq.getLevel() < 1 || courseReq.getLevel() > 5)) return false;

        // 確認老師存在且 role == 2
        User tutor = userRepo.findById(courseReq.getTutorId()).orElse(null);
        if (tutor == null) {
            System.out.println("tutor is null");
            return false;
        }
        if (tutor.getRole() != 2) {
            System.out.println("user isn't tutor");
            return false;
        }

        courseRepo.save(buildCourses(courseReq));
        return true;
    }

    public List<CourseResp> getAllCourses() {
        return courseRepo.findAll().stream()
                .map(this::buildCourseResp)
                .collect(Collectors.toList());
    }

    public CourseResp getCourseById(Long courseId) {
        Course course = courseRepo.findById(courseId).orElse(null);
        if (course == null) return null;
        return buildCourseResp(course);
    }

    private CourseResp buildCourseResp(Course course) {
        List<Long> orderIds = orderRepo.findByCourseId(course.getId()).stream()
                .map(Order::getId)
                .collect(Collectors.toList());

        List<Long> bookingIds = orderIds.isEmpty()
                ? List.of()
                : bookingRepo.findByOrderIdIn(orderIds).stream()
                        .map(Bookings::getId)
                        .collect(Collectors.toList());

        List<LessonFeedback> feedbacks = bookingIds.isEmpty()
                ? List.of()
                : feedbackRepo.findByBookingIdIn(bookingIds);

        Double avgRating = bookingIds.isEmpty()
                ? null
                : feedbackRepo.findAverageRatingByBookingIdIn(bookingIds);

        CourseResp resp = new CourseResp();
        resp.setId(course.getId());
        resp.setTutorId(course.getTutorId());
        resp.setName(course.getName());
        resp.setSubject(course.getSubject());
        resp.setLevel(course.getLevel());
        resp.setDescription(course.getDescription());
        resp.setPrice(course.getPrice());
        resp.setActive(course.getActive());
        resp.setAvgRating(avgRating);
        resp.setFeedbacks(feedbacks.stream()
                .map(f -> new CourseResp.FeedbackItem(f.getRating(), f.getComment()))
                .collect(Collectors.toList()));
        return resp;
    }

    private Course buildCourses(CourseReq courseReq){

    // GET 單筆課程
    public Optional<Course> findById(Long id) {
        return courseRepo.findById(id);
    }

    // GET 老師所有課程（不分上下架）
    public List<Course> findByTutorId(Long tutorId) {
        return courseRepo.findByTutorId(tutorId);
    }

    // GET 老師已上架課程
    public List<Course> findByTutorIdActive(Long tutorId) {
        return courseRepo.findByTutorIdAndActive(tutorId, true);
    }

    // PUT 更新課程
    public Optional<Course> updateCourse(Long id, CourseReq req) {
        return courseRepo.findById(id).map(existing -> {
            validateCourseReq(req);
            existing.setName(req.getName().trim());
            existing.setSubject(req.getSubject());
            /* if (req.getLevel() != null) existing.setLevel(req.getLevel()); */
            if (req.getDescription() != null) existing.setDescription(req.getDescription());
            existing.setPrice(req.getPrice());
            existing.setActive(req.getActive());
            return courseRepo.save(existing);
        });
    }

    // DELETE 刪除課程
    public boolean deleteById(Long id) {
        if (courseRepo.existsById(id)) {
            courseRepo.deleteById(id);
            return true;
        }
        return false;
    }

    // 驗證課程資料（不含 tutor 角色驗證，供 update 使用）
    private void validateCourseReq(CourseReq req) {
        if (req == null) throw new IllegalArgumentException("課程資料不能為空");
        if (req.getName() == null || req.getName().trim().isEmpty())
            throw new IllegalArgumentException("課程標題不能為空");
        if (req.getSubject() == null || !VALID_SUBJECTS.contains(req.getSubject()))
            throw new IllegalArgumentException("科目代碼無效，有效值為 11/12/13/21/22/23/31");
        if (req.getPrice() == null || req.getPrice() <= 0)
            throw new IllegalArgumentException("定價必須大於 0");
        if (req.getActive() == null)
            throw new IllegalArgumentException("上架狀態不能為空");
        if (req.getLevel() != null && (req.getLevel() < 1 || req.getLevel() > 5))
            throw new IllegalArgumentException("難易度必須在 1-5 之間");
    }

    private Course buildCourses(CourseReq courseReq) {
        Course course = new Course();
        course.setTutorId(courseReq.getTutorId());
        course.setName(courseReq.getName().trim());
        course.setSubject(courseReq.getSubject());
        /* course.setLevel(courseReq.getLevel()); */
        course.setDescription(courseReq.getDescription());
        course.setPrice(courseReq.getPrice());
        course.setActive(courseReq.getActive());
        return course;
    }
}
