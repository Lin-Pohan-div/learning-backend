# 規格文件 - 測試說明

來源檔案: `src/test/java/com/learning/api/ApiApplicationTests.java`、`src/test/java/com/learning/api/controller/ChatMessageControllerTest.java`、`src/test/java/com/learning/api/controller/LessonFeedbackControllerTest.java`、`src/test/java/com/learning/api/controller/ReviewControlTest.java`

---

## 測試架構概述

| 項目 | 說明 |
|---|---|
| 測試框架 | JUnit 5 (Jupiter) |
| 整合測試 | Spring Boot Test (`@SpringBootTest`) |
| HTTP 測試 | MockMvc (`MockMvcBuilders.webAppContextSetup`) |
| JSON 序列化 | Jackson `ObjectMapper` |
| 斷言函式庫 | Hamcrest Matchers |
| 交易管理 | `@Transactional`（每個測試後自動 rollback） |
| 測試總數 | 約 53 個測試方法 |

---

## API 互動邏輯 (HTTP Endpoints)

### 1. Chat Message API — `ChatMessageControllerTest`

測試前置設定（`@BeforeEach`）：
- 建立測試使用者（role=2，家教角色）
- 建立測試課程（綁定上述使用者）
- 建立測試預約訂單（`Order`）
- 建立一筆初始聊天訊息（role=1，message="Initial message"）

---

#### 1-1. 取得預約的聊天訊息（GET）

* **請求資訊（HTTP Request）**
  - Method: `GET`
  - URL: `/api/chatMessage/booking/{bookingId}`
  - Headers: 無需額外 headers

* **回應內容 (Response)**
  - HTTP Status: `200 OK`
  - Body:
```json
[
  {
    "id": 1,
    "bookingId": 1,
    "role": 1,
    "message": "Initial message",
    "createdAt": "..."
  }
]
```

| 測試案例 | 說明 | 預期結果 |
|---|---|---|
| `getByBookingId_existingBooking_shouldReturnMessages` | 有效的 bookingId | 200，回傳包含至少一筆訊息的陣列，驗證 bookingId、role、message |
| `getByBookingId_noMessages_shouldReturnEmptyList` | 刪除所有訊息後查詢 | 200，回傳空陣列 `[]` |
| `getByBookingId_messagesOrderedByCreatedAtAsc` | 新增第二筆訊息後查詢 | 200，回傳 2 筆，依 createdAt 升冪排序（Initial message → Second message） |

---

#### 1-2. 新增聊天訊息（POST）

* **請求資訊（HTTP Request）**
  - Method: `POST`
  - URL: `/api/chatMessage`
  - Headers: `Content-Type: application/json`
  - Payload (Request Body):
```json
{
  "bookingId": 1,
  "role": 1,
  "message": "Hello tutor"
}
```

* **回應內容 (Response)**
  - HTTP Status: `201 Created`
  - Body:
```json
{
  "id": 2,
  "bookingId": 1,
  "role": 1,
  "message": "Hello tutor"
}
```

| 測試案例 | 說明 | Payload | 預期結果 |
|---|---|---|---|
| `post_validRequest_studentRole_shouldReturn201` | 學生角色（role=1）傳送訊息 | bookingId, role=1, message="Hello tutor" | 201，回傳含 id、bookingId、role、message |
| `post_validRequest_tutorRole_shouldReturn201` | 家教角色（role=2）傳送訊息 | bookingId, role=2, message="Hello student" | 201，回傳 role=2、message="Hello student" |
| `post_missingBookingId_shouldReturn400` | 缺少 bookingId | role, message | 400，錯誤訊息含 "Booking ID" |
| `post_missingRole_shouldReturn400` | 缺少 role | bookingId, message | 400，錯誤訊息含 "Role" |
| `post_emptyMessage_shouldReturn400` | 訊息為空白字串 | bookingId, role, message="   " | 400，錯誤訊息含 "消息內容" |
| `post_nonExistingBookingId_shouldReturn404` | 不存在的 bookingId (999999) | bookingId=999999, role=1, message | 404 Not Found |

