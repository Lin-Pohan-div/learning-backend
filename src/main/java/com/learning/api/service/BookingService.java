package com.learning.api.service;

import com.learning.api.dto.BookingDTO;
import com.learning.api.entity.Booking;
import com.learning.api.entity.Course;
import com.learning.api.entity.User;
import com.learning.api.repo.BookingRepo;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.OrderRepository;
import com.learning.api.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private OrderRepository orderRepo;
    @Autowired
    private CourseRepo courseRepo;

    public List<BookingDTO> getTutorBookings(Long tutorId) {
        return bookingRepo.findByTutorId(tutorId).stream()
                .map(b -> {
                    String studentName = userRepo.findById(b.getStudentId())
                            .map(User::getName)
                            .orElse("學生 #" + b.getStudentId());

                    // 從 order 查 courseId，再查課程名稱
                    String courseName = orderRepo.findById(b.getOrderId())
                            .flatMap(o -> courseRepo.findById(o.getCourseId()))
                            .map(Course::getName)
                            .orElse("課程");

                    return new BookingDTO(
                            b.getId(), b.getOrderId(), b.getTutorId(),
                            b.getStudentId(), studentName,
                            courseName,  // ← 新增
                            b.getDate(), b.getHour(), b.getStatus(), b.getSlotLocked()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 建立單筆預約紀錄（由 CheckoutService 呼叫，不對外開放）
     * 把建立 Booking 的邏輯集中在這裡，讓 CheckoutService 職責更單純
     */
    public Booking createBooking(Long orderId, Long tutorId, Long studentId,
                                 LocalDate date, Integer hour) {
        // 1. 建立新的預約物件
        Booking b = new Booking();
        b.setOrderId(orderId);     // 綁定訂單 ID（必填，DB 規定不能為空）
        b.setTutorId(tutorId);     // 老師 ID
        b.setStudentId(studentId); // 學生 ID
        b.setDate(date);           // 預約日期
        b.setHour(hour);           // 預約小時
        b.setStatus(1);            // 1 = 排程中（scheduled）
        b.setSlotLocked(true);     // 鎖定時段，防止其他人搶訂

        // 2. 寫入資料庫並回傳儲存後的物件（含自動產生的 ID）
        return bookingRepo.save(b);
    }
}
