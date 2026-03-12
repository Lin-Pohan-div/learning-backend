# 測試規格文件 - VideoRoomControllerTest

來源檔案: `src/test/java/com/learning/api/controller/VideoRoomControllerTest.java`

---

## 測試架構

| 項目 | 說明 |
|---|---|
| 測試類型 | 單元測試 (`@ExtendWith(MockitoExtension.class)`) |
| Mock 物件 | `SimpMessagingTemplate`、`ChatMessageService` |
| 注入方式 | `@InjectMocks VideoRoomController` |
| 測試 bookingId | `42L`（常數 `BOOKING_ID`） |
| 測試數量 | 16 個測試方法 |

> 與其他 Controller Test 不同，VideoRoomController 使用**純單元測試（Mockito）**，不啟動 Spring Context，無資料庫操作。

---

## 測試案例

### signal() — WebRTC 信令中繼

目標 Topic: `/topic/room/{bookingId}/signal`

| 測試方法 | 說明 | 驗證重點 |
|---|---|---|
| `signal_offer_shouldRelayToCorrectTopic` | type="offer"，senderRole=1 | `convertAndSend("/topic/room/42/signal", msg)` 被呼叫 |
| `signal_answer_shouldRelayToCorrectTopic` | type="answer"，senderRole=2 | 同上，topic 正確 |
| `signal_iceCandidate_shouldRelayToCorrectTopic` | type="candidate"，含 candidate/sdpMid/sdpMLineIndex | 同上，topic 正確 |
| `signal_shouldNotInteractWithChatService` | 傳送任意信令 | `chatMessageService` 完全未被呼叫 |
| `signal_differentBookingId_shouldUseDifferentTopic` | bookingId=99 | 發送到 `/topic/room/99/signal`，`/topic/room/42/signal` 不被呼叫 |

---

### chat() — 即時聊天訊息（文字）

目標 Topic: `/topic/room/{bookingId}/chat`

| 測試方法 | 說明 | 驗證重點 |
|---|---|---|
| `chat_textMessage_shouldSaveAndBroadcast` | TEXT 訊息 | `chatMessageService.save(bookingId, 1, 1, "Hello!", null)` 被呼叫，結果廣播到 topic |
| `chat_nullMessageType_shouldDefaultToText` | `messageType=null` | 自動帶入 `MessageType.TEXT.getValue()=1` 呼叫 save |
| `chat_broadcastContainsPersistedEntity` | 廣播內容 | 廣播的 `ChatMessage` id 與 save 回傳的實體一致（id=99） |
| `chat_shouldNotBroadcastToSignalOrEventsTopic` | 不污染其他 topic | `/topic/room/42/signal` 和 `/topic/room/42/events` 不被呼叫 |

---

### chat() — 即時聊天訊息（媒體）

| 測試方法 | messageType | mediaUrl | 驗證重點 |
|---|---|---|---|
| `chat_stickerMessage_shouldSaveAndBroadcast` | 2（STICKER） | `stickers/001.png` | `save(id, 2, 2, null, url)` 被呼叫，廣播到 topic |
| `chat_voiceMessage_shouldSaveAndBroadcast` | 3（VOICE） | `audio/001.mp3` | `save(id, 2, 3, null, url)` 被呼叫 |
| `chat_imageMessage_shouldSaveAndBroadcast` | 4（IMAGE） | `images/001.jpg` | `save(id, 2, 4, null, url)` 被呼叫 |
| `chat_videoMessage_shouldSaveAndBroadcast` | 5（VIDEO） | `videos/001.mp4` | `save(id, 2, 5, null, url)` 被呼叫 |

---

### event() — 房間加入/離開事件

目標 Topic: `/topic/room/{bookingId}/events`

| 測試方法 | 說明 | 驗證重點 |
|---|---|---|
| `event_joined_shouldBroadcastToCorrectTopic` | type="joined"，role=1 | `convertAndSend("/topic/room/42/events", event)` 被呼叫 |
| `event_left_shouldBroadcastToCorrectTopic` | type="left"，role=2 | 同上，topic 正確 |
| `event_shouldNotInteractWithChatService` | 傳送任意事件 | `chatMessageService` 完全未被呼叫 |
| `event_differentBookingId_shouldUseDifferentTopic` | bookingId=100 | 發送到 `/topic/room/100/events`，`/topic/room/42/events` 不被呼叫 |