---

#### 1-3. 更新聊天訊息（PUT）

* **請求資訊（HTTP Request）**
  - Method: `PUT`
  - URL: `/api/chatMessage/{id}`
  - Headers: `Content-Type: application/json`
  - Payload (Request Body):
```json
{
  "message": "Updated message content"
}
```

* **回應內容 (Response)**
  - HTTP Status: `200 OK`
  - Body:
```json
{
  "id": 1,
  "bookingId": 1,
  "message": "Updated message content"
}
```

| 測試案例 | 說明 | 預期結果 |
|---|---|---|
| `put_existingId_shouldReturn200WithUpdatedMessage` | 有效 id | 200，回傳更新後的 message，id 與 bookingId 不變 |
| `put_nonExistingId_shouldReturn404` | id=999999 | 404 Not Found |
| `put_emptyMessage_shouldReturn400` | message="  " | 400，錯誤訊息含 "消息內容" |

---

#### 1-4. 刪除聊天訊息（DELETE）

* **請求資訊（HTTP Request）**
  - Method: `DELETE`
  - URL: `/api/chatMessage/{id}`

| 測試案例 | 說明 | 預期結果 |
|---|---|---|
| `delete_existingId_shouldReturn204` | 有效 id | 204 No Content |
| `delete_nonExistingId_shouldReturn404` | id=999999 | 404 Not Found |
| `delete_thenGetByBookingId_shouldReturnEmptyList` | 刪除後再 GET | 刪除 204，隨後 GET 回傳空陣列 |

---

### 2. Lesson Feedback API — `LessonFeedbackControllerTest`

測試前置設定（`@BeforeEach`）：
- 清除所有既有 feedback（`deleteAllInBatch()`）
- 建立初始 feedback（bookingId=1, rating=4, comment="Initial feedback"）

---

#### 2-1. 取得所有課程回饋（GET ALL）

* **請求資訊（HTTP Request）**
  - Method: `GET`
  - URL: `/api/lesson-feedbacks`

* **回應內容 (Response)**
  - HTTP Status: `200 OK`
  - Body:
```json
[
  {
    "id": 1,
    "bookingId": 1,
    "rating": 4,
    "comment": "Initial feedback"
  }
]
```

| 測試案例 | 說明 | 預期結果 |
|---|---|---|
| `getAll_shouldReturnListWithSavedFeedback` | 查詢全部 | 200，陣列中含 savedFeedback 的 id、rating=4、comment="Initial feedback" |

---

#### 2-2. 依 ID 取得課程回饋（GET BY ID）

* **請求資訊（HTTP Request）**
  - Method: `GET`
  - URL: `/api/lesson-feedbacks/{id}`

| 測試案例 | 說明 | 預期結果 |
|---|---|---|
| `getById_existingId_shouldReturn200WithFeedback` | 有效 id | 200，回傳 id、rating=4、comment="Initial feedback" |
| `getById_nonExistingId_shouldReturn404` | id=999999 | 404 Not Found |

---

#### 2-3. 依 Lesson ID 取得課程回饋（GET BY LESSON ID）

* **請求資訊（HTTP Request）**
  - Method: `GET`
  - URL: `/api/lesson-feedbacks/lesson/{lessonId}`

| 測試案例 | 說明 | 預期結果 |
|---|---|---|
| `getByLessonId_noFeedbacks_shouldReturnEmptyList` | 不存在的 lessonId | 200，回傳空陣列 `[]` |

---

#### 2-4. 取得平均評分（GET AVERAGE RATING）

* **請求資訊（HTTP Request）**
  - Method: `GET`
  - URL: `/api/lesson-feedbacks/lesson/{lessonId}/average-rating`

