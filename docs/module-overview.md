# 模組說明 — Learning Backend

本文件說明各 Java 套件的職責、邊界，以及模組間的依賴關係。

---

## 套件結構總覽

```
com.learning.api
├── ApiApplication.java          ← Spring Boot 入口
│
├── controller/                  ← HTTP & WebSocket 接收層
├── service/                     ← 業務邏輯層
├── repo/                        ← 資料存取層（Spring Data JPA）
├── entity/                      ← JPA 實體（資料庫對應）
├── dto/                         ← 請求/回應資料容器
│
├── security/                    ← JWT 認證、Spring Security 設定
├── config/                      ← 應用程式設定（WebSocket、Mail）
├── exception/                   ← 全域例外處理
├── annotation/                  ← 自訂組合 Annotation
└── enums/                       ← 列舉型別
```

---

## 各模組說明

### `controller/` — 接收層

**職責：** 接受 HTTP 請求、委派給 Service、回傳 HTTP 回應。Controller 不含業務邏輯。

| 檔案 | 路徑前綴 | 說明 |
|------|---------|------|
| [AuthController](../src/main/java/com/learning/api/controller/AuthController.java) | `/api/auth` | 註冊、登入 |
| [CourseController](../src/main/java/com/learning/api/controller/CourseController.java) | `/api/courses` | 課程 CRUD |
| [CheckoutController](../src/main/java/com/learning/api/controller/CheckoutController.java) | `/api/shop` | 購課結帳 |
| [OrderController](../src/main/java/com/learning/api/controller/OrderController.java) | `/api/orders` | 訂單管理 |
| [TutorController](../src/main/java/com/learning/api/controller/TutorController.java) | `/api/tutor` | 老師後台管理 |
| [TutorProfileController](../src/main/java/com/learning/api/controller/TutorProfileController.java) | `/api/teacher/profile` | 老師前台個人檔案 |
| [TutorScheduleController](../src/main/java/com/learning/api/controller/TutorScheduleController.java) | `/api/teacher/schedules` | 老師排班 |
| [ReviewController](../src/main/java/com/learning/api/controller/ReviewController.java) | `/api/reviews` | 課程評論 |
| [FeedbackController](../src/main/java/com/learning/api/controller/FeedbackController.java) | `/api/feedbacks` | 課後回饋 |
| [ChatMessageController](../src/main/java/com/learning/api/controller/ChatMessageController.java) | `/api/chatMessage` | 聊天訊息 CRUD |
| [VideoRoomController](../src/main/java/com/learning/api/controller/VideoRoomController.java) | `/app/*` (WS) | WebSocket 即時通訊 |
| [BookingController](../src/main/java/com/learning/api/controller/BookingController.java) | `/api/bookings` | 預約（委派給 OrderService） |
| [TeacherController](../src/main/java/com/learning/api/controller/TeacherController.java) | `/api/teacher` | 老師建立課程 |

---

### `service/` — 業務邏輯層

**職責：** 實作商業規則、協調多個 Repository、管理交易邊界（`@Transactional`）。

| 檔案 | 職責 |
|------|------|
| [MemberService](../src/main/java/com/learning/api/service/MemberService.java) | 使用者註冊（BCrypt 雜湊）、登入驗證、JWT 產生 |
| [CheckoutService](../src/main/java/com/learning/api/service/CheckoutService.java) | 購課原子交易：驗證餘額 → 驗證時段 → 建立 Order/Bookings → 扣款 |
| [OrderService](../src/main/java/com/learning/api/service/OrderService.java) | 訂單 CRUD、95 折邏輯、付款、取消 |
| [CourseService](../src/main/java/com/learning/api/service/CourseService.java) | 課程 CRUD、批次查詢防 N+1 |
| [TutorService](../src/main/java/com/learning/api/service/TutorService.java) | 老師資料 CRUD |
| [TutorProfileService](../src/main/java/com/learning/api/service/TutorProfileService.java) | 老師個人檔案管理（防重複建立） |
| [TutorScheduleService](../src/main/java/com/learning/api/service/TutorScheduleService.java) | 排班 toggle、週排程查詢 |
| [ReviewService](../src/main/java/com/learning/api/service/ReviewService.java) | 課程評論 CRUD、平均分計算、資料驗證 |
| [LessonFeedbackService](../src/main/java/com/learning/api/service/LessonFeedbackService.java) | 課後回饋 CRUD、rating 平均 |
| [ChatMessageService](../src/main/java/com/learning/api/service/ChatMessageService.java) | 聊天訊息 CRUD、messageType 驗證 |
| [EmailService](../src/main/java/com/learning/api/service/EmailService.java) | 寄送 HTML 格式預約通知、回饋通知 |
| [BookingService](../src/main/java/com/learning/api/service/BookingService.java) | 輕薄層，將 booking 請求委派給 OrderService |
| [TeacherCourseService](../src/main/java/com/learning/api/service/TeacherCourseService.java) | 老師建立課程 |

