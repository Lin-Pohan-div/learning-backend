package com.learning.api.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learning.api.dto.CourseDTO;
import com.learning.api.dto.CourseReq;
import com.learning.api.dto.CourseSearchDTO;
import com.learning.api.dto.course.CourseResp;
import com.learning.api.entity.Bookings;
import com.learning.api.entity.Course;
import com.learning.api.entity.LessonFeedback;
import com.learning.api.entity.Order;
import com.learning.api.entity.Tutor;
import com.learning.api.entity.TutorSchedule;
import com.learning.api.repo.BookingRepository;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.LessonFeedbackRepository;
import com.learning.api.repo.OrderRepository;
import com.learning.api.repo.TutorRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseService {

    private static final Set<Integer> VALID_SUBJECTS = Set.of(11, 12, 13, 21, 22, 23, 31);

    private final TutorRepo tutorRepo;
    private final CourseRepo courseRepo;
    private final OrderRepository orderRepo;
    private final BookingRepository bookingRepo;
    private final LessonFeedbackRepository feedbackRepo;

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
        if (dto.getName() != null) course.setName(dto.getName());
        if (dto.getSubject() != null) course.setSubject(dto.getSubject());
        if (dto.getDescription() != null) course.setDescription(dto.getDescription());
        if (dto.getPrice() != null) course.setPrice(dto.getPrice());
        course.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        return toDTO(courseRepo.save(course));
    }

    // ── 查：課程詳細（含評分）────────────────────────────────────────

    public CourseResp getCourseById(Long courseId) {
        Course course = courseRepo.findById(courseId).orElse(null);
        if (course == null) return null;

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

    // ── 搜尋（分頁）──────────────────────────────────────────────────

    public Page<Course> searchCourses(Specification<Course> spec, Pageable pageable) {
        return courseRepo.findAll(spec, pageable);
    }

    // ── 查：entity 單筆 / 老師全部 / 老師上架 ──────────────────────

    public Optional<Course> findById(Long id) {
        return courseRepo.findById(id);
    }

    public List<Course> findByTutorId(Long tutorId) {
        return courseRepo.findByTutorId(tutorId);
    }

    public List<Course> findByTutorIdActive(Long tutorId) {
        return courseRepo.findByTutorIdAndIsActive(tutorId, true);
    }

    // ── 修（新版，回傳 entity）────────────────────────────────────────

    public Optional<Course> updateCourse(Long id, com.learning.api.dto.course.CourseReq req) {
        return courseRepo.findById(id).map(existing -> {
            validateNewStyleCourseReq(req);
            existing.setName(req.getName().trim());
            existing.setSubject(req.getSubject());
            if (req.getDescription() != null) existing.setDescription(req.getDescription());
            existing.setPrice(req.getPrice());
            existing.setIsActive(req.getActive());
            return courseRepo.save(existing);
        });
    }

    // ── 刪（新版，回傳 boolean）────────────────────────────────────────

    public boolean deleteById(Long id) {
        if (courseRepo.existsById(id)) {
            courseRepo.deleteById(id);
            return true;
        }
        return false;
    }

    // ── 修（舊版，依 tutorId 驗權，回傳 DTO）──────────────────────────

    @Transactional
    public CourseDTO updateCourse(Long tutorId, Long courseId, CourseReq dto) {
        Course course = findCourseOrThrow(courseId);
        validateCourseOwnership(course, tutorId);

        if (dto.getName()        != null) course.setName(dto.getName());
        if (dto.getSubject()     != null) course.setSubject(dto.getSubject());
        if (dto.getDescription() != null) course.setDescription(dto.getDescription());
        if (dto.getPrice()       != null) course.setPrice(dto.getPrice());
        if (dto.getIsActive()    != null) course.setIsActive(dto.getIsActive());

        return toDTO(courseRepo.save(course));
    }

    // ── 刪（舊版，依 tutorId 驗權）────────────────────────────────────

    @Transactional
    public void deleteCourse(Long tutorId, Long courseId) {
        Course course = findCourseOrThrow(courseId);
        validateCourseOwnership(course, tutorId);
        courseRepo.delete(course);
    }

    // ── 課程卡片查詢 ──────────────────────────────────────────────────

    public List<CourseSearchDTO> getAllCourseCards() {
        List<Course> courses = courseRepo.findAll().stream()
                .filter(c -> c.getIsActive() != null && c.getIsActive())
                .collect(Collectors.toList());

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

            if (course.getTutor().getSchedules() != null) {
                List<String> slots = course.getTutor().getSchedules().stream()
                        .filter(TutorSchedule::getIsAvailable)
                        .map(this::convertToSlotTag)
                        .collect(Collectors.toList());
                dto.setAvailableSlots(slots);
            }

            return dto;
        }).collect(Collectors.toList());
    }

    // ── 私有輔助方法 ──────────────────────────────────────────────────

    private void validateNewStyleCourseReq(com.learning.api.dto.course.CourseReq req) {
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
        resp.setTutorId(course.getTutor().getId());
        resp.setName(course.getName());
        resp.setSubject(course.getSubject());
        resp.setDescription(course.getDescription());
        resp.setPrice(course.getPrice());
        resp.setActive(course.getIsActive());
        resp.setAvgRating(avgRating);
        resp.setFeedbacks(feedbacks.stream()
                .map(f -> new CourseResp.FeedbackItem(f.getRating(), f.getComment()))
                .collect(Collectors.toList()));
        return resp;
    }

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

    private String convertToSlotTag(TutorSchedule s) {
        String period = "morning";
        int hour = s.getHour();
        if (hour >= 13 && hour < 17) period = "afternoon";
        else if (hour >= 17) period = "evening";
        return s.getWeekday() + "-" + period;
    }
}