* **回應內容 (Response)**
  - HTTP Status: `200 OK`
  - Body:
```json
{
  "bookingId": 999999,
  "averageRating": 0.0
}
```

| 測試案例 | 說明 | 預期結果 |
|---|---|---|
| `getAverageRating_noFeedbacks_shouldReturnZero` | 無回饋的 lessonId | 200，bookingId 對應 lessonId，averageRating=0.0 |

---

#### 2-5. 新增課程回饋（POST）

* **請求資訊（HTTP Request）**
  - Method: `POST`
  - URL: `/api/lesson-feedbacks`
  - Headers: `Content-Type: application/json`
  - Payload (Request Body):
```json
{
  "bookingId": 1,
  "rating": 5,
  "comment": "Great lesson"
}
```

* **回應內容 (Response)**
  - HTTP Status: `201 Created`
  - Body:
```json
{
  "id": 2,
  "bookingId": 1,
  "rating": 5,
  "comment": "Great lesson"
}
```

| 測試案例 | 說明 | Payload | 預期結果 |
|---|---|---|---|
| `post_validRequest_shouldReturn201` | 正常新增 | bookingId=1, rating=5, comment="Great lesson" | 201，回傳含 id、rating=5、comment |
| `post_missingLessonId_shouldReturn400` | 缺少 bookingId | rating=3, comment | 400 Bad Request |
| `post_missingRating_shouldReturn400` | 缺少 rating | bookingId=1, comment | 400 Bad Request |
| `post_ratingBelowMin_shouldReturn400` | rating=0（低於最小值 1） | bookingId=1, rating=0 | 400 Bad Request |
| `post_ratingAboveMax_shouldReturn400` | rating=6（超過最大值 5） | bookingId=1, rating=6 | 400 Bad Request |

> **Rating 驗證規則**：rating 必須介於 **1～5**（含），超出範圍一律回傳 400。

---

#### 2-6. 更新課程回饋（PUT）

* **請求資訊（HTTP Request）**
  - Method: `PUT`
  - URL: `/api/lesson-feedbacks/{id}`
  - Headers: `Content-Type: application/json`
  - Payload (Request Body):
```json
{
  "rating": 2,
  "comment": "Updated comment"
}
```

| 測試案例 | 說明 | 預期結果 |
|---|---|---|
| `put_existingId_shouldReturn200WithUpdatedFeedback` | 有效 id | 200，回傳更新後 rating=2、comment="Updated comment"，id 不變 |
| `put_nonExistingId_shouldReturn404` | id=999999 | 404 Not Found |

---

#### 2-7. 刪除課程回饋（DELETE）

* **請求資訊（HTTP Request）**
  - Method: `DELETE`
  - URL: `/api/lesson-feedbacks/{id}`

| 測試案例 | 說明 | 預期結果 |
|---|---|---|
| `delete_existingId_shouldReturn204` | 有效 id | 204 No Content |
| `delete_nonExistingId_shouldReturn404` | id=999999 | 404 Not Found |
| `delete_thenGetById_shouldReturn404` | 刪除後再 GET | 刪除 204，隨後 GET 回傳 404 |

---

### 3. Review API — `ReviewControlTest`

測試前置設定（`@BeforeEach`）：
- 建立學生使用者（role=1）及家教使用者（role=2）
- 建立兩個測試課程（Course 1、Course 2），並綁定家教使用者
- 清除所有既有 review（`deleteAll()`）
- 建立初始 review（userId, courseId, rating=4, comment="Initial comment"）

---

#### 3-1. 取得所有評論（GET ALL）

* **請求資訊（HTTP Request）**
  - Method: `GET`
  - URL: `/api/reviews`

* **回應內容 (Response)**
  - HTTP Status: `200 OK`
  - Body:
```json
[
  {
    "id": 1,
    "userId": 1,
    "courseId": 1,
    "rating": 4,
    "comment": "Initial comment"
  }
]
```

