package com.learning.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.learning.api.dto.BookingResponseDTO;
import com.learning.api.dto.CancelResponseDTO;
import com.learning.api.dto.CourseDto;
import com.learning.api.dto.PackageResponseDTO;
import com.learning.api.dto.TodayCourseDto;
import com.learning.api.entity.Booking;
import com.learning.api.entity.Course;
import com.learning.api.entity.Order;
import com.learning.api.entity.Tutor;
import com.learning.api.entity.User;
import com.learning.api.entity.WalletLog;
import com.learning.api.repo.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;


//@Service
public class StudentCourseService {

    private final TutorRepo tutorRepo;
	 @Autowired private WalletLogsRepo walletLogsRepo;
	 @Autowired private UserRepo usersRepo;
	 @Autowired private OrderRepository ordersRepo;
	 @Autowired private BookingRepo bookingsRepo;
	 @Autowired private CourseRepo coursesRepo;

    StudentCourseService(TutorRepo tutorRepo) {
        this.tutorRepo = tutorRepo;
    } // needed for purchase logic

	 public List<PackageResponseDTO> getMyPackages(Long userId) {
		 
		 	List<Order> orders = ordersRepo.findByUserId(userId);
		 	
		 	List<Long> courseIds = orders.stream().map(Order::getCourseId).distinct().toList();

		    // 3. 一次性查出這些課程的 ID 和 Name (假設你有 CourseRepo)
		    // 這裡使用 findAllById 效能很好
		    Map<Long, String> courseNameMap = coursesRepo.findAllById(courseIds).stream()
		            .collect(Collectors.toMap(Course::getId, Course::getName));
		 
	        return ordersRepo.findByUserId(userId).stream()
	            .map(order -> new PackageResponseDTO(
	                order.getId(),
	                courseNameMap.getOrDefault(order.getCourseId(), "未知課程"),
	                order.getLessonCount(),
	                order.getLessonUsed(),
	                order.getLessonCount() - order.getLessonUsed(), // 計算剩餘堂數
	                order.getStatus()
	            )).toList();
	    }

    /**
     * 學生購買課程包時的流程
     * 1. 檢查課程是否存在、學生是否有足夠錢包餘額
     * 2. 扣學生錢包、加老師錢包
     * 3. 建立訂單並紀錄 WalletLogs 供日後查
     *
     * @param studentId 購買者 id
     * @param courseId 課程 id
     * @param lessonCount 堂數
     * @return 包裝後的 PackageResponseDTO
     */
    @Transactional
    public PackageResponseDTO Buycourse(Long studentId, Long courseId, Integer lessonCount) {
        // 基本參數檢查
        if (lessonCount == null || lessonCount <= 0) {
            throw new IllegalArgumentException("lessonCount 必須大於 0");
        }

        User student = usersRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("找不到帳號"));

