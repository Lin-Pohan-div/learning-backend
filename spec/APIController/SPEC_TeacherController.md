# 規格文件 - Teacher API（TeacherController / TutorProfileController / TutorScheduleController / TutorFeedbackController）

來源檔案:
- `src/main/java/com/learning/api/controller/TeacherController.java`
- `src/main/java/com/learning/api/controller/TutorProfileController.java`
- `src/main/java/com/learning/api/controller/TutorScheduleController.java`
- `src/main/java/com/learning/api/controller/TutorFeedbackController.java`

Base URL: `http://localhost:8080`

---

## 概述

老師（Tutor）相關的 REST API，涵蓋課程管理、個人檔案更新、排班管理及課後回饋提交。

---

## API 互動邏輯

### 1. 新增課程（TeacherController）

* **請求資訊（HTTP Request）**
- Method: `POST`
- URL: `/api/teacher/courses`
- Headers: `Content-Type: application/json`
- Payload (Request Body):
```json
{
  "tutorId": 2,
  "name": "初級兒童美語",
  "subject": 1,
  "level": 5,
  "price": 700,
  "description": "本課程教學設計有趣，激發孩子對口說的信心",
  "active": true
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---|---|
| `tutorId` | Long | 是 | 老師 ID（開發測試用，正式版改由登入資訊取得） |
| `name` | String | 是 | 課程名稱 |
| `subject` | Integer | 否 | 科目代碼 |
| `level` | Integer | 否 | 難度等級 |
| `price` | Integer | 否 | 課程單價（元） |
| `description` | String | 否 | 課程描述 |
| `active` | Boolean | 否 | 是否上架 |

* **回應內容 (Response)**
- HTTP Status: `200 OK`（成功）
- Body:
```json
{
  "message": "課程新增成功！學生現在可以購買了！"
}
```
- HTTP Status: `400 Bad Request`（資料格式錯誤或價格異常）
- Body:
```json
{
  "message": "新增課程失敗，請檢查資料格式或價格"
}
```

---

### 2. 更新老師個人檔案（TutorProfileController）

* **請求資訊（HTTP Request）**
- Method: `PUT`
- URL: `/api/teacher/profile`
- Headers: `Content-Type: application/json`
- Payload (Request Body):
```json
{
  "tutorId": 2,
  "name": "王小明",
  "intro": "英語教學十年，專注兒童英文",
  "certificate": "TESOL 認證",
  "video": "https://example.com/intro.mp4"
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---|---|
| `tutorId` | Long | 是 | 老師 ID |
| `name` | String | 否 | 更新 users 表的姓名 |
| `intro` | String | 否 | 更新 tutors 表的介紹 |
| `certificate` | String | 否 | 證照資訊 |
| `video` | String | 否 | 自我介紹影片連結 |

* **回應內容 (Response)**
- HTTP Status: `200 OK`（成功）
- Body:
```json
{
  "message": "個人檔案儲存成功！您的學生現在可以看到最新資訊了！"
}
```
- HTTP Status: `400 Bad Request`（未提供 tutorId）
- Body:
```json
{
  "message": "必須提供老師 ID"
}
```
- HTTP Status: `404 Not Found`（找不到該老師）
- Body:
```json
{
  "message": "更新失敗，找不到該名老師"
}
```

---

### 3. 新增排班時段（TutorScheduleController）

* **請求資訊（HTTP Request）**
- Method: `POST`
- URL: `/api/teacher/schedules`
- Headers: `Content-Type: application/json`
- Payload (Request Body): `TutorSchedule` 實體
```json
{
  "tutorId": 2,
  "startTime": "2026-03-15T10:00:00",
  "endTime": "2026-03-15T11:00:00"
}
```

* **回應內容 (Response)**
- HTTP Status: `200 OK`（成功）
- Body:
```json
{
  "message": "排班成功！該時段已開放給家長預約。"
}
```
- HTTP Status: `400 Bad Request`（時段衝突或資料錯誤）
- Body:
```json
{
  "message": "（錯誤原因）"
}
```

---

### 4. 取得老師所有排班（TutorScheduleController）

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/teacher/schedules/{tutorId}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body:
```json
{
  "tutorId": 2,
  "schedules": [
    {
      "id": 1,
      "tutorId": 2,
      "startTime": "2026-03-15T10:00:00",
      "endTime": "2026-03-15T11:00:00"
    }
  ]
}
```

---

### 5. 老師送出課後回饋（TutorFeedbackController）

* **請求資訊（HTTP Request）**
- Method: `POST`
- URL: `/api/teacher/feedbacks`
- Headers: `Content-Type: application/json`
- Payload (Request Body): `LessonFeedback` 實體
```json
{
  "bookingId": 10,
  "rating": 4,
  "comment": "學生今天表現不錯，已達學習目標"
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---|---|
| `bookingId` | Long | 是 | 所屬 Booking 的 ID |
| `rating` | Integer | 是 | 評分（1-5） |
| `comment` | String | 否 | 課後回饋內容 |

* **回應內容 (Response)**
- HTTP Status: `200 OK`（成功）
- Body:
```json
{
  "message": "課後回饋送出成功！家長將會收到通知。"
}
```
- HTTP Status: `400 Bad Request`（評分不在範圍 / 已重複填寫）
- Body:
```json
{
  "message": "評分必須介於 1 到 5 之間"
}
```
```json
{
  "message": "這堂課已經填寫過回饋囉！"
}
```

* **限制**：每個 `bookingId` 只能提交一次課後回饋。
