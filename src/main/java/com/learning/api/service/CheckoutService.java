package com.learning.api.service;

import com.learning.api.dto.CheckoutReq;
import com.learning.api.entity.*;
import com.learning.api.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CheckoutService {

    @Autowired private UserRepo userRepo;           // 需建立
    @Autowired private CourseRepo courseRepo;       // 需建立
    @Autowired private OrderRepo orderRepo;
    @Autowired private BookingRepo bookingRepo;
    @Autowired private TutorScheduleRepo scheduleRepo;

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
            // B. 檢查是否已被搶先預約
            if (bookingRepo.findByTutorIdAndDateAndHour(course.getTutorId(), slot.getDate(), slot.getHour()).isPresent()) {
                return "時段 " + slot.getDate() + " " + slot.getHour() + ":00 已被他人預約";
            }
        }

        // 5. 正式扣錢與建立紀錄 (Transactional 保證原子性)
        // A. 扣除錢包
        student.setWallet(student.getWallet() - totalPrice);
        userRepo.save(student);

        // B. 建立訂單
        Order order = new Order();
        order.setUserId(student.getId());
        order.setCourseId(course.getId());
        order.setUnitPrice(course.getPrice());
        order.setLessonCount(totalSlots);
        order.setLessonUsed(totalSlots); // 因為是直接買時段，所以直接標記為已使用
        order.setStatus(2); // 2:成交
        Order savedOrder = orderRepo.save(order);

        // C. 建立多筆預約 (Bookings)
        for (CheckoutReq.Slot slot : req.getSelectedSlots()) {
            Booking b = new Booking();
            b.setOrderId(savedOrder.getId());
            b.setTutorId(course.getTutorId());
            b.setStudentId(student.getId());
            b.setDate(slot.getDate());
            b.setHour(slot.getHour());
            b.setStatus(1); // 1:排程中
            bookingRepo.save(b);
        }

        return "success";
    }
}