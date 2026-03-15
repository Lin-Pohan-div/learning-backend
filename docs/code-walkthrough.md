# Code Walkthrough — Learning Backend

本文件帶你從請求進入點到資料庫，逐層追蹤程式碼，幫助你快速建立對整個系統的心理模型。

---

## 1. 應用程式啟動流程

```
ApiApplication.main()
  └── SpringApplication.run()
        ├── SecurityConfig         → 建立 JWT Filter Chain、CORS 設定
        ├── WebSocketConfig        → 註冊 STOMP broker、/ws 端點
        ├── MailConfig             → 設定 JavaMailSender
        └── JPA Entity Scan        → 掃描 com.learning.api.entity.*
```

**入口點：** [ApiApplication.java](../src/main/java/com/learning/api/ApiApplication.java)

---

## 2. 請求生命週期（HTTP）

以 `POST /api/auth/login` 為例，追蹤一個完整請求：

```
HTTP Request
  │
  ▼
[JwtFilter] (OncePerRequestFilter)
  ├── 讀取 Authorization: Bearer <token>
  ├── JwtService.parseToken() → 驗證簽章與到期時間
  ├── 取出 userId、role → 寫入 SecurityContextHolder
  └── 若 /api/auth/** → token 為空也直接放行
  │
  ▼
[SecurityConfig] – authorizeHttpRequests
  └── /api/auth/** → permitAll()，直接放行
  │
  ▼
[AuthController.login()]
  └── MemberService.login(LoginReq)
        ├── memberRepo.findByEmail()   → 查詢資料庫
        ├── BCrypt.checkpw()           → 比對密碼雜湊
        ├── JwtService.generateToken() → 產生 JWT
        └── 回傳 LoginResp { token, UserResp }
  │
  ▼
HTTP Response 200 OK
```

**關鍵檔案：**
- [JwtFilter.java](../src/main/java/com/learning/api/security/JwtFilter.java)
- [AuthController.java](../src/main/java/com/learning/api/controller/AuthController.java)
- [MemberService.java](../src/main/java/com/learning/api/service/MemberService.java)

---

## 3. JWT 認證機制

```
登入成功
  └── JwtService.generateToken(User)
        ├── Claims: sub=userId, email, role, iat, exp
        ├── 簽章算法: HMAC-SHA (secret 來自 ${jwt.secret})
        └── 有效期: ${jwt.exp-minutes}（預設 24 小時）

後續請求
  └── Header: Authorization: Bearer eyJ...
        └── JwtFilter 解析 → SecurityContextHolder
              └── 權限: ROLE_STUDENT | ROLE_TEACHER | ROLE_ADMIN
```

Role 對應表：

| DB 值 | Spring Security 角色 |
|-------|---------------------|
| 1     | ROLE_STUDENT        |
| 2     | ROLE_TEACHER        |
| 3     | ROLE_ADMIN          |

---

## 4. 結帳購課流程（核心業務邏輯）

`POST /api/shop/purchase` 是系統中最複雜的交易，涉及多個一致性保證：

```
CheckoutController.purchase(CheckoutReq)
  └── CheckoutService.processPurchase()  ← @Transactional
        │
        ├── [驗證] 取得 Course、計算總金額
        ├── [驗證] userRepo 查詢學生錢包餘額 → 餘額不足則 400
        ├── [驗證] 逐一檢查 selectedSlots：
        │     ├── tutorScheduleRepo → 確認該時段為 available
        │     └── bookingsRepo → 確認無衝突預約
        │
        ├── [寫入] 建立 Order（狀態 1=pending）
        ├── [寫入] 逐一建立 Bookings（orderId 關聯）
        ├── [寫入] 更新 TutorSchedule status → "booked"
        ├── [寫入] 扣除學生 wallet
        └── [寫入] 新增 WalletLog（transactionType=2）

        若任何步驟失敗 → 整個 @Transactional 回滾
```

**關鍵檔案：**
- [CheckoutController.java](../src/main/java/com/learning/api/controller/CheckoutController.java)
- [CheckoutService.java](../src/main/java/com/learning/api/service/CheckoutService.java)

---

## 5. 課程列表的 N+1 防護

`GET /api/courses` 需要同時回傳每門課的平均評分，天真的做法會產生 N+1 查詢：

```
❌ 錯誤做法（N+1）:
   courses.forEach(c -> feedbackRepo.findByCourseId(c.getId()))

✅ CourseService.getAllCourses() 的做法:
   1. 一次查詢所有 courses
   2. 收集全部 courseId
   3. feedbackRepo.findByCourseIdIn(courseIds)  ← 單一 IN 查詢
   4. 在 Java 中以 Map 分組 → 計算平均分
   5. 組裝 List<CourseResp>
```

**關鍵檔案：** [CourseService.java](../src/main/java/com/learning/api/service/CourseService.java)

---

## 6. WebSocket 即時通訊流程

```
Client                          Server
  │                               │
  ├── SockJS connect /ws ────────>│
  │                               │ WebSocketConfig 設定 STOMP broker
  │                               │
  ├── SUBSCRIBE /topic/room/{bookingId}/chat ──>│
  │                               │
  ├── SEND /app/chat/{bookingId} ─────────────>│
  │                               │ VideoRoomController.handleChat()
  │                               │   └── simpMessagingTemplate.convertAndSend()
  │<─── MESSAGE /topic/room/{bookingId}/chat ──│
  │                               │
```

同一個 booking 房間有三個 topic：
- `/topic/room/{id}/signal` — WebRTC ICE/SDP 訊號交換
- `/topic/room/{id}/chat` — 文字聊天
- `/topic/room/{id}/events` — 加入/離開事件

**關鍵檔案：** [VideoRoomController.java](../src/main/java/com/learning/api/controller/VideoRoomController.java)

---

## 7. 全域例外處理

所有 Controller 拋出的例外都由 `GlobalExceptionHandler` 統一攔截：

```
Exception 類型                    → HTTP 狀態碼  回應格式
─────────────────────────────────────────────────────
NoSuchElementException            → 404          ErrorResponse
IllegalArgumentException          → 400          ErrorResponse
MethodArgumentNotValidException   → 400          欄位錯誤清單
Exception (其他)                  → 500          ErrorResponse
```

**關鍵檔案：** [GlobalExceptionHandler.java](../src/main/java/com/learning/api/exception/GlobalExceptionHandler.java)

---

## 8. 訂單折扣邏輯

`OrderService.createOrder()` 在建立訂單時自動計算折扣：

```java
// 10 堂以上打 95 折
if (lessonCount >= 10) {
    discountPrice = (int) (unitPrice * lessonCount * 0.95);
} else {
    discountPrice = unitPrice * lessonCount;
}
```

---

## 9. Email 通知觸發點

系統在以下節點寄送 HTML 格式信件：

| 觸發事件    | 寄件方法                           |
|-------------|------------------------------------|
| 建立預約    | `EmailService.sendBookingEmail()`  |
| 提交課後回饋 | `EmailService.sendFeedbackEmail()` |

Email 失敗不會中斷 API 回應（獨立 try-catch）。

**關鍵檔案：** [EmailService.java](../src/main/java/com/learning/api/service/EmailService.java)
