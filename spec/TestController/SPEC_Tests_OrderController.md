# 測試規格文件 - OrderControllerTest

來源檔案: `src/test/java/com/learning/api/controller/OrderControllerTest.java`

---

## 測試架構

| 項目 | 說明 |
|---|---|
| 測試類型 | 整合測試 (`@SpringBootTest`) |
| HTTP 測試 | MockMvc (`webAppContextSetup`) |
| 交易管理 | `@Transactional`（每個測試後自動 rollback） |
| 測試數量 | 20 個測試方法 |

---

## 前置設定（`@BeforeEach`）

每個測試執行前依序建立：
1. 測試使用者 `testUser`（role=1，email=`student_order@example.com`，wallet=0）
2. 無訂單使用者 `noOrderUser`（role=1，email=`noorder_student@example.com`）
3. 啟用課程 `activeCourse`（active=true，price=500）
4. 停用課程 `inactiveCourse`（active=false，price=500）
5. `pendingOrder`（status=1，lessonCount=5，lessonUsed=0）
6. `dealOrder`（status=2，lessonCount=5，lessonUsed=0）
7. `completeOrder`（status=3，lessonCount=5，lessonUsed=5）

---

## 訂單狀態說明

| status 值 | 說明 |
|---|---|
| 1 | pending（待付款） |
| 2 | deal（已付款） |
| 3 | complete（已完成） |

---

## 測試案例

### POST /api/orders — 建立訂單

| 測試方法 | Payload | 預期結果 |
|---|---|---|
| `createOrder_validRequest_shouldReturn200` | 有效 userId、courseId（啟用）、lessonCount=5 | 200，`$.message="訂單建立成功"` |
| `createOrder_10Lessons_shouldReturn200` | 有效 userId、courseId（啟用）、lessonCount=10 | 200，`$.message="訂單建立成功"` |
| `createOrder_nonExistentUser_shouldReturn400` | userId=999999（不存在） | 400，`$.message="建立訂單失敗"` |
| `createOrder_inactiveCourse_shouldReturn400` | courseId 對應停用課程 | 400，`$.message="建立訂單失敗"` |

---

### GET /api/orders/{id} — 查詢單筆訂單

| 測試方法 | 說明 | 預期結果 |
|---|---|---|
| `getOrder_existingId_shouldReturn200` | 有效 id（pendingOrder） | 200，驗證 `$.id`、`$.userId`、`$.courseId`、`$.status=1` |
| `getOrder_nonExistentId_shouldReturn404` | id=999999（不存在） | 404，`$.message="訂單不存在"` |

---

### GET /api/orders/user/{userId} — 查詢使用者所有訂單

| 測試方法 | 說明 | 預期結果 |
|---|---|---|
| `getOrdersByUser_existingUser_shouldReturnList` | 有訂單的使用者 | 200，陣列 size>=3 |
| `getOrdersByUser_noOrders_shouldReturnEmptyList` | 無訂單的使用者 | 200，空陣列 `[]` |

---

### PUT /api/orders/{id} — 更新訂單

Request Body: `{ "lessonCount": 8 }`

| 測試方法 | 說明 | 預期結果 |
|---|---|---|
| `updateOrder_lessonCount_shouldReturn200` | pending 訂單更新 lessonCount | 200，`$.message="訂單更新成功"` |
| `updateOrder_completeStatus_shouldReturn400` | complete 訂單不可更新 | 400，`$.message="訂單更新失敗"` |
| `updateOrder_nonExistentId_shouldReturn400` | id=999999（不存在） | 400，`$.message="訂單更新失敗"` |

---

### PATCH /api/orders/{id}/status — 更新訂單狀態

Request Body: `{ "status": 2 }`

| 測試方法 | 說明 | 預期結果 |
|---|---|---|
| `updateStatus_pendingToDeal_shouldReturn200` | status 1→2（正向推進） | 200，`$.message="狀態更新成功"` |
| `updateStatus_backward_shouldReturn400` | status 2→1（逆向回退） | 400，`$.message="狀態更新失敗"` |
| `updateStatus_nonExistentId_shouldReturn400` | id=999999（不存在） | 400，`$.message="狀態更新失敗"` |

---

### DELETE /api/orders/{id} — 取消訂單

| 測試方法 | 說明 | 預期結果 |
|---|---|---|
| `cancelOrder_pendingStatus_shouldReturn200` | pending 訂單可取消 | 200，`$.message="訂單已取消"` |
| `cancelOrder_dealStatus_shouldReturn400` | deal 訂單不可取消 | 400，`$.message="取消失敗，僅 pending 訂單可取消"` |
| `cancelOrder_nonExistentId_shouldReturn400` | id=999999（不存在） | 400，`$.message="取消失敗，僅 pending 訂單可取消"` |

---

### POST /api/orders/{id}/pay — 支付訂單

| 測試方法 | 說明 | 預期結果 |
|---|---|---|
| `payOrder_pendingStatus_shouldReturn200` | pending 訂單可支付 | 200，`$.message="支付成功"` |
| `payOrder_dealStatus_shouldReturn400` | deal 訂單不可重複支付 | 400，`$.message="支付失敗"` |
| `payOrder_nonExistentId_shouldReturn400` | id=999999（不存在） | 400，`$.message="支付失敗"` |

---

## 重要驗證邏輯

- 訂單狀態只能正向推進（1→2→3），不可逆向回退
- 只有 pending（status=1）訂單可執行取消與支付
- complete（status=3）訂單不可更新 lessonCount
- GET 查詢不存在的訂單回傳 404（其他失敗操作回傳 400）
- 使用者無訂單時 GET /user/{userId} 回傳空陣列（非 404）
