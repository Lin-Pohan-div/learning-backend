# 規格文件 - FeedbackController

來源檔案: `src/main/java/com/learning/api/controller/FeedbackController.java`

Base URL: `http://localhost:8080`

---

## 概述

管理課堂回饋（LessonFeedback）的 REST API，支援依 Booking 查詢及平均評分計算。
此為學生對課堂的回饋；老師提交課後回饋請參考 [SPEC_TeacherController.md](./SPEC_TeacherController.md)。

---

## API 互動邏輯

### 1. 取得所有課堂回饋

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/feedbacks`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body:
```json
[
  {
    "id": 1,
    "bookingId": 7,
    "focusScore": 4,
    "comprehensionScore": 5,
    "confidenceScore": 4,
    "rating": 5,
    "comment": "老師講解清楚"
  }
]
```

---

### 2. 取得單一課堂回饋

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/feedbacks/{id}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body:
```json
{
  "id": 1,
  "bookingId": 7,
  "focusScore": 4,
  "comprehensionScore": 5,
  "confidenceScore": 4,
  "rating": 5,
  "comment": "老師講解清楚"
}
```
- HTTP Status: `404 Not Found`（找不到資源，Body 為空）

---

### 3. 取得指定 Booking 的所有回饋

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/feedbacks/lesson/{bookingId}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body: 同「取得所有課堂回饋」格式的陣列

---

### 4. 取得指定 Booking 的平均評分

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/feedbacks/lesson/{bookingId}/average-rating`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body:
```json
{
  "bookingId": 7,
  "averageRating": 4.5
}
```
- 若該課堂尚無回饋，`averageRating` 回傳 `0.0`。

---

### 5. 建立課堂回饋

* **請求資訊（HTTP Request）**
- Method: `POST`
- URL: `/api/feedbacks`
- Headers: `Content-Type: application/json`
- Payload (Request Body):
```json
{
  "bookingId": 7,
  "focusScore": 4,
  "comprehensionScore": 5,
  "confidenceScore": 4,
  "rating": 5,
  "comment": "老師講解清楚"
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---|---|
| `bookingId` | Long | 是 | 所屬 Booking 的 ID |
| `focusScore` | Integer | 否 | 專注度評分 |
| `comprehensionScore` | Integer | 否 | 理解度評分 |
| `confidenceScore` | Integer | 否 | 信心度評分 |
| `rating` | Integer | 否 | 整體評分（通常為 1-5） |
| `comment` | String | 否 | 回饋內容，最長 1000 字元 |

* **回應內容 (Response)**
- HTTP Status: `201 Created`
- Body: 建立後的 `LessonFeedback` 實體

---

### 6. 更新課堂回饋

* **請求資訊（HTTP Request）**
- Method: `PUT`
- URL: `/api/feedbacks/{id}`
- Headers: `Content-Type: application/json`
- Payload (Request Body): 同「建立課堂回饋」格式

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body: 更新後的 `LessonFeedback` 實體
- HTTP Status: `404 Not Found`（找不到資源，Body 為空）

---

### 7. 刪除課堂回饋

* **請求資訊（HTTP Request）**
- Method: `DELETE`
- URL: `/api/feedbacks/{id}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `204 No Content`（成功）
- HTTP Status: `404 Not Found`（找不到資源）
- Body: 無

---

## 資料庫欄位說明

| 實體 | 資料表 | 主鍵 | 重要欄位 |
|---|---|---|---|
| `LessonFeedback` | `lesson_feedback` | `id` (Long, 自動生成) | `booking_id` (不可為空)、`focus_score`、`comprehension_score`、`confidence_score`、`rating`、`comment` (最長 1000, 可為空) |
