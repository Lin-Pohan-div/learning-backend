package com.learning.api.service;

import com.learning.api.dto.CheckoutReq;
import com.learning.api.entity.*;
import com.learning.api.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CheckoutService {

    @Autowired private UserRepo userRepo;
    @Autowired private CourseRepo courseRepo;
    @Autowired private OrderRepository orderRepo;
    @Autowired private BookingRepo bookingRepo;
    @Autowired private TutorScheduleRepo scheduleRepo;

    // 🌟 新增：注入錢包流水帳的 Repo
    @Autowired private WalletLogRepo walletLogRepo;

    @Transactional
    public String processPurchase(CheckoutReq req) {
        User student = userRepo.findById(req.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("找不到學生資料"));
        Course course = courseRepo.findById(req.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("找不到課程資料"));

        int totalSlots = req.getSelectedSlots().size();
        if (totalSlots == 0) throw new IllegalArgumentException("請至少選擇一個時段");

        // 計算單堂折扣價與總金額
        int unitDiscountPrice = calculateUnitDiscountPrice(course.getPrice(), totalSlots);
        int totalPrice = unitDiscountPrice * totalSlots;

        if (student.getWallet() < totalPrice) {
            return "餘額不足";
        }

        // 檢查時段
        for (CheckoutReq.Slot slot : req.getSelectedSlots()) {
            int weekday = slot.getDate().getDayOfWeek().getValue();
            var sched = scheduleRepo.findByTutorIdAndWeekdayAndHour(course.getTutorId(), weekday, slot.getHour());

            if (sched.isEmpty() || !sched.get().getIsAvailable()) {
                throw new IllegalArgumentException("時段 " + slot.getDate() + " " + slot.getHour() + ":00 老師未開放");
            }
            if (bookingRepo.findByTutorIdAndDateAndHourAndSlotLockedTrue(course.getTutorId(), slot.getDate(), slot.getHour()).isPresent()) {
                throw new IllegalArgumentException("時段已被他人預約");
            }
        }

        // 1. 扣除錢包餘額
        student.setWallet(student.getWallet() - totalPrice);
        userRepo.save(student);

        // 2. 建立訂單
        Order order = new Order();
        order.setUserId(student.getId());
        order.setCourseId(course.getId());
        order.setUnitPrice(course.getPrice());
        order.setDiscountPrice(unitDiscountPrice);
        order.setLessonCount(totalSlots);
        order.setLessonUsed(0);
        order.setStatus(2); // 2: 成交
        Order savedOrder = orderRepo.save(order);

        // 3. 建立預約紀錄
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

        // 🌟 4. 新增：寫入金流明細 (WalletLog)
        WalletLog walletLog = new WalletLog();
        walletLog.setUserId(student.getId());
        walletLog.setTransactionType(2); // 對齊你的註解：2 = 購課
        walletLog.setAmount((long) -totalPrice); // 注意：扣款必須是「負數」！
        walletLog.setRelatedType(1); // 對齊你的註解：1 = order
        walletLog.setRelatedId(savedOrder.getId()); // 綁定剛剛成立的訂單 ID

        // 因為 merchant_trade_no 是 unique，我們用 UUID 產生一組內部交易序號
        walletLog.setMerchantTradeNo("INT_" + UUID.randomUUID().toString().substring(0, 8) + "_" + savedOrder.getId());

        walletLogRepo.save(walletLog);

        return "success";
    }

    private int calculateUnitDiscountPrice(int unitPrice, int count) {
        if (count >= 10) return (int) (unitPrice * 0.90);
        if (count >= 5) return (int) (unitPrice * 0.95);
        return unitPrice;
    }
}