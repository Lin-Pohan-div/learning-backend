# 測試規格文件 - ReviewControlTest

來源檔案: `src/test/java/com/learning/api/controller/ReviewControlTest.java`

---

## 測試架構

| 項目 | 說明 |
|---|---|
| 測試類型 | 整合測試 (`@SpringBootTest`) |
| HTTP 測試 | MockMvc (`webAppContextSetup`) |
| 交易管理 | `@Transactional`（每個測試後自動 rollback） |
| 測試數量 | 14 個測試方法 |

---

## 前置設定（`@BeforeEach`）

每個測試執行前依序建立：
1. 學生使用者（role=1，email=`testuser@example.com`）→ 儲存 `savedUserId`
2. 家教使用者（role=2，email=`testtutor@example.com`）
3. 測試課程 1（price=500）→ 儲存 `savedCourseId`
4. 測試課程 2（price=600）→ 儲存 `savedCourseId2`（用於 POST 測試）
5. 清除所有既有 review（`deleteAll()`）
6. 初始 review（`savedReview`，focusScore=4、comprehensionScore=3、confidenceScore=5、comment="Initial comment"）

---

## 測試案例

### GET ALL

URL: `GET /api/reviews`

| 測試方法 | 預期結果 |
|---|---|
| `getAll_shouldReturnListWithSavedReview` | 200，size>=1，`$[0].id` 存在，`$[0].focusScore` 為數值 |

---

### GET BY ID

URL: `GET /api/reviews/{id}`

| 測試方法 | 說明 | 預期結果 |
|---|---|---|
| `getById_existingId_shouldReturn200WithReview` | 有效 id | 200，驗證 `id`、`userId`、`courseId`、`focusScore=4`、`comprehensionScore=3`、`comment="Initial comment"` |
| `getById_nonExistingId_shouldReturn404` | id=999999 | 404 Not Found |

---

### GET BY USER ID

URL: `GET /api/reviews/user/{userId}`

| 測試方法 | 說明 | 預期結果 |
|---|---|---|
| `getByUserId_shouldReturnMatchingReviews` | 有效 userId | 200，size>=1，`$[0].userId` 符合查詢值 |

---

### GET BY COURSE ID

URL: `GET /api/reviews/course/{courseId}`

| 測試方法 | 說明 | 預期結果 |
|---|---|---|
| `getByCourseId_shouldReturnMatchingReviews` | 有效 courseId | 200，size>=1，`$[0].courseId` 符合查詢值 |

---

### GET AVERAGE RATING

URL: `GET /api/reviews/course/{courseId}/average-rating`

| 測試方法 | 說明 | 預期結果 |
|---|---|---|
| `getAverageRating_shouldReturnCourseIdAndAverageRating` | 有效 courseId | 200，`$.courseId` 符合，`$.averageRating` 為數值 |

---

### POST — 建立評價

URL: `POST /api/reviews`

Request Body 範例:
```json
{
  "userId": 1,
  "courseId": 2,
  "focusScore": 5,
  "comprehensionScore": 4,
  "confidenceScore": 3,
  "comment": "Excellent session"
}
```

| 測試方法 | 說明 | 預期結果 |
|---|---|---|
| `post_validRequest_shouldReturn201WithCreatedReview` | 完整欄位（使用 courseId2） | 201，驗證 `id`、`userId`、`courseId`、`focusScore=5`、`comment` |
| `post_missingUserId_shouldReturn400` | 缺少 userId | 400，`$.message` 含 "userId" |
| `post_missingCourseId_shouldReturn400` | 缺少 courseId | 400，`$.message` 含 "courseId" |
| `post_missingFocusScore_shouldReturn400` | 缺少 focusScore | 400，`$.message` 含 "專注分數" |

> **注意**：Review 不含 `rating` 欄位，改以 `focusScore`、`comprehensionScore`、`confidenceScore` 評分。

---

### PUT — 更新評價

URL: `PUT /api/reviews/{id}`

Request Body: `Reviews` 實體（含 userId、courseId、focusScore、comprehensionScore、confidenceScore、comment）

| 測試方法 | 說明 | 預期結果 |
|---|---|---|
| `put_existingId_shouldReturn200WithUpdatedReview` | 有效 id | 200，`$.id` 不變，`$.focusScore=2`、`$.comment="Updated comment"` |
| `put_nonExistingId_shouldReturn404` | id=999999 | 404 Not Found |

---

### DELETE — 刪除評價

URL: `DELETE /api/reviews/{id}`

| 測試方法 | 說明 | 預期結果 |
|---|---|---|
| `delete_existingId_shouldReturn204` | 有效 id | 204 No Content |
| `delete_nonExistingId_shouldReturn404` | id=999999 | 404 Not Found |
| `delete_thenGetById_shouldReturn404` | 刪除後再 GET | 204 → 再 GET 回傳 404 |
