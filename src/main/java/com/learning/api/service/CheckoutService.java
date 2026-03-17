package com.learning.api.service;

import com.learning.api.dto.CheckoutReq;
import com.learning.api.entity.*;
import com.learning.api.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CheckoutService {

    @Autowired private UserRepo userRepo;
    @Autowired private CourseRepo courseRepo;
    @Autowired private OrderRepository orderRepo;
    @Autowired private BookingRepo bookingRepo;
    @Autowired private TutorScheduleRepo scheduleRepo;

    /**
     * 優化查詢效能與事務管理
     * 更嚴謹的判斷流程（檢查學生、檢查課程、檢查排班、檢查重複預約）
     */
    @Transactional
    public String processPurchase(CheckoutReq req) {
        // 1. 預檢查：基本資料是否存在
        User student = userRepo.findById(req.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("找不到學生資料"));
        Course course = courseRepo.findById(req.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("找不到課程資料"));

        int totalSlots = req.getSelectedSlots().size();
        if (totalSlots == 0) throw new IllegalArgumentException("請至少選擇一個時段");

        // 2. 計算折扣後的總額 (5堂95折, 10堂9折)
        int totalPrice = calculateDiscountPrice(course.getPrice(), totalSlots);

        // 3. 檢查錢包
        if (student.getWallet() < totalPrice) {
            return "餘額不足";
        }

        // 4. 時段合法性與衝突檢查 (大師建議：未來可改為批次查詢 IN 以提升效 n)
        for (CheckoutReq.Slot slot : req.getSelectedSlots()) {
            int weekday = slot.getDate().getDayOfWeek().getValue();

            // 檢查老師是否有排班
            var sched = scheduleRepo.findByTutorIdAndWeekdayAndHour(course.getTutor().getId(), weekday, slot.getHour());
            if (sched.isEmpty() || !"available".equals(sched.get().getStatus())) {
                throw new IllegalArgumentException("時段 " + slot.getDate() + " " + slot.getHour() + ":00 老師未開放");
            }

            // 檢查是否已被預約 (防超賣)
            if (bookingRepo.findByTutorIdAndDateAndHour(course.getTutor().getId(), slot.getDate(), slot.getHour()).isPresent()) {
                throw new IllegalArgumentException("時段 " + slot.getDate() + " " + slot.getHour() + ":00 已被他人預約");
            }
        }

        // 5. 執行核心交易 (扣款 -> 訂單 -> 預約)
        student.setWallet(student.getWallet() - totalPrice);
        userRepo.save(student);

        Order order = new Order();
        order.setUserId(student.getId());
        order.setCourseId(course.getId());
        order.setUnitPrice(course.getPrice());
        order.setLessonCount(totalSlots);
        order.setLessonUsed(0); // 剛買，使用次數為 0
        order.setStatus(2); // 2: 成交
        Order savedOrder = orderRepo.save(order);

        for (CheckoutReq.Slot slot : req.getSelectedSlots()) {
            Booking b = new Booking();
            b.setOrderId(savedOrder.getId());
            b.setTutorId(course.getTutor().getId());
            b.setStudentId(student.getId());
            b.setDate(slot.getDate());
            b.setHour(slot.getHour());
            b.setStatus(1); // 1: scheduled
            b.setSlotLocked(true);
            bookingRepo.save(b);
        }

        return "success";
    }

    private int calculateDiscountPrice(int unitPrice, int count) {
        int originalTotal = unitPrice * count;
        if (count >= 10) return (int) (originalTotal * 0.90);
        if (count >= 5) return (int) (originalTotal * 0.95);
        return originalTotal;
    }
}