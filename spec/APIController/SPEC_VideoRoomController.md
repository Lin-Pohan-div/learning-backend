# 規格文件 - VideoRoomController

來源檔案: `src/main/java/com/learning/api/controller/VideoRoomController.java`、`src/main/java/com/learning/api/config/WebSocketConfig.java`

---

## 概述

`VideoRoomController` 是視訊聊天室的 WebSocket 控制器，基於 STOMP over WebSocket（含 SockJS fallback）實現：
- WebRTC 信令中繼（offer / answer / ICE candidate）
- 即時聊天訊息（持久化至 DB 後廣播）
- 房間加入 / 離開事件廣播

---

## WebSocket 連線設定（WebSocketConfig）

| 項目 | 值 |
|------|----|
| 連線端點 | `/ws`（支援 SockJS fallback） |
| 客戶端發送前綴 | `/app` |
| 伺服器推送前綴 | `/topic` |
| 允許來源 | `*`（allowedOriginPatterns） |

---

## 訂閱主題（Subscribe Topics）

| 主題路徑 | 用途 |
|----------|------|
| `/topic/room/{bookingId}/signal` | 接收 WebRTC 信令（offer / answer / ICE candidate） |
| `/topic/room/{bookingId}/chat` | 接收即時聊天訊息（已持久化的 `ChatMessage`） |
| `/topic/room/{bookingId}/events` | 接收房間加入 / 離開事件 |

---

## WebSocket 互動邏輯（STOMP MessageMapping）

### 1. WebRTC 信令中繼

* **發送路徑（Client → Server）**
  - Destination: `/app/signal/{bookingId}`
  - Payload (`SignalingMessage`):

```json
{
  "type": "offer | answer | candidate",
  "senderRole": 1,
  "sdp": "SDP 字串（offer / answer 使用）",
  "candidate": "ICE candidate 字串（candidate 使用）",
  "sdpMid": "ICE candidate sdpMid（candidate 使用）",
  "sdpMLineIndex": 0
}
```

* **廣播目標（Server → Clients）**
  - Destination: `/topic/room/{bookingId}/signal`
  - Payload: 原封不動轉發 `SignalingMessage`

* **處理邏輯**
  - 伺服器僅做純中繼（relay），不做任何訊息轉換或過濾
  - 房間內所有訂閱者均會收到此信令

* **欄位說明**

| 欄位 | 類型 | 說明 |
|------|------|------|
| `type` | String | `"offer"` / `"answer"` / `"candidate"` |
| `senderRole` | Integer | 1 = 學生, 2 = 導師 |
| `sdp` | String | SDP 描述，offer / answer 時使用 |
| `candidate` | String | ICE candidate 字串，candidate 時使用 |
| `sdpMid` | String | ICE candidate sdpMid |
| `sdpMLineIndex` | Integer | ICE candidate sdpMLineIndex |

---

### 2. 即時聊天訊息

* **發送路徑（Client → Server）**
  - Destination: `/app/chat/{bookingId}`
  - Payload (`ChatMessageRequest`):

```json
{
  "bookingId": 1,
  "role": 1,
  "messageType": 1,
  "message": "訊息內容（text 類型使用）",
  "mediaUrl": "https://example.com/media.jpg（media 類型使用）"
}
```

* **廣播目標（Server → Clients）**
  - Destination: `/topic/room/{bookingId}/chat`
  - Payload: 持久化後的 `ChatMessage` 實體

```json
{
  "id": 42,
  "orderId": 1,
  "role": 1,
  "messageType": 1,
  "message": "訊息內容",
  "mediaUrl": null,
  "createdAt": "2026-03-11T10:00:00"
}
```

* **處理邏輯**
  1. 若 `messageType` 為 null，預設為 `1`（TEXT）
  2. 呼叫 `ChatMessageService.save()` 驗證並持久化：
     - 驗證 `bookingId` 不為 null 且 > 0，否則拋出 `IllegalArgumentException`
     - 驗證 `bookingId` 對應的 Order 存在，否則拋出 `NoSuchElementException`
     - 透過 `MessageType.fromValue()` 解析 `messageType`，不支援的值拋出 `IllegalArgumentException`
     - 若 `MessageType.isMedia()` 為 true（非 TEXT），儲存 `mediaUrl`，否則儲存 `message`
  3. 將儲存後的 `ChatMessage` 廣播給房間所有訂閱者

