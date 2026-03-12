# 規格文件 - BookingController

來源檔案: `src/main/java/com/learning/api/controller/BookingController.java`

Base URL: `http://localhost:8080`

---

## 概述

處理課程預約（Booking）建立的 REST API。

---

## API 互動邏輯

### 1. 建立預約

* **請求資訊（HTTP Request）**
- Method: `POST`
- URL: `/api/bookings`
- Headers: `Content-Type: application/json`
- Payload (Request Body):
```json
{
  "userId": 1,
  "courseId": 5,
  "lessonCount": 10
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---|---|
| `userId` | Long | 是 | 預約者的用戶 ID（未來改由 JWT 取得） |
| `courseId` | Long | 是 | 預約的課程 ID |
| `lessonCount` | Integer | 是 | 預約堂數（最小值為 1） |

* **回應內容 (Response)**
- HTTP Status: `200 OK`（成功）
- Body:
```json
{
  "message": "建立成功"
}
```
- HTTP Status: `400 Bad Request`（建立失敗）
- Body:
```json
{
  "message": "建立失敗"
}
```