---

### `repo/` — 資料存取層

**職責：** 繼承 `JpaRepository`，提供 CRUD 及自訂查詢方法。不含業務邏輯。

| Repository | 對應 Entity | 重要自訂查詢 |
|------------|-------------|-------------|
| MemberRepo | User | `findByEmail()` |
| CourseRepo | Course | `findByTutorId()`, `findByTutorIdAndActiveTrue()` |
| BookingsRepo | Bookings | `findByTutorIdAndDateAndHour()` — 衝突偵測 |
| OrderRepo | Order | `findByUserId()` |
| TutorRepo | Tutor | — |
| TutorScheduleRepo | TutorSchedule | `findByTutorId()`, `findByTutorIdAndWeekdayAndHour()` |
| ReviewRepo | Reviews | `findByUserId()`, `findByCourseId()` |
| LessonFeedbackRepo | LessonFeedback | `findByBookingId()` |
| ChatMessageRepo | ChatMessage | `findByOrderId()` |
| WalletLogRepo | WalletLog | — |

---

### `entity/` — JPA 實體

**職責：** 對應資料庫資料表，使用 Hibernate annotation 定義欄位約束。

```
User (使用者)
  ├── id, name, email, password, birthday
  ├── role: 1=學生, 2=老師, 3=管理員
  └── wallet: 儲值點數（Long）

Tutor (老師資料，id = User.id)
  ├── 個人資料: title, avatarUrl, intro, education
  ├── 證書: certificate1/2, certificateName1/2
  ├── 影片: videoUrl1/2
  ├── 銀行: bankCode, bankAccount
  └── status: 1=申請中, 2=合格, 3=停用

TutorSchedule (老師週排程模板)
  ├── tutorId, weekday (1-7), hour (9-21)
  └── status: "available" | "unavailable" | "booked"

Course (課程)
  ├── tutorId, name, subject, description, price
  └── active: 是否上架

Order (訂單)
  ├── userId, courseId, unitPrice, discountPrice
  ├── lessonCount, lessonUsed
  ├── isExperienced: 是否為試課
  └── status: 1=待付款, 2=已成立, 3=已完成

Bookings (預約紀錄)
  ├── orderId, tutorId, studentId
  ├── date, hour
  └── slotLocked: 是否鎖定（退款/缺席用）

Reviews (課程評論)
  └── userId, courseId, focusScore, comprehensionScore, confidenceScore, comment

LessonFeedback (課後回饋)
  └── bookingId, focusScore, comprehensionScore, confidenceScore, comment, rating

ChatMessage (聊天訊息)
  ├── orderId, role (1=學生, 2=老師)
  ├── messageType: 1=文字, 2=貼圖, 3=語音, 4=圖片, 5=影片
  └── message, mediaUrl

WalletLog (錢包交易記錄)
  ├── userId, amount (正數=入帳, 負數=扣款)
  ├── transactionType: 1=儲值, 2=購課, 3=授課收入, 4=退款, 5=提現, 6=平台贈點
  └── relatedType + relatedId: 關聯資源
```

---

### `dto/` — 資料傳輸物件

**職責：** 定義 API 的請求（Req）與回應（Resp）格式，與 Entity 解耦，避免直接暴露資料庫欄位。

