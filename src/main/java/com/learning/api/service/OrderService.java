package com.learning.api.service;

import com.learning.api.dto.OrderDto;
import com.learning.api.entity.Course;
import com.learning.api.entity.Order;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.UserRepository;
import com.learning.api.repo.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepo;
    private final UserRepository userRepo;
    private final CourseRepo courseRepo;

    // 新增訂單
    public boolean createOrder(OrderDto.Req req) {
        if (req == null) return false;
        if (req.getUserId() == null || req.getCourseId() == null || req.getLessonCount() == null) return false;
        if (req.getLessonCount() <= 0) return false;

        if (!userRepo.existsById(req.getUserId())) return false;

        Course course = courseRepo.findById(req.getCourseId()).orElse(null);
        if (course == null || !course.getActive()) return false;

        Order order = new Order();
        order.setUserId(req.getUserId());
        order.setCourseId(req.getCourseId());

        Integer unitPrice = course.getPrice();
        order.setUnitPrice(unitPrice);
        order.setDiscountPrice(calcDiscountPrice(unitPrice, req.getLessonCount()));
        order.setLessonCount(req.getLessonCount());
        order.setLessonUsed(0);
        order.setStatus(1); // pending

        orderRepo.save(order);
        return true;
    }

    // 修改訂單 (lessonCount / lessonUsed，僅限非 complete 訂單)
    public boolean updateOrder(Long id, OrderDto.UpdateReq req) {
        if (req == null) return false;
        if (req.getLessonCount() == null && req.getLessonUsed() == null) return false;

        Order order = orderRepo.findById(id).orElse(null);
        if (order == null) return false;

        // complete(3) 的訂單不可修改
        if (order.getStatus() == 3) return false;

        int newLessonCount = req.getLessonCount() != null ? req.getLessonCount() : order.getLessonCount();
        int newLessonUsed  = req.getLessonUsed()  != null ? req.getLessonUsed()  : order.getLessonUsed();

        // 已使用不可超過購買堂數
        if (newLessonUsed > newLessonCount) return false;

        // lessonCount 調整時同步重算折扣價
        if (req.getLessonCount() != null && !req.getLessonCount().equals(order.getLessonCount())) {
            order.setLessonCount(newLessonCount);
            order.setDiscountPrice(calcDiscountPrice(order.getUnitPrice(), newLessonCount));
        }

        order.setLessonUsed(newLessonUsed);
        orderRepo.save(order);
        return true;
    }

    // 查詢單一訂單
    public OrderDto.Resp getOrderById(Long id) {
        Order order = orderRepo.findById(id).orElse(null);
        if (order == null) return null;
        return toResp(order);
    }

    // 查詢使用者所有訂單
    public List<OrderDto.Resp> getOrdersByUserId(Long userId) {
        return orderRepo.findByUserId(userId)
                .stream()
                .map(this::toResp)
                .collect(Collectors.toList());
    }

    // 更新訂單狀態
    public boolean updateStatus(Long id, OrderDto.StatusReq req) {
        if (req == null || req.getStatus() == null) return false;

        Order order = orderRepo.findById(id).orElse(null);
        if (order == null) return false;

        // 狀態只能向前推進，不可倒退
        if (req.getStatus() <= order.getStatus()) return false;

        order.setStatus(req.getStatus());
        orderRepo.save(order);
        return true;
    }

    // 取消訂單 (僅限 pending 狀態)
    public boolean cancelOrder(Long id) {
        Order order = orderRepo.findById(id).orElse(null);
        if (order == null) return false;

        // 僅 pending(1) 可取消
        if (order.getStatus() != 1) return false;

        orderRepo.deleteById(id);
        return true;
    }

    // 支付訂單
    public boolean payOrder(Long id) {
        Order order = orderRepo.findById(id).orElse(null);
        if (order == null) return false;

        // 僅 pending(1) 可支付
        if (order.getStatus() != 1) return false;

        order.setStatus(2); // paid
        orderRepo.save(order);
        return true;
    }

    private Integer calcDiscountPrice(Integer unitPrice, Integer lessonCount) {
        // 10 堂以上享 95 折
        if (lessonCount >= 10) return (int) (unitPrice * 0.95);
        return unitPrice;
    }

    private OrderDto.Resp toResp(Order order) {
        OrderDto.Resp resp = new OrderDto.Resp();
        resp.setId(order.getId());
        resp.setUserId(order.getUserId());
        resp.setCourseId(order.getCourseId());
        resp.setUnitPrice(order.getUnitPrice());
        resp.setDiscountPrice(order.getDiscountPrice());
        resp.setLessonCount(order.getLessonCount());
        resp.setLessonUsed(order.getLessonUsed());
        resp.setStatus(order.getStatus());
        return resp;
    }
}
