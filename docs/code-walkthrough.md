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
