# 測試規格文件 - LessonFeedbackControllerTest

來源檔案: `src/test/java/com/learning/api/controller/LessonFeedbackControllerTest.java`

---

## 測試架構

| 項目 | 說明 |
|---|---|
| 測試類型 | 整合測試 (`@SpringBootTest`) |
| HTTP 測試 | MockMvc (`webAppContextSetup`) |
| 交易管理 | `@Transactional`（每個測試後自動 rollback） |
| Base URL | `/api/feedbacks` |
| 測試數量 | 16 個測試方法 |

---

## 前置設定（`@BeforeEach`）

每個測試執行前依序建立：
1. 清除所有既有 feedback（`deleteAllInBatch()`）
2. 家教使用者（role=2，email=`tutor_feedback@example.com`）
3. 學生使用者（role=1，email=`student_feedback@example.com`）
4. 測試課程（綁定家教，price=500）
5. 測試訂單 `Order`（status=1）
6. 測試預約 `Bookings`（關聯 Order、Tutor、Student，date=今天，hour=10）→ 儲存 `savedBookingId`
7. 初始 feedback（`savedFeedback`，focusScore=4、comprehensionScore=4、confidenceScore=4、rating=4、comment="Initial feedback"）

---

## 測試案例

### GET ALL

URL: `GET /api/feedbacks`

| 測試方法 | 預期結果 |
|---|---|
| `getAll_shouldReturnListWithSavedFeedback` | 200，size>=1，驗證 `id`、`rating=4`、`focusScore=4`、`comment="Initial feedback"` |

---

### GET BY ID

URL: `GET /api/feedbacks/{id}`

| 測試方法 | 說明 | 預期結果 |
|---|---|---|
| `getById_existingId_shouldReturn200WithFeedback` | 有效 id | 200，驗證 `id`、`focusScore=4`、`comprehensionScore=4`、`confidenceScore=4`、`rating=4`、`comment` |
| `getById_nonExistingId_shouldReturn404` | id=999999 | 404 Not Found |

---

### GET BY BOOKING ID

URL: `GET /api/feedbacks/lesson/{bookingId}`

| 測試方法 | 說明 | 預期結果 |
|---|---|---|
| `getByBookingId_existingId_shouldReturnList` | 有效 bookingId | 200，size>=1 |
| `getByBookingId_nonExistingId_shouldReturnEmptyList` | bookingId=999999 | 200，空陣列 `[]` |

---

### GET AVERAGE RATING

URL: `GET /api/feedbacks/lesson/{bookingId}/average-rating`

| 測試方法 | 說明 | 預期結果 |
|---|---|---|
| `getAverageRating_withFeedback_shouldReturnAverage` | 有效 bookingId（有回饋） | 200，`$.bookingId=savedBookingId`，`$.averageRating=4.0` |
| `getAverageRating_noFeedbacks_shouldReturnZero` | bookingId=999999（無回饋） | 200，`$.bookingId=999999`，`$.averageRating=0.0` |

---

### POST — 建立回饋

URL: `POST /api/feedbacks`

Request Body 範例:
```json
{
  "bookingId": 1,
  "focusScore": 5,
  "comprehensionScore": 4,
  "confidenceScore": 3,
  "rating": 5,
  "comment": "Great lesson"
}
```

| 測試方法 | 說明 | 預期結果 |
|---|---|---|
| `post_validRequest_shouldReturn201` | 完整欄位 | 201，驗證 `id`、`focusScore=5`、`comprehensionScore=4`、`confidenceScore=3`、`rating=5`、`comment` |
| `post_missingRating_shouldReturn400` | 缺少 rating | 400 |
| `post_missingFocusScore_shouldReturn400` | 缺少 focusScore | 400 |
| `post_ratingBelowMin_shouldReturn400` | rating=0（低於最小值 1） | 400 |
| `post_ratingAboveMax_shouldReturn400` | rating=6（超過最大值 5） | 400 |
| `post_scoreOutOfRange_shouldReturn400` | focusScore=10（超出範圍） | 400 |

> **評分驗證規則**：`rating` 必須介於 **1～5**（含）；`focusScore`、`comprehensionScore`、`confidenceScore` 亦有範圍限制，超出均回傳 400。

---

### PUT — 更新回饋

URL: `PUT /api/feedbacks/{id}`

Request Body 範例:
```json
{
  "bookingId": 1,
  "focusScore": 2,
  "comprehensionScore": 3,
  "confidenceScore": 1,
  "rating": 2,
  "comment": "Updated comment"
}
```

| 測試方法 | 說明 | 預期結果 |
|---|---|---|
| `put_existingId_shouldReturn200WithUpdatedFeedback` | 有效 id | 200，`$.id` 不變，驗證所有更新後欄位值 |
| `put_nonExistingId_shouldReturn404` | id=999999 | 404 Not Found |

---

### DELETE — 刪除回饋

URL: `DELETE /api/feedbacks/{id}`

| 測試方法 | 說明 | 預期結果 |
|---|---|---|
| `delete_existingId_shouldReturn204` | 有效 id | 204 No Content |
| `delete_nonExistingId_shouldReturn404` | id=999999 | 404 Not Found |
| `delete_thenGetById_shouldReturn404` | 刪除後再 GET | 204 → 再 GET 回傳 404 |
