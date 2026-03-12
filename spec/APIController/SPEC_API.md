# 規格文件索引 - Learning Backend API

Base URL: `http://localhost:8080`

---

## Controller 規格文件一覽

| Controller | Base Path | 說明 | 規格文件 |
|---|---|---|---|
| `UserController` | `/api/auth` | 會員註冊 / 登入 | [SPEC_UserController.md](./SPEC_UserController.md) |
| `BookingController` | `/api/bookings` | 課程預約建立 | [SPEC_BookingController.md](./SPEC_BookingController.md) |
| `CourseController` | `/api/courses` | 課程建立 | [SPEC_CourseController.md](./SPEC_CourseController.md) |
| `OrderController` | `/api/orders` | 訂單管理（建立、查詢、更新、取消、支付） | [SPEC_OrderController.md](./SPEC_OrderController.md) |
| `ReviewController` | `/api/reviews` | 課程評價 CRUD | [SPEC_ReviewController.md](./SPEC_ReviewController.md) |
| `FeedbackController` | `/api/feedbacks` | 課堂回饋 CRUD | [SPEC_FeedbackController.md](./SPEC_FeedbackController.md) |
| `ChatMessageController` | `/api/chatMessage` | 聊天訊息 CRUD（含多媒體） | [SPEC_ChatMessageController.md](./SPEC_ChatMessageController.md) |
| `TeacherController` | `/api/teacher/courses` | 老師新增課程 | [SPEC_TeacherController.md](./SPEC_TeacherController.md) |
| `TutorProfileController` | `/api/teacher/profile` | 老師個人檔案更新 | [SPEC_TeacherController.md](./SPEC_TeacherController.md) |
| `TutorScheduleController` | `/api/teacher/schedules` | 老師排班管理 | [SPEC_TeacherController.md](./SPEC_TeacherController.md) |
| `TutorFeedbackController` | `/api/teacher/feedbacks` | 老師送出課後回饋 | [SPEC_TeacherController.md](./SPEC_TeacherController.md) |
| `VideoRoomController` | WebSocket `/ws` | 視訊聊天室（WebRTC 信令 / 即時聊天 / 房間事件） | [SPEC_VideoRoomController.md](./SPEC_VideoRoomController.md) |

---

## 全域行為

### 統一錯誤回應格式

驗證失敗或例外發生時，回傳格式如下：
```json
{
  "message": "錯誤描述"
}
```

| HTTP Status | 情境 |
|---|---|
| `400 Bad Request` | 參數驗證失敗 |
| `401 Unauthorized` | 身份驗證失敗 |
| `404 Not Found` | 資源不存在 |
| `500 Internal Server Error` | 伺服器錯誤 |

### 平均評分 null 保護

`ReviewController` 與 `FeedbackController` 查詢平均評分時，若資料庫回傳 null，自動回傳 `0.0`，避免前端處理異常。

---

## 訊息類型定義（ChatMessage / VideoRoom 共用）

| 值 | 類型 | isMedia() | 說明 |
|----|------|-----------|------|
| 1 | TEXT | false | 文字訊息（預設） |
| 2 | STICKER | true | 貼圖 |
| 3 | VOICE | true | 語音 |
| 4 | IMAGE | true | 圖片 |
| 5 | VIDEO | true | 影片 |

---

## 訂單狀態定義（Order）

| 值 | 狀態 | 說明 |
|----|------|------|
| 1 | pending | 待處理（可取消） |
| 2 | deal | 已成交 |
| 3 | complete | 已完成 |
