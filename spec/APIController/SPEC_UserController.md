# 規格文件 - UserController

來源檔案: `src/main/java/com/learning/api/controller/UserController.java`

Base URL: `http://localhost:8080`

---

## 概述

處理使用者身份驗證的 REST API，包含註冊與登入功能。

---

## API 互動邏輯

### 1. 會員註冊

* **請求資訊（HTTP Request）**
- Method: `POST`
- URL: `/api/auth/register`
- Headers: `Content-Type: application/json`
- Payload (Request Body):
```json
{
  "email": "user@example.com",
  "password": "securePassword123",
  "role": "STUDENT"
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---|---|
| `email` | String | 是 | 電子郵件，須唯一 |
| `password` | String | 是 | 密碼 |
| `role` | String | 是 | 角色（例如 STUDENT、TUTOR） |

* **回應內容 (Response)**
- HTTP Status: `200 OK`（成功）
- Body:
```json
{
  "message": "歡迎"
}
```
- HTTP Status: `400 Bad Request`（例如 email 重複）
- Body:
```json
{
  "message": "註冊失敗"
}
```

---

### 2. 會員登入

* **請求資訊（HTTP Request）**
- Method: `POST`
- URL: `/api/auth/login`
- Headers: `Content-Type: application/json`
- Payload (Request Body):
```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---|---|
| `email` | String | 是 | 電子郵件 |
| `password` | String | 是 | 密碼 |

* **回應內容 (Response)**
- HTTP Status: `200 OK`（成功）
- Body:
```json
{
  "message": "歡迎"
}
```
- HTTP Status: `401 Unauthorized`（帳號或密碼錯誤）
- Body:
```json
{
  "message": "帳號或密碼錯誤"
}
```

---

## 資料庫欄位說明

| 實體 | 資料表 | 主鍵 | 重要欄位 |
|---|---|---|---|
| `User` | `users` | `id` (Long, 自動生成) | `email` (不可為空, 唯一)、`password` (不可為空)、`role` (不可為空) |
