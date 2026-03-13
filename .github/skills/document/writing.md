---
name: writing
description: 根據檔案內容撰寫說明文件
---

# 目的
- 透過閱讀與分析 HTML/JS 原始碼，產生該網頁的技術規格文件，並依照Controller分別存檔於 `/spec/SPEC_{目標檔案主檔名}.md`。
- 此文件旨在提供開發者一個清晰的實作指引與維護參考，包含資料流、資料流程圖、互動邏輯與 API 規格。

# 輸出文件要求與格式
- Web API 呼叫必須 **實際執行** 或根據 **使用者指定檔案** 中查找，不可僅憑推測或假設
- Web API 的回傳結果必須完整列出，不可擷取片段或省略重要資訊
- 請嚴格依照以下 Markdown 結構進行輸出：
```markdown

# 規格文件 - {網頁檔名}
來源檔案: `web/{RatingsTester.html}`、`web/{ChatMessageTester.html}`
---
## API 互動邏輯 (fetch)
針對頁面中每一個 Web API 呼叫（fetch/XHR），填寫以下資訊。

### 1. {API 名稱或用途}
* **請求資訊（HTTP Request）**
- Method: `POST` / `GET`
- URL: `{API_URL_VARIABLE}`
- Headers: `Content-Type: application/json`, ...
- Payload (Request Body):
```json
{
"key": "value description"
}
```
* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body
```json
[
{
"field1": "value",
"complexObj": { ... }
}
]
```
- 資料解讀與處理邏輯：（說明前端收到資料後做了什麼重要的轉換，例如日期格式化、欄位計算、過濾無效資料等）
- 顯示邏輯：（說明資料如何被渲染到頁面上，例如使用了哪些 HTML 元素、CSS 樣式、JavaScript 事件等）
- 其他重要細節：（例如錯誤處理、重試機制、性能優化等）
- **注意**：請確保所有資訊均來自實際程式碼分析，並且完整呈現 API 的行為與前端處理邏輯。
-

## 其他重要功能或邏輯
- 功能/邏輯名稱：{功能或邏輯詳述}
- 描述：{詳細描述該功能或邏輯的行為、目的與實作細節}
- 相關程式碼片段：（提供關鍵程式碼片段以說明該功能或邏輯的實作方式，並附上註解說明）


```