        Course course = coursesRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("課程不存在"));
        if (course.getActive() != 1) {
            throw new RuntimeException("課程目前不可購買");
        }

        long totalPrice = course.getPrice().longValue() * lessonCount;
        if (student.getWallet() < totalPrice) {
            throw new RuntimeException("錢包餘額不足，無法購買");
        }

        // 課程開始進行金流處理
        // 1. 扣學生錢包
        student.setWallet((int)(student.getWallet() - totalPrice));
        usersRepo.save(student);

        // 2. 加老師錢包
        // 1. 取得老師對應的 User ID
        Long tutorUserId = course.getTutor().getId(); 

        // 2. 從資料庫查出這個 User
        User tutorUser = usersRepo.findById(tutorUserId)
            .orElseThrow(() -> new RuntimeException("找不到該老師的使用者帳號"));

        // 3. 更新錢包並儲存
        tutorUser.setWallet(tutorUser.getWallet() + (int)totalPrice);
        usersRepo.save(tutorUser);
        
        // 3. 建立訂單
        User user = new User();
        Order order = new Order();
        order.setUserId(user.getId());
        order.setCourseId(course.getId());
        order.setUnitPrice(course.getPrice());
        order.setDiscountPrice((int) totalPrice); // 目前不支援折扣
        order.setLessonCount(lessonCount);
        order.setLessonUsed(0);
        order.setStatus(1); // 1 = 有效/進行中
        ordersRepo.save(order);

        // 4. 建立錢包日誌：學生消費
        WalletLog studentLog = new WalletLog();
        studentLog.setUserId(user.getId());
        studentLog.setTransactionType(2); // 2 = 購課
        studentLog.setAmount(totalPrice); // 金額欄位始終正數，由 transactionType 區分出入
        studentLog.setRelatedType(1); // 1 = order
        studentLog.setRelatedId(order.getId());
        walletLogsRepo.save(studentLog);

        // 5. 建立錢包日誌：老師收入
        WalletLog tutorLog = new WalletLog();
        tutorLog.setUserId(user.getId());
        tutorLog.setTransactionType(3); // 3 = 授課收入
        tutorLog.setAmount(totalPrice);
        tutorLog.setRelatedType(1);
        tutorLog.setRelatedId(order.getId());
        walletLogsRepo.save(tutorLog);


	 	List<Order> orders = ordersRepo.findByUserId(user.getId());
	 	
	 	List<Long> courseIds = orders.stream().map(Order::getCourseId).distinct().toList();
        
	 	Map<Long, String> courseNameMap = coursesRepo.findAllById(courseIds).stream()
		           .collect(Collectors.toMap(Course::getId, Course::getName));
        // 回傳訂單資料
        return new PackageResponseDTO(
                order.getId(),
                courseNameMap.getOrDefault(order.getCourseId(), "未知課程"),
                order.getLessonCount(),
                order.getLessonUsed(),
                order.getLessonCount() - order.getLessonUsed(),
                order.getStatus()
        );
    }

    // 取得特定課程包細節
    public PackageResponseDTO getPackageById(Long packageId) {
        Order order = ordersRepo.findById(packageId).orElseThrow();
        return new PackageResponseDTO(
            order.getId(),
            order.getCourse().getName(),
            order.getLessonCount(),
            order.getLessonUsed(),
            order.getLessonCount() - order.getLessonUsed(),
            order.getStatus()
        );
    }

    public List<BookingResponseDTO> getMyCourses(Long userId) {
        return bookingsRepo.findByStudent_Id(userId).stream()
            .map(b -> new BookingResponseDTO(
                b.getStudent().getName(),           // 學生姓名
                b.getId(),                          // 預約 ID
                b.getTutor().getName(),             // 老師姓名
                b.getOrder().getCourse().getSubject(), // 跨表抓：訂單 -> 課程 -> 科目
                b.getDate(),
                b.getHour(),
                b.getStatus()
            )).toList();
    }
    public List<BookingResponseDTO> getBookingsByOrder(Long orderId) {
        return bookingsRepo.findByOrder_Id(orderId).stream()
            .map(b -> new BookingResponseDTO(
                b.getStudent().getName(),
                b.getId(),
                b.getTutor().getName(),
                b.getOrder().getCourse().getSubject(),
                b.getDate(),
                b.getHour(),
                b.getStatus()
            )).toList();
    }
    public List<TodayCourseDto> getTodayCourses(Long studentId) {
        LocalDate today = LocalDate.now();
        List<Booking> bookings = bookingsRepo.findByStudentIdAndDateOrderByHourAsc(studentId, today);

        // 將 Entity 轉換為 DTO
        return bookings.stream().map(b -> new TodayCourseDto(
            b.getId(),
            b.getDate(),
            b.getHour(),
            b.getStatus(),
            b.getTutor().getName()
        )).toList();
    }
    
    public List<CourseDto> getCoursesByDate(Long studentId, LocalDate date) {
        // 使用傳入的 date 取代 LocalDate.now()
        List<Bookings> bookings = bookingsRepo.findByStudentIdAndDateOrderByHourAsc(studentId, date);

        return bookings.stream().map(b -> new CourseDto(
            b.getId(),
            b.getDate(),
            b.getHour(),
            b.getStatus(),
            b.getTutor().getName() // 確保 DTO 最後一個參數是 String
        )).toList();
    }

    
    @Transactional
    public CancelResponseDTO cancelBooking(Long bookingId, Long userId) {
        // 1. 查找預約並驗證身分
        Booking booking = bookingsRepo.findByIdAndStudent_Id(bookingId, userId)
                .orElseThrow(() -> new RuntimeException("預約不存在或無權限"));

        // 2. 狀態檢查：僅 status=1 (排程中) 可申請取消
        if (booking.getStatus() != 1) {
            return new CancelResponseDTO(false, "僅排程中的課程可申請取消", null);
        }

        LocalDateTime lessonTime = LocalDateTime.of(booking.getDate(), LocalTime.of(booking.getHour(), 0));
        Order order = booking.getOrder();
        
        // 3. 判斷是否在 12 小時前 (符合退費規則)
        if (LocalDateTime.now().plusHours(12).isBefore(lessonTime)) {
            
            // 【新增安全檢查】：如果已使用堂數為 0，代表邏輯有誤或重複扣款
            if (order.getLessonUsed() <= 0) {
                return new CancelResponseDTO(false, "取消失敗：已使用堂數異常，無法退還", order.getLessonCount() - order.getLessonUsed());
            }

            // A. 符合規則：修改狀態為 3，並返還堂數
            booking.setStatus((byte)3);
            order.setLessonUsed(order.getLessonUsed() - 1);
            
            bookingsRepo.save(booking);
            ordersRepo.save(order);
            
            return new CancelResponseDTO(true, "取消成功，已返還堂數", order.getLessonCount() - order.getLessonUsed());
        } else {
            // B. 逾時取消：修改狀態為 3，不退堂
            booking.setStatus((byte)3);
            bookingsRepo.save(booking);
            
            return new CancelResponseDTO(false, "逾時取消 (12hr內)，不予返還堂數", order.getLessonCount() - order.getLessonUsed());
        }
    }
    
    @Transactional
    public String refundEntireOrder(Long orderId, Long userId) {
        // 1. 查找訂單並驗證權限
        Order order = ordersRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("訂單不存在"));
         
        // 【新增安全性檢查】：確保這張訂單真的屬於該使用者
        if (!order.getUser().getId().equals(userId)) {
            return "權限不足，無法操作此訂單";
        }
        
        // 2. 狀態檢查：只有狀態為 1 (有效/購買中) 的訂單可以退
        if (order.getStatus() != 1) {
            return "訂單狀態不符（可能已結案或已退費），無法辦理退課";
        }
        List<Bookings> allBookings = bookingsRepo.findByOrder_Id(orderId);
        LocalDateTime now = LocalDateTime.now();

        AtomicBoolean isin12hr = new AtomicBoolean(false);
        
        allBookings.stream()
        .filter(b -> b.getStatus() == 1) // 只處理原本是「預約中」的
        .forEach(b -> {
            LocalDateTime lessonTime = LocalDateTime.of(b.getDate(), LocalTime.of(b.getHour(), 0));
            
            // 判斷是否在 12 小時內
            if (now.plusHours(12).isAfter(lessonTime)) {
                // A. 12 小時內：視同已上課，不退費
                b.setStatus((byte) 2); 
                isin12hr.set(true);
            } else {
                // B. 12 小時外：准予取消，會退費
                b.setStatus((byte) 3);
                
            }
            b.setSlotLocked(null); // 無論如何都釋放老師時段
        });
        // 3. 【關鍵修改】：計算已完成的堂數
        // 假設 Bookings 的 status 為 2 代表「已上完/已完成」
        
        long completedCount = allBookings.stream()
                .filter(b -> b.getStatus() == 2)
                .count();
        
        long remainingLessons = order.getLessonCount() - completedCount;
        if (remainingLessons <= 0) {
            return "無剩餘堂數可退費";
        }
        long refundAmount = order.getDiscountPrice().longValue() * remainingLessons;
        
        // 6. 批次儲存更新後的預約狀態
        bookingsRepo.saveAll(allBookings);
        
        User user = order.getUser();
        
        user.setWallet((int)(user.getWallet() + refundAmount));
	    usersRepo.save(user);
	     
        // 5. 建立 WalletLogs 紀錄 (強制轉型 byte)
         
        WalletLog log = new WalletLog();
        log.setUser(user);
        log.setTransactionType(4); // 4=退款
        log.setAmount(refundAmount);
        log.setRelatedType(1);    // 1=order
        log.setRelatedId(orderId);
        walletLogsRepo.save(log);

        // 7. 更新訂單狀態為「已退費/終止」
        // 假設 4 代表系統自動退課完成
        order.setStatus((byte)4); 
        ordersRepo.save(order);
        
        String msg = "整單退課成功！退還堂數：" + remainingLessons + "，退費金額：$" + refundAmount;
        if (isin12hr.get()) {
            msg = msg + "（註：部分課堂因在 12 小時內開課，視同已使用不予退費）";
        }
        return msg;
    }



}
