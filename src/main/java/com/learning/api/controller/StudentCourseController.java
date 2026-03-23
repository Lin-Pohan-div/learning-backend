package com.learning.api.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learning.api.dto.BookingResponseDTO;
import com.learning.api.dto.BuycourseRequestDTO;
import com.learning.api.dto.CancelBookingRequestDTO;
import com.learning.api.dto.CancelResponseDTO;
import com.learning.api.dto.CourseResponseDto;
import com.learning.api.dto.PackageResponseDTO;
import com.learning.api.dto.RefundOrderRequestDTO;
import com.learning.api.dto.TodayCourseDto;
import com.learning.api.service.StudentCourseService;


@RestController
@RequestMapping("/api")
public class StudentCourseController {

    @Autowired
    private StudentCourseService courseService;

    // 4 & 6. 取得我的所有課程包 (Student Package / My Courses)
    @GetMapping({"/student-packages/me", "/orders/me"})
    public List<PackageResponseDTO> getMyPackages(@RequestParam("userId") Long userId) {
        return courseService.getMyPackages(userId);
    }

    // 4. 取得特定課程包資訊
    @GetMapping("/student-packages/{packageId}")
    public PackageResponseDTO getPackageDetail(@PathVariable Long packageId) {
        return courseService.getPackageById(packageId);
    }

    // 6. 取得我的預約紀錄 (My Bookings)
    @GetMapping("/courses/me")
    public List<BookingResponseDTO> getMycourses(@RequestParam("userId") Long userId) {
        return courseService.getMyCourses(userId);
    }
    
    @GetMapping("/bookings/me")
    public List<BookingResponseDTO> getMyBookings(@RequestParam("orderId") Long orderId) {
        return courseService.getBookingsByOrder(orderId);
    }
    
    @GetMapping("/today/me")
    public List<TodayCourseDto> getMyTodayCourses(@RequestParam("userId") Long userId) {
        // userId 傳入後對應到資料庫的 studentId
        return courseService.getTodayCourses(userId);
    }
    
    @GetMapping("/daily/me")
    public List<CourseResponseDto> getMyCoursesByDate(
            @RequestParam("userId") Long userId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return courseService.getCoursesByDate(userId, date);
    }
    
    @GetMapping("/future/me")
    public List<TodayCourseDto> getMyFutureCourses(@RequestParam("userId") Long userId) {
        return courseService.getFutureCourses(userId);
    }
    
    @PostMapping("/bookings/cancel")
    public CancelResponseDTO cancelBooking(@RequestBody CancelBookingRequestDTO request) {
        return courseService.cancelBooking(request.bookingId(), request.userId());
    }

    @PostMapping("/orders/refund")
    public String refundOrder(@RequestBody RefundOrderRequestDTO request) {
        return courseService.refundEntireOrder(request.orderId(), request.userId());
    }

//    // 新增購買課程的 API
//    @PostMapping("/orders/buycourse")
//    public PackageResponseDTO buycourseCourse(@RequestBody BuycourseRequestDTO request) {
//        return courseService.Buycourse(request.userId(), request.courseId(), request.lessonCount());
//    }

}
