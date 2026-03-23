package com.learning.api.service;

import com.learning.api.dto.CheckoutReq;
import com.learning.api.dto.EmailBookingDTO;
import com.learning.api.dto.EmailBookingTimeDTO;
import com.learning.api.entity.*;
import com.learning.api.enums.UserRole;
import com.learning.api.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CheckoutService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private CourseRepo courseRepo;
    @Autowired
    private OrderRepository orderRepo;
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private TutorScheduleRepo scheduleRepo;
    @Autowired
    private EmailService emailService;

    // 🌟 新增：注入錢包流水帳的 Repo
    @Autowired
    private WalletLogsRepo walletLogRepo;

    // 1. 注入 BookingService，讓建立預約的邏輯集中在那裡
    @Autowired
    private BookingService bookingService;

    @Transactional
    public String processPurchase(CheckoutReq req, Long studentId, UserRole role) {
        if (role != UserRole.STUDENT) return "非學生權限";

        User student = userRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("找不到用戶資料"));
        Course course = courseRepo.findById(req.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("找不到課程資料"));

        if (req.getLessonCount() != 1 && req.getLessonCount() != 5 && req.getLessonCount() != 10)
            throw new IllegalArgumentException("無此數量方案");

        int totalSlots = req.getSelectedSlots().size();
        if (totalSlots != req.getLessonCount())
            throw new IllegalArgumentException("選擇時段數量與課程不相符");
        if (totalSlots == 0)
            throw new IllegalArgumentException("請至少選擇一個時段");

        int unitDiscountPrice = calculateUnitDiscountPrice(course.getPrice(), totalSlots);
        int totalPrice = unitDiscountPrice * totalSlots;

        if (student.getWallet() < totalPrice) return "餘額不足";

        // ─── 第一階段：全部驗證，任何一個時段有問題就整筆取消 ───

        // 6. 用 List 先把要存的 Booking 收集起來，驗證全部通過才儲存
        List<CheckoutReq.Slot> validatedSlots = new ArrayList<>();

        for (CheckoutReq.Slot slot : req.getSelectedSlots()) {

            // 7. 把日期轉換成星期幾（Java DayOfWeek：週一=1 ... 週日=7）
            int weekday = slot.getDate().getDayOfWeek().getValue();

            // 8. 查詢老師這個星期幾的這個小時有沒有開放
            var sched = scheduleRepo.findByTutorIdAndWeekdayAndHour(
                    course.getTutor().getId(), weekday, slot.getHour());

            // 9. 如果老師沒有開放這個時段，直接拋例外（整筆交易 rollback）
            if (sched.isEmpty() || !sched.get().getIsAvailable()) {
                throw new IllegalArgumentException(
                        "時段 " + slot.getDate() + " " + slot.getHour() + ":00 老師未開放");
            }

            // 10. 查詢這個時段是否已被其他學生鎖定（slotLocked = true）
            if (bookingRepo.findByTutorIdAndDateAndHourAndSlotLockedTrue(
                    course.getTutor().getId(), slot.getDate(), slot.getHour()).isPresent()) {
                throw new IllegalArgumentException("時段已被他人預約，請重新選擇");
            }

            // 11. 驗證通過，加入待儲存清單
            validatedSlots.add(slot);
        }

        // ─── 第二階段：全部驗證通過，開始執行扣款與建立紀錄 ───

        // 12. 扣除學生錢包餘額
        student.setWallet(student.getWallet() - totalPrice);
        userRepo.save(student);

        // 13. 建立訂單紀錄
        Order order = new Order();
        order.setUserId(student.getId());           // 綁定學生
        order.setCourseId(course.getId());          // 綁定課程
        order.setUnitPrice(course.getPrice());      // 原始單堂定價（快照，日後定價改變不影響）
        order.setDiscountPrice(unitDiscountPrice);  // 實際折扣後單堂價
        order.setLessonCount(totalSlots);           // 購買堂數
        order.setLessonUsed(0);                     // 已使用堂數，初始為 0
        order.setStatus(2);                         // 2 = 成交（deal）
        Order savedOrder = orderRepo.save(order);   // 儲存並取得含 ID 的訂單

        // 14. 建立每個時段的預約紀錄（呼叫 BookingService，職責分離）
        //     使用 try-catch 捕捉資料庫的唯一約束衝突，防止極端並發下的超賣
        for (CheckoutReq.Slot slot : validatedSlots) {
            try {
                bookingService.createBooking(
                        savedOrder.getId(),          // 綁定訂單
                        course.getTutor().getId(),   // 老師 ID
                        student.getId(),             // 學生 ID
                        slot.getDate(),              // 預約日期
                        slot.getHour()               // 預約小時
                );
            } catch (DataIntegrityViolationException e) {
                // 15. 如果在極端情況下（兩人同時搶同一時段）資料庫唯一鍵衝突
                //     拋出友善的錯誤訊息，@Transactional 會自動 rollback 整筆交易
                throw new IllegalArgumentException("時段 " + slot.getDate() + " " + slot.getHour()
                        + ":00 已被他人搶走，請重新選擇");
            }
        }

        // 16. 寫入金流明細（WalletLog），記錄這筆扣款
        WalletLog walletLog = new WalletLog();
        walletLog.setUserId(student.getId());
        walletLog.setTransactionType(2);            // 2 = 購課
        walletLog.setAmount((long) -totalPrice);    // 負數代表扣款
        walletLog.setRelatedType(1);                // 1 = 關聯到 order
        walletLog.setRelatedId(savedOrder.getId()); // 綁定訂單 ID

        // 17. 用 UUID 產生唯一的內部交易序號（merchant_trade_no 是 UNIQUE，不能重複）
        walletLog.setMerchantTradeNo(
                "INT_" + UUID.randomUUID().toString().substring(0, 8) + "_" + savedOrder.getId());
        walletLogRepo.save(walletLog);

        // 18. 寄送 Email 通知老師
        List<EmailBookingTimeDTO> times = new ArrayList<>();
        for (CheckoutReq.Slot slot : validatedSlots) {
            EmailBookingTimeDTO t = new EmailBookingTimeDTO();
            t.setDate(slot.getDate());
            t.setHour(slot.getHour());
            times.add(t);
        }

        EmailBookingDTO emailDTO = new EmailBookingDTO();
        emailDTO.setTutorEmail(course.getTutor().getUser().getEmail());
        emailDTO.setTutorName(course.getTutor().getUser().getName());
        emailDTO.setStudentName(student.getName());
        emailDTO.setCourseName(course.getName());
        emailDTO.setTimes(times);
        emailService.sendBookingEmail(emailDTO);

        return "success";
    }

    /**
     * 根據購買堂數計算折扣後的單堂價格
     * 10 堂以上打 9 折，5 堂以上打 95 折，其他原價
     */
    private int calculateUnitDiscountPrice(int unitPrice, int count) {
        if (count >= 10) return (int) (unitPrice * 0.90); // 9 折
        if (count >= 5)  return (int) (unitPrice * 0.95); // 95 折
        return unitPrice;                                  // 原價
    }
}
