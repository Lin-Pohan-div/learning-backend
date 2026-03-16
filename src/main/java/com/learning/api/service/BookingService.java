package com.learning.api.service;

import com.learning.api.dto.OrderDto;
import com.learning.api.dto.booking.BookingReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * BookingService 負責預約入口驗證；
 * 實際建立訂單的邏輯（含折扣計算）委派給 OrderService，避免重複實作。
 */
@Service
public class BookingService {

    @Autowired
    private OrderService orderService;

    // bookingReq.getUserId() 僅供開發測試使用，正式版改由登入資訊取得
    public boolean sendBooking(BookingReq bookingReq) {
        if (bookingReq == null) return false;

        OrderDto.Req req = new OrderDto.Req();
        req.setUserId(bookingReq.getUserId());
        req.setCourseId(bookingReq.getCourseId());
        req.setLessonCount(bookingReq.getLessonCount());

        return orderService.createOrder(req);
    }
}
