# 規格文件 - OrderController

來源檔案: `src/main/java/com/learning/api/controller/OrderController.java`

Base URL: `http://localhost:8080`

---

## 概述

管理訂單（Order）的 REST API，支援建立、查詢、更新、取消及支付訂單。

---

## 訂單狀態定義

| 值 | 狀態 | 說明 |
|----|------|------|
| 1 | pending | 待處理（可取消） |
| 2 | deal | 已成交 |
| 3 | complete | 已完成 |

---

## 折扣規則

購買堂數 >= 10 堂時，自動享 95 折優惠（`discountPrice = unitPrice × lessonCount × 0.95`）。

---

## API 互動邏輯

### 1. 新增訂單

* **請求資訊（HTTP Request）**
- Method: `POST`
- URL: `/api/orders`
- Headers: `Content-Type: application/json`
- Payload (Request Body):
```json
{
  "userId": 3,
  "courseId": 5,
  "lessonCount": 10
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---|---|
| `userId` | Long | 是 | 用戶 ID（未來改由 JWT 取得） |
| `courseId` | Long | 是 | 課程 ID |
| `lessonCount` | Integer | 是 | 購買堂數（最小值為 1，>= 10 享 95 折） |

* **回應內容 (Response)**
- HTTP Status: `200 OK`（成功）
- Body:
```json
{
  "message": "訂單建立成功"
}
```
- HTTP Status: `400 Bad Request`
- Body:
```json
{
  "message": "建立訂單失敗"
}
```

---

### 2. 查詢單一訂單

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/orders/{id}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body:
```json
{
  "id": 1,
  "userId": 3,
  "courseId": 5,
  "unitPrice": 700,
  "discountPrice": 6650,
  "lessonCount": 10,
  "lessonUsed": 3,
  "status": 2
}
```
- HTTP Status: `404 Not Found`
- Body:
```json
{
  "message": "訂單不存在"
}
```

---

### 3. 查詢使用者所有訂單

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/orders/user/{userId}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body: 同「查詢單一訂單」格式的陣列

---

### 4. 修改訂單（堂數 / 已使用堂數）

* **請求資訊（HTTP Request）**
- Method: `PUT`
- URL: `/api/orders/{id}`
- Headers: `Content-Type: application/json`
- Payload (Request Body):
```json
{
  "lessonCount": 15,
  "lessonUsed": 4
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---|---|
| `lessonCount` | Integer | 否 | 調整購買堂數（須 >= 已使用堂數，最小值 1） |
| `lessonUsed` | Integer | 否 | 更新已使用堂數（須 <= lessonCount，最小值 0） |

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body:
```json
{
  "message": "訂單更新成功"
}
```
- HTTP Status: `400 Bad Request`
- Body:
```json
{
  "message": "訂單更新失敗"
}
```

---

### 5. 更新訂單狀態

* **請求資訊（HTTP Request）**
- Method: `PATCH`
- URL: `/api/orders/{id}/status`
- Headers: `Content-Type: application/json`
- Payload (Request Body):
```json
{
  "status": 2
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---|---|
| `status` | Integer | 是 | 訂單狀態（1=pending, 2=deal, 3=complete） |

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body:
```json
{
  "message": "狀態更新成功"
}
```
- HTTP Status: `400 Bad Request`
- Body:
```json
{
  "message": "狀態更新失敗"
}
```

---

### 6. 取消訂單

* **請求資訊（HTTP Request）**
- Method: `DELETE`
- URL: `/api/orders/{id}`
- Payload: 無

* **限制**：僅限 `pending`（狀態=1）的訂單可取消。

* **回應內容 (Response)**
- HTTP Status: `200 OK`（成功）
- Body:
```json
{
  "message": "訂單已取消"
}
```
- HTTP Status: `400 Bad Request`（非 pending 狀態或找不到訂單）
- Body:
```json
{
  "message": "取消失敗，僅 pending 訂單可取消"
}
```

---

### 7. 支付訂單

* **請求資訊（HTTP Request）**
- Method: `POST`
- URL: `/api/orders/{id}/pay`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`（成功）
- Body:
```json
{
  "message": "支付成功"
}
```
- HTTP Status: `400 Bad Request`（支付失敗）
- Body:
```json
{
  "message": "支付失敗"
}
```
