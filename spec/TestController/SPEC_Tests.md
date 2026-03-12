# 測試規格文件索引

---

## 測試架構概述

| 項目 | 說明 |
|---|---|
| 測試框架 | JUnit 5 (Jupiter) |
| 整合測試 | Spring Boot Test (`@SpringBootTest`) + MockMvc |
| 單元測試 | Mockito (`@ExtendWith(MockitoExtension.class)`) |
| JSON 序列化 | Jackson `ObjectMapper` |
| 斷言函式庫 | Hamcrest Matchers、AssertJ |
| 交易管理 | `@Transactional`（整合測試，每個測試後自動 rollback） |

---

## 測試文件一覽

| 測試檔案 | 測試類型 | 測試數量 | 規格文件 |
|---|---|---|---|
| `ChatMessageControllerTest` | 整合測試 | 17 | [SPEC_Tests_ChatMessageController.md](./SPEC_Tests_ChatMessageController.md) |
| `LessonFeedbackControllerTest` | 整合測試 | 16 | [SPEC_Tests_FeedbackController.md](./SPEC_Tests_FeedbackController.md) |
| `ReviewControlTest` | 整合測試 | 14 | [SPEC_Tests_ReviewController.md](./SPEC_Tests_ReviewController.md) |
| `VideoRoomControllerTest` | 單元測試（Mockito） | 16 | [SPEC_Tests_VideoRoomController.md](./SPEC_Tests_VideoRoomController.md) |
| `ApiApplicationTests` | Context 載入測試 | 1 | 見下方說明 |

**總計：64 個測試方法**

---

## 共用測試模式

### 交易自動回滾（整合測試）

所有整合測試類別標註 `@Transactional`，每個測試後 DB 變更自動回滾，測試間互不影響。

```java
@SpringBootTest
@Transactional
class SomeControllerTest { ... }
```

### 前置資料依序建立（整合測試）

整合測試透過 `@BeforeEach` 建立外鍵相依的測試資料，順序為：

```
User → Course → Order → Booking → 主要測試實體
```

### 單元測試不啟動 Context（VideoRoomController）

`VideoRoomControllerTest` 使用 Mockito 直接注入 Mock，不啟動 Spring Context 也不存取資料庫，執行速度快。

---

## HTTP 狀態碼規範

| 操作 | 狀態碼 | 說明 |
|---|---|---|
| GET（成功） | `200 OK` | 回傳資源或空陣列 |
| POST（成功） | `201 Created` | 建立成功，回傳新資源 |
| PUT（成功） | `200 OK` | 更新成功，回傳更新後資源 |
| DELETE（成功） | `204 No Content` | 刪除成功，無 body |
| 資源不存在 | `404 Not Found` | ID 查無對應資源 |
| 驗證失敗 | `400 Bad Request` | 必填欄位缺失或格式不合法 |

---

## 應用程式啟動測試

**來源**: `src/test/java/com/learning/api/ApiApplicationTests.java`

`contextLoads()` 確認 Spring Boot 應用程式能成功啟動並載入所有 Bean，為最基礎的健康確認測試。

```java
@SpringBootTest
class ApiApplicationTests {
    @Test
    void contextLoads() { }
}
```
