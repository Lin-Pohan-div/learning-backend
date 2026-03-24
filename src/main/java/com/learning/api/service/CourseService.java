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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private static final Set<Integer> VALID_SUBJECTS = Set.of(11, 12, 13, 21, 22, 23, 31);

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
