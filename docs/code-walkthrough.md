# Learning Platform Backend — Code Walkthrough

本文件帶你從請求進入點到資料庫，逐層追蹤程式碼，幫助你快速建立對整個系統的心理模型。

---

## 目錄

1. [專案概覽](#1-專案概覽)
2. [應用程式啟動流程](#2-應用程式啟動流程)
3. [目錄結構說明](#3-目錄結構說明)
4. [請求生命週期（HTTP）](#4-請求生命週期http)
5. [JWT 認證機制](#5-jwt-認證機制)
6. [會員系統：註冊與登入](#6-會員系統註冊與登入)
7. [結帳購課流程（核心業務邏輯）](#7-結帳購課流程核心業務邏輯)
8. [課程模組與 N+1 查詢防護](#8-課程模組與-n1-查詢防護)
9. [訂單模組](#9-訂單模組)
10. [WebSocket 即時視訊教室](#10-websocket-即時視訊教室)
11. [檔案上傳流程](#11-檔案上傳流程)
12. [全域例外處理](#12-全域例外處理)
13. [資料模型一覽](#13-資料模型一覽)
14. [預約模組](#14-預約模組)
15. [教師個人檔案模組](#15-教師個人檔案模組)
16. [教師課表模組](#16-教師課表模組)
17. [課程瀏覽與搜尋（公開 API）](#17-課程瀏覽與搜尋公開-api)
18. [課後回饋模組](#18-課後回饋模組)
19. [課程評價模組](#19-課程評價模組)
20. [Email 通知服務](#20-email-通知服務)
21. [支付服務](#21-支付服務)

---

## 1. 專案概覽

| 項目 | 內容 |
|------|------|
| 框架 | Spring Boot 4.0.2 |
| 語言版本 | Java 21 |
| 資料庫 | MySQL（JPA / Hibernate） |
| 認證 | JWT（jjwt 0.12.6） |
| 即時通訊 | WebSocket + STOMP + SockJS |
| 構建工具 | Maven |
| 程式碼生成 | Lombok |
| 文件 | SpringDoc OpenAPI (Swagger UI) |

**核心功能模組：**

```
auth          → 會員註冊 / JWT 登入
courses       → 課程 CRUD + 分頁搜尋
bookings      → 預約管理
orders        → 訂單管理（含折扣計算）
shop/purchase → 一站式結帳（下單 + 預約 + 扣款）
chatMessage   → 聊天訊息（REST + 檔案上傳）
videoRoom     → 即時視訊教室（WebSocket/STOMP）
tutor         → 教師個人資料
feedback      → 課後回饋（學生評分）
reviews       → 課程評價
```

---

## 2. 應用程式啟動流程

```
ApiApplication.main()
  └── SpringApplication.run()
        ├── SecurityConfig      (Profile != test)  → JWT Filter Chain、CORS 設定
        ├── WebSocketConfig                         → 註冊 STOMP broker + /ws 端點
        ├── MailConfig                              → 設定 JavaMailSender
        ├── WebConfig                               → 靜態資源映射 /uploads/**
        └── JPA Entity Scan                         → 掃描 com.learning.api.entity.*
```

> `@SpringBootApplication(excludeName = {SecurityAutoConfiguration})` — 停用原生 Security 自動設定，改由 `SecurityConfig` 完全接管。

**入口點：** [ApiApplication.java](../src/main/java/com/learning/api/ApiApplication.java)

---

## 3. 目錄結構說明

```
src/main/java/com/learning/api/
│
├── ApiApplication.java          ← Spring Boot 入口
│
├── annotation/
│   └── ApiController.java       ← 組合注解：@RestController（簡化 Controller 宣告）
│
├── config/
│   ├── SecurityConfig.java      ← Spring Security + CORS（無狀態 JWT 模式）
│   ├── WebSocketConfig.java     ← STOMP broker + SockJS 端點
│   ├── WebConfig.java           ← /uploads/** 靜態資源映射
│   └── MailConfig.java          ← JavaMailSender 設定
│
├── security/
│   ├── JwtService.java          ← JWT 生成 / 解析 / 驗證
│   ├── JwtFilter.java           ← OncePerRequestFilter：每次請求驗 token
│   ├── CustomUserDetailsService ← 從 DB 載入 UserDetails
│   └── SecurityUser.java        ← UserDetails 包裝（含 User entity）
│
├── controller/                  ← HTTP 路由層（薄層，不含業務邏輯）
├── service/                     ← 業務邏輯層（交易、驗證、計算）
├── repo/                        ← JPA Repository 介面
├── entity/                      ← JPA Entity（對應資料庫資料表）
├── dto/                         ← 請求/回應 DTO（與 entity 分離）
├── enums/                       ← UserRole, MessageType
└── exception/
    └── GlobalExceptionHandler   ← @RestControllerAdvice 全域例外攔截
```

---

## 4. 請求生命週期（HTTP）

以 `POST /api/auth/login` 為例，追蹤一個完整請求從進入到回應：

```
HTTP Request
  │
  ▼
[JwtFilter] (OncePerRequestFilter)          ← Profile != test 才載入
  ├── 讀取 Authorization: Bearer <token>
  ├── 若 header 為空 → filterChain.doFilter() 直接放行
  ├── 擷取 token（去掉 "Bearer " 前綴）
  ├── JwtService.email(token)               → 取出 email
  ├── CustomUserDetailsService.loadUser()   → 查 DB 取得 User
  ├── JwtService.isTokenValid()             → 比對 email + 檢查到期
  └── 寫入 SecurityContextHolder（含 ROLE_xxx 權限）
  │
  ▼
[SecurityConfig] – authorizeHttpRequests
  ├── /api/auth/**     → permitAll()
  ├── /api/view/**     → permitAll()
  ├── /api/tutor/**    → hasRole("TUTOR")
  ├── /api/student/**  → hasRole("STUDENT")
  ├── /api/admin/**    → hasRole("ADMIN")
  └── anyRequest()     → authenticated()
  │
  ▼
[AuthController.login()]
  └── AuthService.loginReq(LoginReq)
        ├── userRepository.findByEmail()       → 查 DB
        ├── passwordEncoder.matches()          → BCrypt 比對
        ├── JwtService.generateToken(user)     → 產生 JWT
        └── 回傳 LoginResp { token }
  │
  ▼
HTTP Response 200 OK  { "token": "eyJ..." }
```

**關鍵檔案：**
- [JwtFilter.java](../src/main/java/com/learning/api/security/JwtFilter.java)
- [SecurityConfig.java](../src/main/java/com/learning/api/security/SecurityConfig.java)
- [AuthController.java](../src/main/java/com/learning/api/controller/AuthController.java)
- [AuthService.java](../src/main/java/com/learning/api/service/AuthService.java)

---

## 5. JWT 認證機制

### Token 生成

```
JwtService.generateToken(User user)
  ├── subject     = user.getEmail()
  ├── claim:userId = user.getId()
  ├── claim:role  = user.getRole().name()    ← "STUDENT" | "TUTOR" | "ADMIN"
  ├── issuedAt    = now
  ├── expiration  = now + ${jwt.exp-minutes} 分鐘
  └── signWith    = HMAC-SHA（${jwt.secret} 轉 SecretKey）
```

### Token 驗證（JwtFilter）

```
Authorization: Bearer eyJhbGc...
                          │
                   JwtService.email(token)      → 取 subject
                          │
          CustomUserDetailsService.loadByEmail()
                          │
                   JwtService.isTokenValid()
                   ├── email 一致
                   └── expiration > now
                          │
              SecurityContextHolder.setAuthentication()
              └── authority = "ROLE_" + role    （e.g. ROLE_STUDENT）
```

**Role 對應表：**

| UserRole 列舉 | Spring Security 權限 | URL 保護範圍 |
|---------------|---------------------|-------------|
| `STUDENT`     | `ROLE_STUDENT`      | `/api/student/**` |
| `TUTOR`       | `ROLE_TUTOR`        | `/api/tutor/**` |
| `ADMIN`       | `ROLE_ADMIN`        | `/api/admin/**` |

**關鍵檔案：** [JwtService.java](../src/main/java/com/learning/api/security/JwtService.java)

---

## 6. 會員系統：註冊與登入

### 6.1 註冊流程

```
POST /api/auth/register
  └── AuthController.register(@Valid RegisterReq)
        └── MemberService.register(registerReq)
              ├── email.trim().toLowerCase()          → 統一格式  
              ├── userRepo.existsByEmail()            → 重複檢查
              ├── BCrypt.hashpw(password, gensalt())  → 密碼雜湊
              ├── buildMember()                       → 組裝 User entity
              └── userRepo.save()                     → 寫入 DB
```

`@Valid` 搭配 DTO 上的驗證注解（`@NotBlank`、`@Email`、`@Size` 等）在進入 Service 前攔截格式錯誤，由 `GlobalExceptionHandler` 統一轉為 400 回應。

### 6.2 登入流程

```
POST /api/auth/login
  └── AuthController.login(@Valid LoginReq)
        └── AuthService.loginReq(loginReq)
              ├── userRepo.findByEmail()                   → 找不到 → 400
              ├── passwordEncoder.matches(raw, hash)       → 不符 → 400
              ├── JwtService.generateToken(user)           → 產生 token
              └── 回傳 LoginResp { token: "eyJ..." }
```

**關鍵檔案：**
- [MemberService.java](../src/main/java/com/learning/api/service/MemberService.java)
- [AuthService.java](../src/main/java/com/learning/api/service/AuthService.java)

---

## 7. 結帳購課流程（核心業務邏輯）

`POST /api/shop/purchase` 是系統中最複雜的交易，涉及多個資料表的原子性操作：

```
CheckoutController.purchase(CheckoutReq)
  └── CheckoutService.processPurchase()  ← @Transactional（任何失敗全部回滾）
        │
        ├── [查詢] userRepo.findById(studentId)    → 取得學生資訊
        ├── [查詢] courseRepo.findById(courseId)   → 取得課程與單價
        │
        ├── [計算] totalPrice = course.price × selectedSlots.size()
        │
        ├── [驗證] student.wallet < totalPrice → return "餘額不足"
        │
        ├── [防超賣] 逐一檢查每個 Slot：
        │     ├── scheduleRepo.findByTutorIdAndWeekdayAndHour()
        │     │     └── status != "available" → return "時段已不開放"
        │     └── bookingRepo.findByTutorIdAndDateAndHour()
        │           └── 若存在 → return "時段已被他人預約"
        │
        ├── [扣款]  student.wallet -= totalPrice;  userRepo.save()
        │
        ├── [建單]  Order { status=2(成交), lessonCount=totalSlots }
        │           Order savedOrder = orderRepo.save(order)
        │
        └── [建預約] List<Bookings> — 批次 bookingRepo.saveAll()
                     每筆 Booking { orderId, tutorId, studentId, date, hour, status=1 }
        │
        ▼
      return "success"
```

**HTTP 回應對照：**

| processPurchase 回傳 | HTTP 狀態 | 前端行為 |
|----------------------|-----------|---------|
| `"success"` | 200 | 顯示成功訊息 |
| `"餘額不足"` | 402 | 導向儲值頁 |
| 其他錯誤字串 | 400 | 顯示錯誤提示 |

**關鍵檔案：**
- [CheckoutController.java](../src/main/java/com/learning/api/controller/CheckoutController.java)
- [CheckoutService.java](../src/main/java/com/learning/api/service/CheckoutService.java)

---

## 8. 課程模組與 N+1 查詢防護

### 8.1 課程 CRUD

```
CourseController (路由: /api/courses)
  ├── GET  /              → getAllCourses()        → List<CourseResp>（含平均評分）
  ├── GET  /{id}          → getCourseById()        → CourseResp
  ├── GET  /tutor/{tid}   → findByTutorId()        → 該教師所有課程
  ├── GET  /tutor/{tid}/active → findByTutorIdActive() → 上架課程
  ├── POST /              → sendCourses()          → 建立課程（驗 TUTOR 角色）
  ├── PUT  /{id}          → updateCourse()         → 更新課程
  └── DELETE /{id}        → deleteById()           → 刪除課程
```

### 8.2 N+1 查詢防護（getAllCourses）

`GET /api/courses` 需回傳每門課的平均評分，若逐筆查詢會產生 N+1 問題。實際做法：

```
CourseService.getAllCourses()
  │
  ├── ① courseRepo.findAll()                         → 一次撈所有課程
  ├── ② orderRepo.findByCourseIdIn(courseIds)        → 一次撈所有訂單（IN 查詢）
  ├── ③ bookingRepo.findByOrderIdIn(allOrderIds)     → 一次撈所有預約（IN 查詢）
  ├── ④ feedbackRepo.findByBookingIdIn(allBookingIds)→ 一次撈所有回饋（IN 查詢）
  │
  └── 在 Java 記憶體中用 Map 分組對應，組裝 List<CourseResp>
      └── avgRating = feedbacks.stream().mapToInt(rating).average()
```

DB 查詢固定為 **4 次**，不隨課程數量增長。

### 8.3 課程驗證規則

```java
// CourseService.sendCourses()
VALID_SUBJECTS = { 11, 12, 13,   // 年級課程：低/中/高年級
                   21, 22, 23,   // 檢定升學：GEPT / YLE / 國中先修
                   31 }          // 其他
price  > 0
level  ∈ [1, 5]（可選）
tutorId 對應 User 且 role == TUTOR
```

**關鍵檔案：** [CourseService.java](../src/main/java/com/learning/api/service/CourseService.java)

---

## 9. 訂單模組

### 9.1 訂單狀態機

```
1 (pending)  ──────→  2 (deal/paid)  ──────→  3 (complete)
     │
     └── DELETE /api/orders/{id}  ← 僅 pending 可取消
```

> 狀態只能向前推進（`newStatus > currentStatus`），不可倒退。

### 9.2 折扣計算

```java
// OrderService.calcDiscountPrice()
// 購買 10 堂以上，單堂享 95 折
if (lessonCount >= 10) {
    return (int)(unitPrice * 0.95);   // ← 折扣後的「每堂」價格
} else {
    return unitPrice;
}
```

注意：`discountPrice` 儲存的是**折扣後單堂價**，乘以 `lessonCount` 才是實際總金額。

### 9.3 訂單 API

| 方法 | URL | 說明 |
|------|-----|------|
| POST | `/api/orders` | 建立訂單 |
| GET | `/api/orders/{id}` | 查詢單一訂單 |
| GET | `/api/orders/user/{userId}` | 查詢用戶所有訂單 |
| PUT | `/api/orders/{id}` | 更新堂數 / 已用堂數 |
| PATCH | `/api/orders/{id}/status` | 更新訂單狀態 |
| DELETE | `/api/orders/{id}` | 取消訂單（僅 pending） |
| PATCH | `/api/orders/{id}/pay` | 支付訂單（pending → paid） |

**關鍵檔案：** [OrderService.java](../src/main/java/com/learning/api/service/OrderService.java)

---

## 10. WebSocket 即時視訊教室

### 10.1 連線流程

```
Client                              Server
  │                                   │
  ├── SockJS connect /ws ────────────>│ WebSocketConfig
  │   (HTTP Upgrade → WebSocket)      │ ├── broker: /topic
  │                                   │ └── appPrefix: /app
  │                                   │
  ├── SUBSCRIBE /topic/room/{id}/chat ─────>│
  ├── SUBSCRIBE /topic/room/{id}/signal ───>│
  ├── SUBSCRIBE /topic/room/{id}/events ───>│
  │                                   │
  ├── SEND /app/chat/{bookingId} ──────────>│ VideoRoomController.chat()
  │                                   │   └── ChatMessageService.save()  ← 持久化
  │                                   │   └── messagingTemplate.convertAndSend()
  │<── MESSAGE /topic/room/{id}/chat ───────│
```

### 10.2 訊息主題一覽

| STOMP 路徑 | 方向 | 說明 | 持久化 |
|-----------|------|------|--------|
| `/app/signal/{bookingId}` | Client → Server | WebRTC offer/answer/ICE candidate | ✗ |
| `/topic/room/{id}/signal` | Server → Client | 轉發信令給另一端 | ✗ |
| `/app/chat/{bookingId}` | Client → Server | 傳送文字聊天 | ✓ |
| `/topic/room/{id}/chat` | Server → Client | 廣播聊天訊息 | ✓ |
| `/app/event/{bookingId}` | Client → Server | 加入 / 離開事件 | ✗ |
| `/topic/room/{id}/events` | Server → Client | 廣播加入 / 離開 | ✗ |

### 10.3 聊天訊息類型

```java
// MessageType enum
TEXT(1), STICKER(2), VOICE(3), IMAGE(4), VIDEO(5), FILE(6)
```

`isMedia()` 回傳 `true` 時，`message` 欄位為 `null`，`mediaUrl` 指向上傳檔案 URL。

**關鍵檔案：**
- [VideoRoomController.java](../src/main/java/com/learning/api/controller/VideoRoomController.java)
- [WebSocketConfig.java](../src/main/java/com/learning/api/config/WebSocketConfig.java)
- [ChatMessageController.java](../src/main/java/com/learning/api/controller/ChatMessageController.java)

---

## 11. 檔案上傳流程

`POST /api/chatMessage/upload` (multipart/form-data)

```
ChatMessageController.uploadFile(file, bookingId, role)
  │
  ├── [驗證] file.isEmpty() → 400 Bad Request
  │
  ├── [偵測] MIME type → MessageType
  │     image/* → IMAGE(4)
  │     video/* → VIDEO(5)
  │     audio/* → VOICE(3)
  │     其他    → FILE(6)
  │
  ▼
FileStorageService.store(MultipartFile file)
  ├── 擷取原始副檔名（e.g. ".png"）
  ├── 產生 UUID 檔名：{uuid}.png
  ├── Files.copy() → ${file.upload-dir}/{uuid}.png
  └── 回傳存取 URL：${file.base-url}/uploads/{uuid}.png
  │
  ▼
ChatMessageService.save(bookingId, role, messageType.getValue(), null, fileUrl)
  └── 持久化 ChatMessage { message=null, mediaUrl=fileUrl }
  │
  ▼
HTTP 201 Created — 已儲存的 ChatMessage（含 id 與 createdAt）
```

儲存後的 URL 可透過 `GET /uploads/{filename}` 直接存取，由 `WebConfig` 映射至磁碟目錄。

**關鍵檔案：**
- [ChatMessageController.java](../src/main/java/com/learning/api/controller/ChatMessageController.java)
- [FileStorageService.java](../src/main/java/com/learning/api/service/FileStorageService.java)
- [WebConfig.java](../src/main/java/com/learning/api/config/WebConfig.java)

---

## 12. 全域例外處理

`@RestControllerAdvice GlobalExceptionHandler` 攔截所有 Controller 層拋出的例外：

```
例外類型                        → HTTP 狀態碼  回應格式
───────────────────────────────────────────────────────────
IllegalArgumentException        → 400          { "msg": "..." }
UsernameNotFoundException       → 400          { "msg": "..." }
```

認證 / 授權失敗由 `SecurityConfig` 直接處理（不經 ExceptionHandler）：

```
未登入 (401) → { "msg": "請先登入" }
權限不足 (403) → { "msg": "權限不足" }
```

**關鍵檔案：** [GlobalExceptionHandler.java](../src/main/java/com/learning/api/exception/GlobalExceptionHandler.java)

---

## 13. 資料模型一覽

| Entity | 資料表 | 主要欄位 | 關聯 |
|--------|--------|---------|------|
| `User` | `users` | id, name, email, password, role, wallet | ← Tutor (1:1) |
| `Tutor` | `tutors` | id (=User.id), title, intro, education, status | → User (MapsId) |
| `Course` | `courses` | id, tutorId, name, subject, price, active | → Tutor (ManyToOne) |
| `TutorSchedule` | `tutor_schedules` | id, tutorId, weekday, hour, status | → Tutor (ManyToOne) |
| `Order` | `orders` | id, userId, courseId, unitPrice, discountPrice, lessonCount, lessonUsed, status | |
| `Bookings` | `bookings` | id, orderId, tutorId, studentId, date, hour, status, slotLocked | |
| `LessonFeedback` | `lesson_feedback` | id, bookingId, focusScore, comprehensionScore, confidenceScore, rating, comment | |
| `Reviews` | `reviews` | id, userId, courseId, focusScore, comprehensionScore, confidenceScore, rating, comment | |
| `ChatMessage` | `chat_messages` | id, orderId, role, messageType, message, mediaUrl | |
| `WalletLog` | `wallet_log` | id, userId, amount, ... | |

**訂單狀態：** `1=pending` / `2=deal` / `3=complete`
**預約狀態：** `0=建立中` / `1=排程中`
**教師狀態：** `1=pending` / `2=qualified` / `3=停權`
**教師時段：** `"available"` / `"inactive"`

---

## 14. 預約模組

### 14.1 預約建立流程

`POST /api/bookings` 提供獨立建立預約的入口（開發測試用），結帳流程中的預約則由 `CheckoutService` 批次建立。

```
BookingController.sendBooking(BookingReq)
  └── BookingService.sendBooking(bookingReq)
        ├── [驗證] bookingReq == null → false
        ├── [驗證] lessonCount <= 0  → false
        ├── [查詢] userRepository.existsById(userId) → 用戶不存在 → false
        ├── [查詢] courseRepo.findById(courseId)     → 課程不存在 → false
        ├── buildBooking()
        │     ├── tutorId    = course.getTutorId()
        │     ├── studentId  = req.getUserId()
        │     ├── date / hour = req 帶入
        │     └── status     = 0（建立中）
        └── bookingRepo.save(booking)              → true
```

> **注意：** `bookingReq.getUserId()` 目前由前端傳入，供開發測試使用；正式版應改由 JWT `SecurityContext` 取得登入用戶身份。

**關鍵檔案：**
- [BookingController.java](../src/main/java/com/learning/api/controller/BookingController.java)
- [BookingService.java](../src/main/java/com/learning/api/service/BookingService.java)

---

## 15. 教師個人檔案模組

### 15.1 兩組 Controller 的職責劃分

| Controller | 路由前綴 | 用途 |
|------------|----------|------|
| `TutorController` | `/api/tutor` | 公開查看教師完整資訊（含課程、評價） |
| `TutorProfileController` | `/api/teacher/profile` | 教師管理自己的個人檔案（CRUD） |

### 15.2 公開教師頁面（TutorController）

```
GET /api/tutor/{id}?courseId={courseId}
  └── TutorController.getTutorProfile(id, courseId)
        ├── [1] TutorService.getTutor(id)                → 取得 Tutor entity
        ├── [2] TutorService.findSchedulesByTutorId(id)  → 一週課表
        ├── [3] TutorService.findCoursesByTutorId(id)    → 所有課程
        ├── [4] 選定課程邏輯：
        │     ├── courseId != null → 查指定課程
        │     └── courseId == null → 預設第一門課
        ├── [5] TutorService.findReviewsByCourseId(selectedCourse.id) → 課程評價
        ├── [6] 計算平均評分（Java stream）
        └── 組裝 TutorProfileDTO { name, headline, avatar, intro,
                                    certificate, videoUrl, schedules,
                                    reviews, averageRating }
```

### 15.3 教師個人檔案管理（TutorProfileController）

| 方法 | URL | 說明 |
|------|-----|------|
| GET | `/api/teacher/profile/{tutorId}` | 取得教師檔案 |
| POST | `/api/teacher/profile` | 初次建立教師檔案 |
| PUT | `/api/teacher/profile` | 更新教師檔案 |
| DELETE | `/api/teacher/profile/{tutorId}` | 刪除教師檔案 |

**建立 / 更新流程（TutorService）：**

```
TutorService.createProfile(TutorUpdateReq)  ← @Transactional
  ├── userRepo.findById(tutorId)    → 找不到 → "找不到該名老師"
  ├── tutorRepo.existsById()        → 已存在 → "個人檔案已存在，請使用 PUT 更新"
  ├── new Tutor()
  ├── applyDtoToTutor()             → 設定 title / avatar / intro / education /
  │                                    certificate1,2 / videoUrl1,2 / bankCode / bankAccount
  ├── tutorRepo.save(tutor)
  └── 若 dto.name != null → userRepo.save(user) ← 同步更新 User.name

TutorService.updateProfile(TutorUpdateReq)  ← @Transactional
  ├── 若 tutorRepo.findById() 找不到 → new Tutor()（upsert 語意）
  ├── applyDtoToTutor()
  └── tutorRepo.save() + 可選 userRepo.save()
```

**關鍵檔案：**
- [TutorController.java](../src/main/java/com/learning/api/controller/TutorController.java)
- [TutorProfileController.java](../src/main/java/com/learning/api/controller/TutorProfileController.java)
- [TutorService.java](../src/main/java/com/learning/api/service/TutorService.java)

---

## 16. 教師課表模組

教師以 7×24 格子的週課表形式管理可上課時段。資料庫只儲存 **開放** 的時段（`status="available"`），省略的格子視為休息。

### 16.1 切換時段狀態

```
POST /api/teacher/schedules/toggle
  └── TutorScheduleService.toggleSchedule(ToggleReq)
        ├── [驗證] weekday ∈ [1,7]，hour ∈ [9,21] → 否則 400
        ├── scheduleRepo.findByTutorIdAndWeekdayAndHour()
        │
        ├── targetStatus == "available"
        │     └── 若紀錄不存在 → INSERT 新時段
        │
        └── targetStatus != "available"（休息）
              └── 若紀錄存在 → DELETE（保持資料表精簡）
```

> **設計理念：** 以「有紀錄 = 開放」替代 boolean 欄位，刪除即等於關閉，資料表始終只含有效時段。

### 16.2 查詢週課表

```
GET /api/teacher/schedules/{tutorId}
  └── TutorScheduleService.getWeeklySchedule(tutorId)
        └── scheduleRepo.findByTutorId(tutorId)
              → List<ScheduleDTO.Res> { id, weekday, hour, status }
```

前端收到清單後，對應不到資料的格子自動顯示為「休息」。

**關鍵檔案：**
- [TutorScheduleController.java](../src/main/java/com/learning/api/controller/TutorScheduleController.java)
- [TutorScheduleService.java](../src/main/java/com/learning/api/service/TutorScheduleService.java)

---

## 17. 課程瀏覽與搜尋（公開 API）

`/api/view/**` 路由無需登入（`SecurityConfig` 設定 `permitAll()`），供前台課程列表頁使用。

### 17.1 課程搜尋 API

```
GET /api/view/courses?page=0&teacherName=&courseName=&subjectCategory=&subject=&priceRange=&weekday=&timeSlot=
  └── CourseViewController.searchCourses()
        ├── PageRequest.of(page, 10)           → 每頁 10 筆
        ├── CourseSpec.filterCourses(...)       → 動態 JPA Specification
        └── courseService.searchCourses(spec, pageable)
              → Page<CourseSearchDTO> { id, tutorId, tutorName, avatar, title,
                                        name, subject, description, price }
```

### 17.2 CourseSpec 動態篩選條件

```
CourseSpec.filterCourses()  → 組合 Predicate 清單
  ├── [強制] active == true                        ← 只顯示上架課程
  ├── [可選] teacherName LIKE %?%                  ← JOIN tutor.user.name
  ├── [可選] courseName   LIKE %?%
  ├── [可選] subject == ?                          ← 精確科目（11, 12, 21...）
  │   或     subject BETWEEN category ~ category+9 ← 大分類（10, 20, 30）
  ├── [可選] price BETWEEN min AND max             ← priceRange 格式："min-max"
  └── [可選] tutor.schedules.weekday == weekday    ← JOIN TutorSchedule
             tutor.schedules.hour BETWEEN ...      ← timeSlot: morning/afternoon/evening
             query.distinct(true)                  ← 防重複
```

**時段對照：**

| timeSlot | 時間區間 |
|----------|---------|
| `morning` | 09:00 – 12:00 |
| `afternoon` | 13:00 – 16:00 |
| `evening` | 17:00 – 20:00 |

### 17.3 教師課表查詢（公開）

```
GET /api/view/teacher_schedule/{teacherId}
  └── scheduleRepo.findByTutorId(teacherId)
        → Map<weekday, List<hour>>
          例：{ 1: [9, 10, 14], 3: [15, 16] }
```

**關鍵檔案：**
- [CourseViewController.java](../src/main/java/com/learning/api/controller/CourseViewController.java)
- [CourseSpec.java](../src/main/java/com/learning/api/Spec/CourseSpec.java)

---

## 18. 課後回饋模組

課後回饋（`LessonFeedback`）由教師對**每堂預約課程**填寫，並在儲存後觸發 Email 通知學生。

### 18.1 評分規則

```
LessonFeedbackService.validate(feedback)
  ├── focusScore        ∈ [1, 5]   （參與專注度）
  ├── comprehensionScore ∈ [1, 5]  （理解度）
  ├── confidenceScore   ∈ [1, 5]   （口語自信度）
  ├── rating            ∈ [1, 5]   （整體評分）
  └── comment.length    ≤ 1000 字元
```

### 18.2 回饋 API

| 方法 | URL | 說明 |
|------|-----|------|
| GET | `/api/feedbacks` | 取得所有回饋 |
| GET | `/api/feedbacks/{id}` | 取得單筆回饋 |
| GET | `/api/feedbacks/lesson/{bookingId}` | 依預約查詢回饋 |
| GET | `/api/feedbacks/lesson/{bookingId}/average-rating` | 取得預約平均評分 |
| POST | `/api/feedbacks` | 新增回饋（每堂限一筆） |
| PUT | `/api/feedbacks/{id}` | 更新回饋 |
| DELETE | `/api/feedbacks/{id}` | 刪除回饋 |

> **防重複：** `save()` 在寫入前會呼叫 `existsByBookingId()`，同一堂課重複填寫會拋出 `IllegalArgumentException → 400`。

**關鍵檔案：**
- [FeedbackController.java](../src/main/java/com/learning/api/controller/FeedbackController.java)
- [LessonFeedbackService.java](../src/main/java/com/learning/api/service/LessonFeedbackService.java)

---

## 19. 課程評價模組

課程評價（`Reviews`）由學生對**課程**填寫，與課後回饋（針對單堂預約）不同。

### 19.1 評價 API

| 方法 | URL | 說明 |
|------|-----|------|
| GET | `/api/reviews` | 取得所有評價 |
| GET | `/api/reviews/{id}` | 取得單筆評價 |
| GET | `/api/reviews/user/{userId}` | 取得用戶所有評價 |
| GET | `/api/reviews/course/{courseId}` | 取得課程所有評價 |
| GET | `/api/reviews/course/{courseId}/average-rating` | 取得課程平均評分 |
| POST | `/api/reviews` | 新增評價 |
| PUT | `/api/reviews/{id}` | 更新評價 |
| DELETE | `/api/reviews/{id}` | 刪除評價 |

### 19.2 評價建立驗證

```
ReviewController.create(ReviewRequest)
  ├── userId  == null → 400 "驗證失敗: userId 不能為空"
  ├── courseId == null → 400 "驗證失敗: courseId 不能為空"
  └── reviewService.save(review) → 201 Created
```

> `Reviews` 與 `LessonFeedback` 共用相同的評分維度（focusScore / comprehensionScore / confidenceScore / rating），但層級不同：Reviews 屬於課程層級，LessonFeedback 屬於單堂預約層級。

**關鍵檔案：**
- [ReviewController.java](../src/main/java/com/learning/api/controller/ReviewController.java)
- [ReviewService.java](../src/main/java/com/learning/api/service/ReviewService.java)

---

## 20. Email 通知服務

`EmailService` 以 `@ConditionalOnBean(JavaMailSender.class)` 條件載入，若環境未設定 SMTP 則自動略過，不影響其他功能。

### 20.1 通知場景

| 方法 | 觸發時機 | 收件人 | 信件主旨 |
|------|---------|--------|---------|
| `sendBookingEmail(dto)` | 結帳完成後 | 教師 | `【課程預約通知】{studentName}` |
| `sendFeedbackEmail(dto)` | 教師填寫課後回饋後 | 學生 | `【課程回饋】{courseName}` |
| `sendSimpleEmail(to, subject, text)` | 通用純文字通知 | 任意 | 自訂 |

### 20.2 預約通知 Email 內容

```
EmailService.sendBookingEmail(EmailBookingDTO)
  ├── buildBookingHtml(dto)
  │     ├── 收件教師姓名與學生姓名
  │     ├── 課程名稱與堂數
  │     └── 每筆預約時段表格（日期 + 時段 HH:00 ~ HH+1:00）
  └── MimeMessageHelper.send()  ← HTML 格式
```

### 20.3 回饋通知 Email 內容

```
EmailService.sendFeedbackEmail(FeedbackEmailDTO)
  ├── buildFeedbackHtml(dto)
  │     ├── 學生 / 教師 / 課程 / 上課時間
  │     ├── 三項評分（專注度 / 理解力 / 口語自信）以星星圖示呈現
  │     └── 教師評語
  └── MimeMessageHelper.send()  ← HTML 格式
```

> **容錯設計：** 兩個 HTML 郵件方法皆包含 try-catch，發送失敗只記錄 log，不拋出例外，確保主業務流程不受影響。

**關鍵檔案：**
- [EmailService.java](../src/main/java/com/learning/api/service/EmailService.java)
- [MailConfig.java](../src/main/java/com/learning/api/config/MailConfig.java)

---

## 21. 支付服務

`PaymentService` 封裝從待付訂單扣款的原子性操作，並寫入 `WalletLog` 交易紀錄。

```
PaymentService.pay(orderId)  ← @Transactional
  ├── [查詢] orderRepository.findById(orderId)
  │     └── 找不到 or status != 1（pending） → false
  │
  ├── [計算] totalCost = discountPrice × lessonCount
  │
  ├── [查詢] userRepository.findById(order.userId)
  │     └── 找不到 or wallet < totalCost → false（餘額不足）
  │
  ├── [扣款]  user.wallet -= totalCost
  │           userRepository.save(user)
  │
  ├── [記錄]  WalletLog { userId, transactionType=2, amount=-totalCost,
  │                        relatedType=1, relatedId=orderId }
  │           walletLogRepository.save(log)
  │
  ├── [更新]  order.status = 2（paid）
  │           orderRepository.save(order)
  │
  └── return true
```

**WalletLog 欄位說明：**

| 欄位 | 值 | 說明 |
|------|----|------|
| `transactionType` | `2` | 支出 |
| `amount` | 負數 | 扣款金額 |
| `relatedType` | `1` | 關聯對象為訂單 |
| `relatedId` | orderId | 關聯訂單 ID |

> **注意：** `PaymentService` 目前未加 `@Service` 注解，無法被 Spring 容器自動注入；實際支付邏輯由 `CheckoutService` 直接實作，`PaymentService` 為獨立封裝備用。

**關鍵檔案：**
- [PaymentService.java](../src/main/java/com/learning/api/service/PaymentService.java)