| DTO 類別 | 用途 |
|----------|------|
| `LoginReq` / `LoginResp` | 登入請求與回應 |
| `RegisterReq` | 註冊請求 |
| `CourseReq` / `CourseResp` | 課程請求與回應（含 averageRating） |
| `OrderDto.Req` / `.UpdateReq` / `.Resp` / `.StatusReq` | 訂單各操作 |
| `CheckoutReq` | 購課請求（含 selectedSlots） |
| `TutorReq` / `TutorProfileDTO` | 老師資料 |
| `ScheduleDTO.ToggleReq` / `.Res` | 排班操作 |
| `ReviewRequest` | 評論請求 |
| `FeedbackRequest` | 課後回饋請求 |
| `ChatMessageRequest` | 聊天訊息請求 |
| `EmailBookingDTO` / `FeedbackEmailDTO` | Email 通知資料 |

---

### `security/` — 認證與授權

**職責：** JWT 生成與驗證、Spring Security 過濾鏈設定、角色對應。

| 檔案 | 說明 |
|------|------|
| [SecurityConfig](../src/main/java/com/learning/api/security/SecurityConfig.java) | HTTP 安全設定：無狀態 Session、CORS、路由授權規則 |
| [JwtFilter](../src/main/java/com/learning/api/security/JwtFilter.java) | 每個請求解析 Bearer token，寫入 SecurityContext |
| [JwtService](../src/main/java/com/learning/api/security/JwtService.java) | JWT 生成（含 userId/email/role claims）、解析、到期驗證 |
| [SecurityUser](../src/main/java/com/learning/api/security/SecurityUser.java) | 實作 UserDetails，將 role int 對應到 ROLE_* 字串 |

**授權規則摘要：**

```
Public (無需登入):
  GET  /api/courses/**
  GET  /api/teacher/**
  GET  /api/reviews/**
  GET  /api/feedbacks/**
  /api/auth/**
  /ws/**
  /swagger-ui/**

ROLE_TEACHER only:
  POST/PUT/DELETE /api/teacher/**

ROLE_ADMIN only:
  /test-email/**

其他:
  需登入（任何角色皆可）
```

---

### `config/` — 設定類別

| 檔案 | 說明 |
|------|------|
| [WebSocketConfig](../src/main/java/com/learning/api/config/WebSocketConfig.java) | STOMP broker、`/ws` 端點、SockJS 支援 |
| [MailConfig](../src/main/java/com/learning/api/config/MailConfig.java) | JavaMailSender Bean、寄件人設定 |

---

### `exception/` — 例外處理

| 檔案 | 說明 |
|------|------|
| [GlobalExceptionHandler](../src/main/java/com/learning/api/exception/GlobalExceptionHandler.java) | `@RestControllerAdvice`，統一例外 → HTTP 狀態碼對應 |
| ErrorResponse | 統一錯誤回應格式 `{ status, message, timestamp }` |

---

### `annotation/` — 自訂 Annotation

| Annotation | 說明 |
|-----------|------|
| `@ApiController` | 組合 `@RestController` + `@RequestMapping`，減少 Controller 的樣板 |

---

### `enums/` — 列舉

| Enum | 值 | 說明 |
|------|----|------|
| `MessageType` | TEXT, STICKER, VOICE, IMAGE, VIDEO | ChatMessage.messageType 對應 |

---

## 模組依賴圖

```
Controller
    │
    ▼
  Service  ←──────── EmailService (side effect)
    │
    ├──▶ Repository ──▶ MySQL (JPA)
    │
    └──▶ JwtService (MemberService 登入時使用)

Security (JwtFilter)
    │
    ├──▶ JwtService (解析 token)
    └──▶ SecurityContextHolder (寫入認證資訊)
```

**設計原則：**
- Controller 只呼叫同層或下層（Service），不直接呼叫 Repository
- Service 可呼叫多個 Repository，但 Repository 之間不互相呼叫
- Email 通知在 Service 完成核心邏輯後觸發，失敗不影響主流程
- `@Transactional` 只標注在 Service 層

---

## 環境設定

| 設定檔 | 啟用條件 | 關鍵差異 |
|--------|---------|---------|
| [application.properties](../src/main/resources/application.properties) | 預設（開發） | 固定 DB 帳密, `ddl-auto=update`, `show-sql=true` |
| [application-prod.properties](../src/main/resources/application-prod.properties) | `--spring.profiles.active=prod` | 環境變數注入, `ddl-auto=validate`, `show-sql=false` |

生產環境必要環境變數：`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`
