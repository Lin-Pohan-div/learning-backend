# 規格文件 - ChatMessageController

來源檔案: `src/main/java/com/learning/api/controller/ChatMessageController.java`

Base URL: `http://localhost:8080`

---

## 概述

管理課堂聊天訊息的 REST API，支援文字與多媒體訊息（圖片、影片、語音、貼圖）。

---

## 訊息類型定義（MessageType enum）

| 值 | 類型 | isMedia() | 說明 |
|----|------|-----------|------|
| 1 | TEXT | false | 文字訊息（預設） |
| 2 | STICKER | true | 貼圖 |
| 3 | VOICE | true | 語音 |
| 4 | IMAGE | true | 圖片 |
| 5 | VIDEO | true | 影片 |

---

## API 互動邏輯

### 1. 取得指定 Booking 的所有聊天訊息

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/chatMessage/booking/{bookingId}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body:
```json
[
  {
    "id": 1,
    "orderId": 10,
    "role": 1,
    "messageType": 1,
    "message": "你好，請問有問題嗎？",
    "mediaUrl": null,
    "createdAt": "2026-03-09T10:00:00"
  }
]
```
- 資料解讀：依 `bookingId` 查詢所有對應的聊天訊息，以陣列形式回傳。

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
  "role": 1,
  "messageType": 1,
  "message": "你好，請問有問題嗎？",
  "mediaUrl": null
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---|---|
| `bookingId` | Long | 是 | 所屬 Booking 的 ID |
| `role` | Integer | 是 | 發訊者角色（1=學生, 2=導師） |
| `messageType` | Integer | 否 | 訊息類型（見上表），null 時預設 1（TEXT） |
| `message` | String | 條件必填 | 文字內容（TEXT 類型時必填，不得為空白） |
| `mediaUrl` | String | 條件必填 | 媒體 URL（非 TEXT 類型時必填，不得為空白） |

* **回應內容 (Response)**
- HTTP Status: `201 Created`
- Body:
```json
{
  "id": 1,
  "orderId": 10,
  "role": 1,
  "messageType": 1,
  "message": "你好，請問有問題嗎？",
  "mediaUrl": null,
  "createdAt": "2026-03-09T10:00:00"
}
```
- 錯誤回應 (400 Bad Request):
```json
{
  "message": "驗證失敗: Booking ID 不能為空"
}
```
- 驗證規則：`bookingId` 或 `role` 為 null → 400；TEXT 類型時 `message` 為空 → 400；媒體類型時 `mediaUrl` 為空 → 400；不支援的 `messageType` 值 → 400。

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
| `message` | String | 是 | 新的文字內容，不得為空白 |

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body: 更新後的 `ChatMessage` 實體
- 錯誤回應 (400 Bad Request):
```json
{
  "message": "驗證失敗: 消息內容不能為空"
}
```
- 錯誤回應 (404 Not Found): Body 為空

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

## 資料庫欄位說明

| 實體 | 資料表 | 主鍵 | 重要欄位 |
|---|---|---|---|
| `ChatMessage` | `chat_messages` | `id` (Long, 自動生成) | `order_id` (不可為空)、`role` (Integer, 不可為空)、`message_type` (Integer, 預設1)、`message` (最長 1000)、`media_url` (最長 500)、`created_at` (自動產生，唯讀) |
