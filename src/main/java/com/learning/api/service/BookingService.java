package com.learning.api.service;

import com.learning.api.dto.booking.BookingReq;
import com.learning.api.entity.Bookings;
import com.learning.api.entity.Course;
import com.learning.api.entity.Order;
import com.learning.api.repo.BookingRepository;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.OrderRepository;
import com.learning.api.repo.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * BookingService 負責預約入口驗證與課程狀態管理。
 */
@Service
@RequiredArgsConstructor
public class BookingService {
    private final UserRepository userRepository;
    private final CourseRepo courseRepo;
    private final BookingRepository bookingRepo;
    private final OrderRepository orderRepo;
    private final WalletService walletService;

    // bookingReq.getUserId() 僅供開發測試使用，正式版改由登入資訊取得
    public boolean sendBooking(BookingReq bookingReq) {
        if (bookingReq == null) return false;

        // lessonCount > 0
        if (bookingReq.getLessonCount() <= 0) return false;

        // member existsById
        if (!userRepository.existsById(bookingReq.getUserId())) return false;

        // course findById
        Course course = courseRepo.findById(bookingReq.getCourseId()).orElse(null);
        if (course == null) return false;

        // check courseId isActive
        if (!course.getActive()) return false;

        // buildBooking
        Bookings booking = buildBooking(bookingReq, course);
        bookingRepo.save(booking);

        return true;
    }

    /**
     * 課程完成：status 1→2，發放教師授課收入。
     */
    @Transactional
    public String completeBooking(Long bookingId) {
        Bookings booking = bookingRepo.findById(bookingId).orElse(null);
        if (booking == null) return "預約不存在";
        if (booking.getStatus() != 1) return "只有排程中的課程可標記為完成";

        booking.setStatus((byte) 2); // 2: Complete
        bookingRepo.save(booking);

        // 從訂單取得課程單價，發放教師收入並更新已使用堂數
        Order order = orderRepo.findById(booking.getOrderId()).orElse(null);
        if (order != null) {
            long income = order.getUnitPrice();
            // 增加教師錢包並記錄授課收入 (type=3, relatedType=2 Booking)
            walletService.credit(booking.getTutorId(), income, 3, 2, bookingId);

            // 遞增已使用堂數
            order.setLessonUsed(order.getLessonUsed() + 1);
            orderRepo.save(order);
        }

        return "success";
    }

    /**
     * 取消課程：status 1→3，釋放時段，退款給學生。
     */
    @Transactional
    public String cancelBooking(Long bookingId) {
        Bookings booking = bookingRepo.findById(bookingId).orElse(null);
        if (booking == null) return "預約不存在";
        if (booking.getStatus() != 1) return "只有排程中的課程可取消";

        booking.setStatus((byte) 3); // 3: Cancelled
        booking.setSlotLocked(null); // 釋放時段，允許其他人預約
        bookingRepo.save(booking);

        // 從訂單取得折扣後單價，退款給學生 (type=4, relatedType=1 Order)
        Order order = orderRepo.findById(booking.getOrderId()).orElse(null);
        if (order != null) {
            walletService.credit(booking.getStudentId(), order.getDiscountPrice(), 4, 1, booking.getOrderId());
        }

        return "success";
    }

    private Bookings buildBooking(BookingReq req, Course course) {
        Bookings booking = new Bookings();
        booking.setOrderId(req.getOrderId());
        booking.setTutorId(course.getTutorId());
        booking.setStudentId(req.getUserId());
        booking.setDate(req.getDate());
        booking.setHour(req.getHour());
        booking.setSlotLocked(true);
        booking.setStatus((byte) 1); // 1: Scheduled
        return booking;
    }

    /** CheckoutService 用：根據時段資訊建立 Bookings 實體（不儲存）。 */
    public Bookings buildFromSlot(Long orderId, Long tutorId, Long studentId,
                                  java.time.LocalDate date, Integer hour) {
        Bookings b = new Bookings();
        b.setOrderId(orderId);
        b.setTutorId(tutorId);
        b.setStudentId(studentId);
        b.setDate(date);
        b.setHour(hour);
        b.setSlotLocked(true);
        b.setStatus((byte) 1); // 1: Scheduled
        return b;
    }
}
