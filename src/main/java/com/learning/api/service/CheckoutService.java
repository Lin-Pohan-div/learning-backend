package com.learning.api.service;

import com.learning.api.dto.CheckoutReq;
import com.learning.api.entity.*;
import com.learning.api.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final UserRepository userRepo;
    private final CourseRepo courseRepo;
    private final OrderRepository orderRepo;
    private final BookingRepository bookingRepo;
    private final TutorScheduleRepo scheduleRepo;
    private final WalletService walletService;
    private final BookingService bookingService;

    @Transactional
    public String processPurchase(CheckoutReq req) {
        // 1. 取得學生資訊與課程單價
        User student = userRepo.findById(req.getStudentId()).orElseThrow();
        Course course = courseRepo.findById(req.getCourseId()).orElseThrow();

        // 2. 計算總金額
        int totalSlots = req.getSelectedSlots().size();
        int totalPrice = course.getPrice() * totalSlots;

        // 3. 檢查錢包餘額
        if (student.getWallet() < totalPrice) {
            return "餘額不足"; // 前端收到後跳轉儲值頁
        }

        // 4. 防超賣檢查 (最重要！)
        for (CheckoutReq.Slot slot : req.getSelectedSlots()) {
            // A. 檢查老師有沒有排班
            int weekday = slot.getDate().getDayOfWeek().getValue();
            var sched = scheduleRepo.findByTutorIdAndWeekdayAndHour(course.getTutorId(), weekday, slot.getHour());
            if (sched.isEmpty() || !"available".equals(sched.get().getStatus())) {
                return "時段 " + slot.getDate() + " " + slot.getHour() + ":00 已不開放";
            }
            // B. 檢查是否已被搶先預約（排除已取消的預約，允許重新預約同一時段）
            if (bookingRepo.findByTutorIdAndDateAndHourAndStatusNot(course.getTutorId(), slot.getDate(), slot.getHour(), (byte) 3).isPresent()) {
                return "時段 " + slot.getDate() + " " + slot.getHour() + ":00 已被他人預約";
            }
        }

        // 5. 正式扣錢與建立紀錄 (Transactional 保證原子性)
        // A. 建立訂單（先建立以取得 orderId 供 WalletLog 使用）
        Order order = new Order();
        order.setUserId(student.getId());
        order.setCourseId(course.getId());
        order.setUnitPrice(course.getPrice());
        order.setDiscountPrice(course.getPrice()); // 直購不打折，每堂即原價
        order.setLessonCount(totalSlots);
        order.setLessonUsed(0); // 課程尚未上課，lessonUsed 在每次 completeBooking 時遞增
        order.setStatus(2); // 2:成交
        Order savedOrder = orderRepo.save(order);

        // B. 扣除錢包並建立消費 WalletLog (type=2 購課, relatedType=1 Order)
        walletService.debit(student.getId(), totalPrice, 2, 1, savedOrder.getId());

        // C. 建立多筆預約 (Bookings) — 批次儲存減少 DB round-trip
        List<Bookings> bookingList = new ArrayList<>();
        for (CheckoutReq.Slot slot : req.getSelectedSlots()) {
            bookingList.add(bookingService.buildFromSlot(
                    savedOrder.getId(), course.getTutorId(), student.getId(),
                    slot.getDate(), slot.getHour()));
        }
        try {
            bookingRepo.saveAll(bookingList);
        } catch (DataIntegrityViolationException e) {
            // 並發情況下被其他請求搶先預約同一時段
            return "時段衝突，請重新選擇";
        }

        return "success";
    }
}
