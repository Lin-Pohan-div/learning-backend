# 規格文件 - Learning Backend API

來源檔案: 
  `src/main/java/com/learning/api/controller/ChatMessageController.java`、
  `src/main/java/com/learning/api/controller/ReviewController.java`、
  `src/main/java/com/learning/api/controller/LessonFeedbackController.java`、
  `src/main/java/com/learning/api/controller/MemberController.java`

Base URL: `http://localhost:8080`

---

## API 互動邏輯

### 1. 取得指定 Booking 的所有聊天訊息

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/chatMessage/booking/{bookingId}`  <!-- controller mapping uses singular chatMessage -->
- Headers: 無特殊需求
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body:
```json
[
  {
    "id": 1,
    "bookingId": 10,
    "role": 0,
    "message": "你好，請問有問題嗎？",
    "createdAt": "2026-03-09T10:00:00.000+00:00"
  }
]
```
- 資料解讀與處理邏輯：依 `bookingId` 查詢所有對應的聊天訊息，以陣列形式回傳。`role` 為 Byte 型別，用於區分發訊者身份（例如學生或老師）。`createdAt` 由資料庫自動產生，不可由外部寫入或更新。

---

### 2. 建立聊天訊息

* **請求資訊（HTTP Request）**
- Method: `POST`
- URL: `/api/chatMessage`
- Headers: `Content-Type: application/json`
- Payload (Request Body):
```json
{
  "bookingId": 10,
  "role": 0,
  "message": "你好，請問有問題嗎？"
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---|---|
| `bookingId` | Long | 是 | 所屬 Booking 的 ID |
| `role` | Byte | 是 | 發訊者角色（例如 0=學生, 1=老師） |
| `message` | String | 是 | 訊息內容，最長 1000 字元，不得為空白 |

* **回應內容 (Response)**
- HTTP Status: `201 Created`
- Body:
```json
{
  "id": 1,
  "bookingId": 10,
  "role": 0,
  "message": "你好，請問有問題嗎？",
  "createdAt": "2026-03-09T10:00:00.000+00:00"
}
```
- 錯誤回應 (400 Bad Request):
```json
{
  "message": "驗證失敗: Booking ID 不能為空"
}
```
- 其他重要細節：若 `bookingId`、`role` 為 null，或 `message` 為空白，回傳 400。找不到對應資源回傳 404；伺服器錯誤回傳 500。

---

### 3. 更新聊天訊息

* **請求資訊（HTTP Request）**
- Method: `PUT`
- URL: `/api/chatMessage/{id}`
- Headers: `Content-Type: application/json`
- Payload (Request Body):
```json
{
  "message": "已更新的訊息內容"
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---|---|
| `message` | String | 是 | 新的訊息內容，不得為空白 |

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body:
```json
{
  "id": 1,
  "bookingId": 10,
  "role": 0,
  "message": "已更新的訊息內容",
  "createdAt": "2026-03-09T10:00:00.000+00:00"
}
```
- 錯誤回應 (404 Not Found): Body 為空
- 錯誤回應 (400 Bad Request):
```json
{
  "message": "驗證失敗: 消息內容不能為空"
}
```

---

### 4. 刪除聊天訊息

* **請求資訊（HTTP Request）**
- Method: `DELETE`
- URL: `/api/chatMessage/{id}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `204 No Content`（成功）
- HTTP Status: `404 Not Found`（找不到資源）
- Body: 無

---

### 5. 取得所有課程評價

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/reviews`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body:
```json
[
  {
    "id": 1,
    "userId": 5,
    "courseId": 3,
    "rating": 5,
    "comment": "課程內容豐富，老師教學認真！"
  }
]
```

---

### 6. 取得單一課程評價

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/reviews/{id}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body:
```json
{
  "id": 1,
  "userId": 5,
  "courseId": 3,
  "rating": 5,
  "comment": "課程內容豐富，老師教學認真！"
}
```
- HTTP Status: `404 Not Found`（找不到資源，Body 為空）

---

### 7. 取得指定用戶的所有評價

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/reviews/user/{userId}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body:
```json
[
  {
    "id": 1,
    "userId": 5,
    "courseId": 3,
    "rating": 5,
    "comment": "課程內容豐富！"
  }
]
```

---

### 8. 取得指定課程的所有評價

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/reviews/course/{courseId}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body:
```json
[
  {
    "id": 1,
    "userId": 5,
    "courseId": 3,
    "rating": 4,
    "comment": "值得推薦"
  }
]
```

---

### 9. 取得指定課程的平均評分

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/reviews/course/{courseId}/average-rating`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body:
```json
{
  "courseId": 3,
  "averageRating": 4.5
}
```
- 資料解讀與處理邏輯：若該課程尚無評價，`averageRating` 回傳 `0.0`。

---

### 10. 建立課程評價

* **請求資訊（HTTP Request）**
- Method: `POST`
- URL: `/api/reviews`
- Headers: `Content-Type: application/json`
- Payload (Request Body):
```json
{
  "userId": 5,
  "courseId": 3,
  "rating": 5,
  "comment": "課程內容豐富，老師教學認真！"
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---|---|
| `userId` | Long | 是 | 評價者的用戶 ID |
| `courseId` | Long | 是 | 被評價的課程 ID |
| `rating` | Byte | 是 | 評分（通常為 1-5） |
| `comment` | String | 否 | 評論內容，最長 1000 字元 |

* **回應內容 (Response)**
- HTTP Status: `201 Created`
- Body:
```json
{
  "id": 1,
  "userId": 5,
  "courseId": 3,
  "rating": 5,
  "comment": "課程內容豐富，老師教學認真！"
}
```
- 錯誤回應 (400 Bad Request):
```json
{
  "message": "驗證失敗: userId 不能為空"
}
```

---

### 11. 更新課程評價

* **請求資訊（HTTP Request）**
- Method: `PUT`
- URL: `/api/reviews/{id}`
- Headers: `Content-Type: application/json`
- Payload (Request Body):
```json
{
  "userId": 5,
  "courseId": 3,
  "rating": 4,
  "comment": "修改後的評論"
}
```

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body:
```json
{
  "id": 1,
  "userId": 5,
  "courseId": 3,
  "rating": 4,
  "comment": "修改後的評論"
}
```
- HTTP Status: `404 Not Found`（找不到資源，Body 為空）

---

### 12. 刪除課程評價

* **請求資訊（HTTP Request）**
- Method: `DELETE`
- URL: `/api/reviews/{id}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `204 No Content`（成功）
- HTTP Status: `404 Not Found`（找不到資源）
- Body: 無

---

### 13. 取得所有課堂回饋

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/Feedbacks`  <!-- controller path uses capital F -->
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body:
```json
[
  {
    "id": 1,
    "lessonId": 7,
    "rating": 5,
    "comment": "老師講解清楚"
  }
]
```

---

### 14. 取得單一課堂回饋

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/Feedbacks/{id}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body:
```json
{
  "id": 1,
  "lessonId": 7,
  "rating": 5,
  "comment": "老師講解清楚"
}
```
- HTTP Status: `404 Not Found`（找不到資源，Body 為空）

---

### 15. 取得指定課堂的所有回饋

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/Feedbacks/lesson/{bookingId}`  <!-- parameter is bookingId in controller -->
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body:
```json
[
  {
    "id": 1,
    "lessonId": 7,
    "rating": 4,
    "comment": "課堂互動良好"
  }
]
```

---

### 16. 取得指定課堂的平均評分

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/Feedbacks/lesson/{bookingId}/average-rating`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body:
```json
{
  "lessonId": 7,
  "averageRating": 4.5
}
```
- 資料解讀與處理邏輯：若該課堂尚無回饋，`averageRating` 回傳 `0.0`。

---

### 17. 建立課堂回饋

* **請求資訊（HTTP Request）**
- Method: `POST`
- URL: `/api/Feedbacks`
- Headers: `Content-Type: application/json`
- Payload (Request Body):
```json
{
  "lessonId": 7,
  "rating": 5,
  "comment": "老師講解清楚"
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---|---|
| `bookingId` | Long | 是 | 所屬 Booking 的 ID |
| `rating` | Byte | 是 | 評分（通常為 1-5） |
| `comment` | String | 否 | 回饋內容，最長 1000 字元 |

* **回應內容 (Response)**
- HTTP Status: `201 Created`
- Body:
```json
{
  "id": 1,
  "lessonId": 7,
  "rating": 5,
  "comment": "老師講解清楚"
}
```
- 錯誤回應 (400 Bad Request):
```json
{
  "message": "驗證失敗: bookingId 不能為空"
}
```

---

### 18. 更新課堂回饋

* **請求資訊（HTTP Request）**
- Method: `PUT`
- URL: `/api/Feedbacks/{id}`
- Headers: `Content-Type: application/json`
- Payload (Request Body):
```json
{
  "lessonId": 7,
  "rating": 4,
  "comment": "修改後的回饋"
}
```

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body:
```json
{
  "id": 1,
  "lessonId": 7,
  "rating": 4,
  "comment": "修改後的回饋"
}
```
- HTTP Status: `404 Not Found`（找不到資源，Body 為空）

---

### 19. 刪除課堂回饋

* **請求資訊（HTTP Request）**
- Method: `DELETE`
- URL: `/api/Feedbacks/{id}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `204 No Content`（成功）
- HTTP Status: `404 Not Found`（找不到資源）
- Body: 無

---

### 20. 會員註冊

* **請求資訊（HTTP Request）**
- Method: `POST`
- URL: `/api/auth/register`
- Headers: `Content-Type: application/json`
- Payload (Request Body):
```json
{
  "email": "user@example.com",
  "password": "securePassword123",
  "role": "STUDENT"
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---|---|
| `email` | String | 是 | 電子郵件，須唯一 |
| `password` | String | 是 | 密碼 |
| `role` | String | 是 | 角色（例如 STUDENT、TUTOR） |

* **回應內容 (Response)**
- HTTP Status: `200 OK`（成功）
- Body:
```json
{
  "msg": "歡迎"
}
```
- HTTP Status: `400 Bad Request`（註冊失敗，例如 email 重複）
- Body:
```json
{
  "msg": "註冊失敗"
}
```

---

### 21. 會員登入

* **請求資訊（HTTP Request）**
- Method: `POST`
- URL: `/api/auth/login`
- Headers: `Content-Type: application/json`
- Payload (Request Body):
```json
{
  "email": "user@example.com",
  "password": "securePassword123",
  "role": "STUDENT"
}
```

* **回應內容 (Response)**
- HTTP Status: `200 OK`（成功）
- Body:
```json
{
  "msg": "歡迎"
}
```
- HTTP Status: `401 Unauthorized`（帳號或密碼錯誤）
- Body:
```json
{
  "msg": "帳號或密碼錯誤"
}
```

---

## 其他重要功能或邏輯

### 統一錯誤回應格式

- 功能/邏輯名稱：`ErrorResponse` 統一錯誤結構
- 描述：`ChatMessageController`、`ReviewController`、`LessonFeedbackController` 均定義了內部靜態類別 `ErrorResponse`，在驗證失敗或例外發生時以一致的 JSON 格式回傳錯誤訊息，並搭配 `@ExceptionHandler(Exception.class)` 全域攔截未預期例外。
- 相關程式碼片段：
```java
// 驗證失敗時
return ResponseEntity.status(HttpStatus.BAD_REQUEST)
    .body(new ErrorResponse("驗證失敗: Booking ID 不能為空"));

// 全域例外攔截
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleException(Exception e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("伺服器錯誤: " + e.getMessage()));
}
```

---

### 平均評分為 null 時的預設值處理

- 功能/邏輯名稱：平均評分 null 保護
- 描述：`ReviewController` 與 `LessonFeedbackController` 在查詢平均評分時，若該資源尚 無任何評分資料（資料庫回傳 null），系統會自動將 `averageRating` 設為 `0.0`，避免前端接收到 null 造成處理異常。
- 相關程式碼片段：
```java
Double avg = reviewService.getAverageRating(courseId);
return ResponseEntity.ok(Map.of(
    "courseId", courseId,
    "averageRating", avg != null ? avg : 0.0
));
```

---

### CORS 設定

- 功能/邏輯名稱：跨來源資源共享（CORS）
- 描述：`MemberController` 套用 `@CrossOrigin(origins = "*")`，允許所有來源的跨域請求存取 `/api/auth` 路徑下的端點，適用於前端開發測試期間。其餘 Controller 未套用 CORS 設定。
- 相關程式碼片段：
```java
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class MemberController { ... }
```

---

### 資料庫欄位說明

| 實體 | 資料表 | 主鍵 | 重要欄位 |
|---|---|---|---|
| `ChatMessage` | `chat_messages` | `id` (Long, 自動生成) | `booking_id` (不可為空)、`role` (Byte, 不可為空)、`message` (最長 1000)、`created_at` (自動產生，唯讀) |
| `Review` | `reviews` | `id` (Long, 自動生成) | `user_id` (不可為空)、`course_id` (不可為空)、`rating` (Byte, 不可為空)、`comment` (最長 1000, 可為空) |
| `LessonFeedback` | `lesson_feedback` | `id` (Long, 自動生成) | `lesson_id` (不可為空)、`rating` (Byte, 不可為空)、`comment` (最長 1000, 可為空) |
| `Member` | `users` | `id` (Long, 自動生成) | `email` (不可為空, 唯一)、`password` (不可為空)、`role` (不可為空) |
