# 規格文件 - ReviewController

來源檔案: `src/main/java/com/learning/api/controller/ReviewController.java`

Base URL: `http://localhost:8080`

---

## 概述

管理課程評價（Review）的 REST API，支援依課程、用戶查詢及平均評分計算。

---

## API 互動邏輯

### 1. 取得所有課程評價

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
    "focusScore": 4,
    "comprehensionScore": 5,
    "confidenceScore": 4,
    "comment": "課程內容豐富，老師教學認真！"
  }
]
```

---

### 2. 取得單一課程評價

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
  "focusScore": 4,
  "comprehensionScore": 5,
  "confidenceScore": 4,
  "comment": "課程內容豐富，老師教學認真！"
}
```
- HTTP Status: `404 Not Found`（找不到資源，Body 為空）

---

### 3. 取得指定用戶的所有評價

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/reviews/user/{userId}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body: 同「取得所有課程評價」格式的陣列

---

### 4. 取得指定課程的所有評價

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/reviews/course/{courseId}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body: 同「取得所有課程評價」格式的陣列

---

### 5. 取得指定課程的平均評分

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
- 若該課程尚無評價，`averageRating` 回傳 `0.0`。

---

### 6. 建立課程評價

* **請求資訊（HTTP Request）**
- Method: `POST`
- URL: `/api/reviews`
- Headers: `Content-Type: application/json`
- Payload (Request Body):
```json
{
  "userId": 5,
  "courseId": 3,
  "focusScore": 4,
  "comprehensionScore": 5,
  "confidenceScore": 4,
  "comment": "課程內容豐富，老師教學認真！"
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---|---|
| `userId` | Long | 是 | 評價者的用戶 ID |
| `courseId` | Long | 是 | 被評價的課程 ID |
| `focusScore` | Integer | 否 | 專注度評分 |
| `comprehensionScore` | Integer | 否 | 理解度評分 |
| `confidenceScore` | Integer | 否 | 信心度評分 |
| `comment` | String | 否 | 評論內容，最長 1000 字元 |

* **回應內容 (Response)**
- HTTP Status: `201 Created`
- Body: 建立後的 `Reviews` 實體
- 錯誤回應 (400 Bad Request):
```json
{
  "message": "驗證失敗: userId 不能為空"
}
```

---

### 7. 更新課程評價

* **請求資訊（HTTP Request）**
- Method: `PUT`
- URL: `/api/reviews/{id}`
- Headers: `Content-Type: application/json`
- Payload (Request Body): `Reviews` 實體欄位

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body: 更新後的 `Reviews` 實體
- HTTP Status: `404 Not Found`（找不到資源，Body 為空）

---

### 8. 刪除課程評價

* **請求資訊（HTTP Request）**
- Method: `DELETE`
- URL: `/api/reviews/{id}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `204 No Content`（成功）
- HTTP Status: `404 Not Found`（找不到資源）
- Body: 無

---

## 資料庫欄位說明

| 實體 | 資料表 | 主鍵 | 重要欄位 |
|---|---|---|---|
| `Reviews` | `reviews` | `id` (Long, 自動生成) | `user_id` (不可為空)、`course_id` (不可為空)、`focus_score`、`comprehension_score`、`confidence_score`、`comment` (最長 1000, 可為空) |