| 測試案例 | 說明 | 預期結果 |
|---|---|---|
| `getAll_shouldReturnListWithSavedReview` | 查詢全部 | 200，陣列中含 id 欄位，rating 為數值型別 |

---

#### 3-2. 依 ID 取得評論（GET BY ID）

* **請求資訊（HTTP Request）**
  - Method: `GET`
  - URL: `/api/reviews/{id}`

| 測試案例 | 說明 | 預期結果 |
|---|---|---|
| `getById_existingId_shouldReturn200WithReview` | 有效 id | 200，回傳 id、userId、courseId、rating=4、comment="Initial comment" |
| `getById_nonExistingId_shouldReturn404` | id=999999 | 404 Not Found |

---

#### 3-3. 依使用者 ID 取得評論（GET BY USER ID）

* **請求資訊（HTTP Request）**
  - Method: `GET`
  - URL: `/api/reviews/user/{userId}`

| 測試案例 | 說明 | 預期結果 |
|---|---|---|
| `getByUserId_shouldReturnMatchingReviews` | 有效 userId | 200，陣列含至少 1 筆，每筆 userId 皆符合查詢條件 |

---

#### 3-4. 依課程 ID 取得評論（GET BY COURSE ID）

* **請求資訊（HTTP Request）**
  - Method: `GET`
  - URL: `/api/reviews/course/{courseId}`

| 測試案例 | 說明 | 預期結果 |
|---|---|---|
| `getByCourseId_shouldReturnMatchingReviews` | 有效 courseId | 200，陣列含至少 1 筆，每筆 courseId 皆符合查詢條件 |

---

#### 3-5. 取得課程平均評分（GET AVERAGE RATING）

* **請求資訊（HTTP Request）**
  - Method: `GET`
  - URL: `/api/reviews/course/{courseId}/average-rating`

* **回應內容 (Response)**
  - HTTP Status: `200 OK`
  - Body:
```json
{
  "courseId": 1,
  "averageRating": 4.0
}
```

| 測試案例 | 說明 | 預期結果 |
|---|---|---|
| `getAverageRating_shouldReturnCourseIdAndAverageRating` | 有效 courseId | 200，回傳 courseId 及數值型別的 averageRating |

---

#### 3-6. 新增評論（POST）

* **請求資訊（HTTP Request）**
  - Method: `POST`
  - URL: `/api/reviews`
  - Headers: `Content-Type: application/json`
  - Payload (Request Body):
```json
{
  "userId": 1,
  "courseId": 2,
  "rating": 5,
  "comment": "Excellent session"
}
```

* **回應內容 (Response)**
  - HTTP Status: `201 Created`
  - Body:
```json
{
  "id": 2,
  "userId": 1,
  "courseId": 2,
  "rating": 5,
  "comment": "Excellent session"
}
```

| 測試案例 | 說明 | Payload | 預期結果 |
|---|---|---|---|
| `post_validRequest_shouldReturn201WithCreatedReview` | 正常新增 | userId, courseId=course2, rating=5, comment | 201，回傳含 id、userId、courseId、rating=5、comment |
| `post_missingUserId_shouldReturn400` | 缺少 userId | courseId, rating=5 | 400，錯誤訊息含 "userId" |
| `post_missingCourseId_shouldReturn400` | 缺少 courseId | userId, rating=3 | 400，錯誤訊息含 "courseId" |
| `post_missingRating_shouldReturn400` | 缺少 rating | userId, courseId, comment | 400，錯誤訊息含 "rating" |

---

#### 3-7. 更新評論（PUT）

* **請求資訊（HTTP Request）**
  - Method: `PUT`
  - URL: `/api/reviews/{id}`
  - Headers: `Content-Type: application/json`
  - Payload (Request Body):
```json
{
  "userId": 1,
  "courseId": 1,
  "rating": 2,
  "comment": "Updated comment"
}
```

