package com.learning.api.service;


import com.learning.api.dto.course.CourseReq;
import com.learning.api.dto.course.CourseResp;
import com.learning.api.entity.*;
import com.learning.api.enums.UserRole;
import com.learning.api.repo.UserRepository;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.OrderRepository;
import com.learning.api.repo.BookingRepository;
import com.learning.api.repo.LessonFeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final UserRepository userRepo;
    private final CourseRepo courseRepo;
    private final OrderRepository orderRepo;
    private final BookingRepository bookingRepo;
    private final LessonFeedbackRepository feedbackRepo;

    private static final Set<Integer> VALID_SUBJECTS = Set.of(11, 12, 13, 21, 22, 23, 31);

    // POST 建立課程
    public boolean sendCourses(CourseReq courseReq) {

        if (courseReq == null) return false;

        if (courseReq.getTutorId() == null || courseReq.getName() == null ||
            courseReq.getSubject() == null || courseReq.getPrice() == null ||
            courseReq.getActive() == null) return false;

        if (courseReq.getName().trim().isEmpty()) return false;

        if (courseReq.getPrice() <= 0) return false;

        if (!VALID_SUBJECTS.contains(courseReq.getSubject())) return false;

        if (courseReq.getLevel() != null && (courseReq.getLevel() < 1 || courseReq.getLevel() > 5)) return false;

        User tutor = userRepo.findById(courseReq.getTutorId()).orElse(null);
        if (tutor == null || tutor.getRole() != UserRole.TUTOR) return false;

        courseRepo.save(buildCourses(courseReq));
        return true;
    }

    /**
     * 批量載入所有課程及其關聯資料，避免 N+1 查詢。
     * 原實作：每筆課程各觸發 orders / bookings / feedbacks 三次 DB 查詢。
     * 新實作：全表各查一次，再於記憶體中映射，DB 查詢固定為 4 次（courses / orders / bookings / feedbacks）。
     */
    public List<CourseResp> getAllCourses() {
        List<Course> courses = courseRepo.findAll();
        if (courses.isEmpty()) return List.of();

        // ① 批量取得所有 orders（依 courseId）
        List<Long> courseIds = courses.stream().map(Course::getId).collect(Collectors.toList());
        List<Order> allOrders = orderRepo.findByCourseIdIn(courseIds);

        // courseId → orderIds
        Map<Long, List<Long>> orderIdsByCourse = allOrders.stream()
                .collect(Collectors.groupingBy(
                        Order::getCourseId,
                        Collectors.mapping(Order::getId, Collectors.toList())
                ));

        // ② 批量取得所有 bookings（依 orderId）
        List<Long> allOrderIds = allOrders.stream().map(Order::getId).collect(Collectors.toList());
        List<Bookings> allBookings = allOrderIds.isEmpty()
                ? List.of()
                : bookingRepo.findByOrderIdIn(allOrderIds);

        // orderId → bookingIds
        Map<Long, List<Long>> bookingIdsByOrder = allBookings.stream()
                .collect(Collectors.groupingBy(
                        Bookings::getOrderId,
                        Collectors.mapping(Bookings::getId, Collectors.toList())
                ));

        // ③ 批量取得所有 feedbacks（依 bookingId）
        List<Long> allBookingIds = allBookings.stream().map(Bookings::getId).collect(Collectors.toList());
        List<LessonFeedback> allFeedbacks = allBookingIds.isEmpty()
                ? List.of()
                : feedbackRepo.findByBookingIdIn(allBookingIds);

        // bookingId → feedbacks
        Map<Long, List<LessonFeedback>> feedbacksByBooking = allFeedbacks.stream()
                .collect(Collectors.groupingBy(LessonFeedback::getBookingId));

        // ④ 組裝每筆課程的回應（pure in-memory，不再打 DB）
        return courses.stream().map(course -> {
            List<Long> courseOrderIds = orderIdsByCourse.getOrDefault(course.getId(), List.of());

            List<Long> courseBookingIds = courseOrderIds.stream()
                    .flatMap(oid -> bookingIdsByOrder.getOrDefault(oid, List.of()).stream())
                    .collect(Collectors.toList());

            List<LessonFeedback> courseFeedbacks = courseBookingIds.stream()
                    .flatMap(bid -> feedbacksByBooking.getOrDefault(bid, List.of()).stream())
                    .collect(Collectors.toList());

            Double avgRating = courseFeedbacks.isEmpty() ? null
                    : courseFeedbacks.stream().mapToInt(LessonFeedback::getRating).average().orElse(0.0);

            return buildCourseResp(course, courseFeedbacks, avgRating);
        }).collect(Collectors.toList());
    }

    public CourseResp getCourseById(Long courseId) {
        Course course = courseRepo.findById(courseId).orElse(null);
        if (course == null) return null;

        // 單筆查詢，N+1 影響極小，維持原本邏輯即可
        List<Long> orderIds = orderRepo.findByCourseId(course.getId()).stream()
                .map(Order::getId).collect(Collectors.toList());

        List<Long> bookingIds = orderIds.isEmpty() ? List.of()
                : bookingRepo.findByOrderIdIn(orderIds).stream()
                        .map(Bookings::getId).collect(Collectors.toList());

        List<LessonFeedback> feedbacks = bookingIds.isEmpty() ? List.of()
                : feedbackRepo.findByBookingIdIn(bookingIds);

        Double avgRating = bookingIds.isEmpty() ? null
                : feedbackRepo.findAverageRatingByBookingIdIn(bookingIds);

        return buildCourseResp(course, feedbacks, avgRating);
    }

    // GET 課程搜尋（分頁 + 篩選）
    public Page<Course> searchCourses(Specification<Course> spec, Pageable pageable) {
        return courseRepo.findAll(spec, pageable);
    }

    // GET 單筆課程（回傳 entity）
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

    private CourseResp buildCourseResp(Course course, List<LessonFeedback> feedbacks, Double avgRating) {
        CourseResp resp = new CourseResp();
        resp.setId(course.getId());
        resp.setTutorId(course.getTutorId());
        resp.setName(course.getName());
        resp.setSubject(course.getSubject());
        resp.setDescription(course.getDescription());
        resp.setPrice(course.getPrice());
        resp.setActive(course.getActive());
        resp.setAvgRating(avgRating);
        resp.setFeedbacks(feedbacks.stream()
                .map(f -> new CourseResp.FeedbackItem(f.getRating(), f.getComment()))
                .collect(Collectors.toList()));
        return resp;
    }

    private Course buildCourses(CourseReq courseReq) {
        Course course = new Course();
        //set
//        course.setTutorId(courseReq.getTutorId());
        course.setName(courseReq.getName().trim());
        course.setSubject(courseReq.getSubject());
        if (courseReq.getDescription() != null) course.setDescription(courseReq.getDescription());
//        course.setLevel(courseReq.getLevel());
        course.setDescription(courseReq.getDescription());
        course.setPrice(courseReq.getPrice());
//        course.setActive(courseReq.getActive());

        return course;
    }
}