* **訊息類型定義（MessageType enum）**

| 值 | 類型 | isMedia() |
|----|------|-----------|
| 1 | TEXT | false |
| 2 | STICKER | true |
| 3 | VOICE | true |
| 4 | IMAGE | true |
| 5 | VIDEO | true |

* **欄位說明（ChatMessageRequest）**

| 欄位 | 類型 | 說明 |
|------|------|------|
| `bookingId` | Long | 訂單/預約 ID |
| `role` | Integer | 1 = 學生, 2 = 導師 |
| `messageType` | Integer | 訊息類型（見上表），null 時預設 1 |
| `message` | String | 文字內容（TEXT 類型使用） |
| `mediaUrl` | String | 媒體 URL（非 TEXT 類型使用） |

* **ChatMessage 實體（chat_messages 資料表）**

| 欄位 | DB 欄位名 | 類型 | 說明 |
|------|-----------|------|------|
| `id` | id | Long | 主鍵（自增） |
| `orderId` | order_id | Long | 關聯訂單 ID |
| `role` | role | Integer | 1 = 學生, 2 = 導師 |
| `messageType` | message_type | Integer | 預設 1 |
| `message` | message | String | 最長 1000 字 |
| `mediaUrl` | media_url | String | 最長 500 字 |
| `createdAt` | created_at | LocalDateTime | 由 DB 自動寫入 |

---

### 3. 房間事件（加入 / 離開）

* **發送路徑（Client → Server）**
  - Destination: `/app/event/{bookingId}`
  - Payload (`RoomEvent`):

```json
{
  "type": "joined | left",
  "role": 1,
  "timestamp": "2026-03-11T10:00:00Z"
}
```

* **廣播目標（Server → Clients）**
  - Destination: `/topic/room/{bookingId}/events`
  - Payload: 原封不動轉發 `RoomEvent`（`timestamp` 由伺服器初始化為 `Instant.now()`）

* **處理邏輯**
  - 伺服器僅做純廣播，不做任何驗證或持久化
  - `timestamp` 欄位在 `RoomEvent` 建構時自動設為當前時間

* **欄位說明**

| 欄位 | 類型 | 說明 |
|------|------|------|
| `type` | String | `"joined"` 或 `"left"` |
| `role` | Integer | 1 = 學生, 2 = 導師 |
| `timestamp` | Instant | 由物件初始化時自動設定 |

---

## 資料流程圖

```
客戶端 A                 伺服器                          客戶端 B
   │                       │                              │
   │── /app/signal/{id} ──>│                              │
   │                       │── /topic/room/{id}/signal ──>│
   │                       │                              │
   │── /app/chat/{id}  ───>│                              │
   │                       │── ChatMessageService.save() ──> DB
   │                       │── /topic/room/{id}/chat ────>│
   │                       │                              │
   │── /app/event/{id}  ──>│                              │
   │                       │── /topic/room/{id}/events ──>│
```

---

## 其他重要功能或邏輯

### 媒體 vs 文字訊息的儲存策略
- **功能**：`MessageType.isMedia()` 決定儲存欄位
- **描述**：當訊息類型為 TEXT（值=1）時，儲存 `message` 欄位；其餘媒體類型（STICKER、VOICE、IMAGE、VIDEO）時，儲存 `mediaUrl` 欄位，兩欄位互斥儲存。
- **相關程式碼（ChatMessageService.java:39-43）**：
```java
if (type.isMedia()) {
    chatMessage.setMediaUrl(mediaUrl);
} else {
    chatMessage.setMessage(message);
}
```

### SockJS Fallback
- **功能**：連線降級支援
- **描述**：`WebSocketConfig` 啟用 SockJS，當瀏覽器不支援原生 WebSocket 時，自動降級使用 HTTP long-polling 等輪詢方式，保持連線相容性。

### 信令不做持久化
- **描述**：`signal()` 與 `event()` 方法僅做廣播，不寫入資料庫；只有 `chat()` 方法會透過 `ChatMessageService` 進行持久化。這樣設計避免了對即時性要求高但無需留存的信令資料造成 DB 負擔。