| 測試案例 | 說明 | 預期結果 |
|---|---|---|
| `put_existingId_shouldReturn200WithUpdatedReview` | 有效 id | 200，回傳更新後 rating=2、comment="Updated comment"，id 不變 |
| `put_nonExistingId_shouldReturn404` | id=999999 | 404 Not Found |

---

#### 3-8. 刪除評論（DELETE）

* **請求資訊（HTTP Request）**
  - Method: `DELETE`
  - URL: `/api/reviews/{id}`

| 測試案例 | 說明 | 預期結果 |
|---|---|---|
| `delete_existingId_shouldReturn204` | 有效 id | 204 No Content |
| `delete_nonExistingId_shouldReturn404` | id=999999 | 404 Not Found |
| `delete_thenGetById_shouldReturn404` | 刪除後再 GET | 刪除 204，隨後 GET 回傳 404 |

---

## 其他重要功能或邏輯

### 應用程式啟動驗證

- **功能/邏輯名稱**：Spring Context 載入測試
- **描述**：`ApiApplicationTests.contextLoads()` 確認 Spring Boot 應用程式能成功啟動並載入所有 Bean，屬於最基礎的健康確認測試。
- **相關程式碼片段**：
```java
@SpringBootTest
class ApiApplicationTests {
    @Test
    void contextLoads() { }
}
```

---

### 交易自動回滾機制

- **功能/邏輯名稱**：`@Transactional` 測試隔離
- **描述**：所有測試類別皆標註 `@Transactional`，每個測試方法執行完畢後，資料庫變更自動回滾，確保測試之間互不影響，無需手動清除資料。
- **相關程式碼片段**：
```java
@SpringBootTest
@Transactional
class ChatMessageControllerTest {
    // 每個 @Test 方法結束後，所有 DB 操作自動回滾
}
```

---

### 測試資料準備模式

- **功能/邏輯名稱**：`@BeforeEach` 標準化前置資料
- **描述**：每個測試方法執行前，透過 `@BeforeEach` 統一建立必要的測試資料（User、Course、Order/Booking、初始實體），確保每個測試都在已知的乾淨狀態下執行。`ChatMessageControllerTest` 與 `ReviewControlTest` 依賴外鍵關係，因此需依序建立 User → Course → Booking/Review。
- **相關程式碼片段**：
```java
@BeforeEach
void setUp() {
    // 1. MockMvc 初始化
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    // 2. 建立相依實體（User → Course → Order）
    // 3. 建立待測試的主要實體
}
```

---

### 錯誤回應格式

- **功能/邏輯名稱**：統一錯誤回應結構
- **描述**：驗證失敗時（400 Bad Request），API 回傳 JSON 物件，包含 `message` 欄位說明錯誤原因。各 API 的錯誤訊息關鍵字如下：

| API | 缺少欄位 | 錯誤訊息關鍵字 |
|---|---|---|
| ChatMessage | bookingId | "Booking ID" |
| ChatMessage | role | "Role" |
| ChatMessage | message（空白） | "消息內容" |
| Review | userId | "userId" |
| Review | courseId | "courseId" |
| Review | rating | "rating" |

- **相關程式碼片段**：
```java
.andExpect(status().isBadRequest())
.andExpect(jsonPath("$.message").value(containsString("Booking ID")));
```

---

### HTTP 狀態碼規範

| 操作 | 狀態碼 | 說明 |
|---|---|---|
| GET（成功） | `200 OK` | 回傳資源或空陣列 |
| POST（成功） | `201 Created` | 建立成功，回傳新資源 |
| PUT（成功） | `200 OK` | 更新成功，回傳更新後資源 |
| DELETE（成功） | `204 No Content` | 刪除成功，無 body |
| 資源不存在 | `404 Not Found` | ID 查無對應資源 |
| 驗證失敗 | `400 Bad Request` | 必填欄位缺失或格式不合法 |
