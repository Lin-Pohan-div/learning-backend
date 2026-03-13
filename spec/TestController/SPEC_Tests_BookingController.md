# 測試規格文件 - BookingControllerTest

來源檔案: `src/test/java/com/learning/api/controller/BookingControllerTest.java`

---

## 測試架構

| 項目 | 說明 |
|---|---|
| 測試類型 | 整合測試 (`@SpringBootTest`) |
| HTTP 測試 | MockMvc (`webAppContextSetup`) |
| 交易管理 | `@Transactional`（每個測試後自動 rollback） |
| 測試數量 | 9 個測試方法 |

---

## 前置設定（`@BeforeEach`）

每個測試執行前依序建立：
1. 測試使用者（role=1，email=`student_booking@example.com`，wallet=0）
2. 啟用課程 `activeCourse`（active=true，price=500，subject=11，level=1）
3. 停用課程 `inactiveCourse`（active=false，其餘同上）

---

## 測試案例

### POST — 建立預約

URL: `POST /api/bookings`

Request Body 範例:
```json
{ "userId": 1, "courseId": 1, "lessonCount": 5 }
```

| 測試方法 | Payload | 預期結果 |
|---|---|---|
| `post_validRequest_shouldReturn200` | 有效 userId、courseId（啟用）、lessonCount=5 | 200，`$.message="建立成功"` |
| `post_10Lessons_shouldReturn200` | 有效 userId、courseId（啟用）、lessonCount=10 | 200，`$.message="建立成功"` |
| `post_nullUserId_shouldReturn400` | userId=null | 400，`$.message="建立失敗"` |
| `post_nullCourseId_shouldReturn400` | courseId=null | 400，`$.message="建立失敗"` |
| `post_nullLessonCount_shouldReturn400` | lessonCount=null | 400，`$.message="建立失敗"` |
| `post_lessonCountZero_shouldReturn400` | lessonCount=0 | 400，`$.message="建立失敗"` |
| `post_nonExistentUser_shouldReturn400` | userId=999999（不存在） | 400，`$.message="建立失敗"` |
| `post_nonExistentCourse_shouldReturn400` | courseId=999999（不存在） | 400，`$.message="建立失敗"` |
| `post_inactiveCourse_shouldReturn400` | courseId 對應停用課程 | 400，`$.message="建立失敗"` |

---

## 重要驗證邏輯

- `lessonCount` 必須為正整數（0 或 null 均不合法）
- `userId` 及 `courseId` 須對應資料庫中存在的記錄
- 課程必須為啟用狀態（`active=true`），否則建立失敗
- 所有失敗情境統一回傳 `400 Bad Request`，body 包含 `$.message="建立失敗"`
