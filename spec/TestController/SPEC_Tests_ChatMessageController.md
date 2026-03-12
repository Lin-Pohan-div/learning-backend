# 測試規格文件 - ChatMessageControllerTest

來源檔案: `src/test/java/com/learning/api/controller/ChatMessageControllerTest.java`

---

## 測試架構

| 項目 | 說明 |
|---|---|
| 測試類型 | 整合測試 (`@SpringBootTest`) |
| HTTP 測試 | MockMvc (`webAppContextSetup`) |
| 交易管理 | `@Transactional`（每個測試後自動 rollback） |
| 測試數量 | 17 個測試方法 |

---

## 前置設定（`@BeforeEach`）

每個測試執行前依序建立：
1. 測試使用者（role=2，家教角色，email=`testtutor@example.com`）
2. 測試課程（綁定上述使用者，price=500）
3. 測試訂單 `testBooking`（`Order`，status=1）
4. 初始聊天訊息 `savedMessage`（role=1，message="Initial message"）

---

## 測試案例

### GET — 取得預約聊天訊息

URL: `GET /api/chatMessage/booking/{bookingId}`

| 測試方法 | 說明 | 預期結果 |
|---|---|---|
| `getByBookingId_existingBooking_shouldReturnMessages` | 有效 bookingId | 200，陣列 size>=1，驗證 `orderId`、`role=1`、`message="Initial message"` |
| `getByBookingId_noMessages_shouldReturnEmptyList` | 刪除所有訊息後查詢 | 200，空陣列 `[]` |
| `getByBookingId_messagesOrderedByCreatedAtAsc` | 新增第二筆後查詢 | 200，size=2，`$[0].message="Initial message"`、`$[1].message="Second message"` |

---

### POST — 建立文字訊息

URL: `POST /api/chatMessage`

Request Body 範例:
```json
{ "bookingId": 1, "role": 1, "message": "Hello tutor" }
```

| 測試方法 | Payload | 預期結果 |
|---|---|---|
| `post_validRequest_studentRole_shouldReturn201` | role=1, message="Hello tutor" | 201，驗證 `id`、`orderId`、`role=1`、`message` |
| `post_validRequest_tutorRole_shouldReturn201` | role=2, message="Hello student" | 201，`role=2`、`message="Hello student"` |
| `post_missingBookingId_shouldReturn400` | 缺少 bookingId | 400，`$.message` 含 "Booking ID" |
| `post_missingRole_shouldReturn400` | 缺少 role | 400，`$.message` 含 "Role" |
| `post_emptyMessage_shouldReturn400` | message="   "（空白） | 400，`$.message` 含 "消息內容" |
| `post_nonExistingBookingId_shouldReturn404` | bookingId=999999 | 404 Not Found |

---

### POST — 建立媒體訊息

| 測試方法 | messageType | mediaUrl | 預期結果 |
|---|---|---|---|
| `post_stickerMessage_shouldReturn201` | 2（貼圖） | `https://example.com/stickers/001.png` | 201，`messageType=2`、`mediaUrl` 正確 |
| `post_voiceMessage_shouldReturn201` | 3（語音） | `https://example.com/audio/001.mp3` | 201，`messageType=3`、`mediaUrl` 正確 |
| `post_imageMessage_shouldReturn201` | 4（圖片） | `https://example.com/images/001.jpg` | 201，`messageType=4`、`mediaUrl` 正確 |
| `post_videoMessage_shouldReturn201` | 5（影片） | `https://example.com/videos/001.mp4` | 201，`messageType=5`、`mediaUrl` 正確 |
| `post_stickerWithoutMediaUrl_shouldReturn400` | 2（貼圖），無 mediaUrl | 無 | 400，`$.message` 含 "貼圖" |
| `post_invalidMessageType_shouldReturn400` | 99（不合法） | 任意 URL | 400 |

---

### PUT — 更新訊息

URL: `PUT /api/chatMessage/{id}`

Request Body: `{ "message": "Updated message content" }`

| 測試方法 | 說明 | 預期結果 |
|---|---|---|
| `put_existingId_shouldReturn200WithUpdatedMessage` | 有效 id | 200，`$.message="Updated message content"`，`$.id` 和 `$.orderId` 不變 |
| `put_nonExistingId_shouldReturn404` | id=999999 | 404 Not Found |
| `put_emptyMessage_shouldReturn400` | message="  " | 400，`$.message` 含 "消息內容" |

---

### DELETE — 刪除訊息

URL: `DELETE /api/chatMessage/{id}`

| 測試方法 | 說明 | 預期結果 |
|---|---|---|
| `delete_existingId_shouldReturn204` | 有效 id | 204 No Content |
| `delete_nonExistingId_shouldReturn404` | id=999999 | 404 Not Found |
| `delete_thenGetByBookingId_shouldReturnEmptyList` | 刪除後再 GET | 204 → 再 GET 回傳空陣列 |

---

## 重要驗證邏輯

- ChatMessage 回應欄位為 `orderId`（非 `bookingId`）
- 媒體訊息（messageType != 1）需提供 `mediaUrl`，缺少時回傳 400
- 訊息排序依 `createdAt` 升冪
