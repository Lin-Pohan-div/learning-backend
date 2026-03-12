# 規格文件 - CourseController

來源檔案: `src/main/java/com/learning/api/controller/CourseController.java`

Base URL: `http://localhost:8080`

---

## 概述

處理課程（Course）建立的 REST API（學生端購買課程）。
老師新增課程請參考 [SPEC_TeacherController.md](./SPEC_TeacherController.md)。

---

## API 互動邏輯

### 1. 建立課程（購買）

* **請求資訊（HTTP Request）**
- Method: `POST`
- URL: `/api/courses`
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
  "message": "ok"
}
```
- HTTP Status: `400 Bad Request`（建立失敗）
- Body:
```json
{
  "message": "建立失敗"
}
```
