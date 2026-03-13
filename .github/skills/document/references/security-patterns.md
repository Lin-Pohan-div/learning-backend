# 安全風險掃描模式清單

## 靜態分析信號（程式碼層）

### 🔴 高危信號

| 模式 | 語言 | 說明 |
|------|------|------|
| `eval(user_input)` | JS/Python | 遠端代碼執行（RCE） |
| 直接字串拼接 SQL | 所有 | SQL 注入 |
| `md5()` / `sha1()` 用於密碼 | 所有 | 弱雜湊算法 |
| 硬編碼 secret/password | 所有 | 憑證洩漏 |
| `pickle.loads()` 處理用戶輸入 | Python | 反序列化漏洞 |
| `__dirname + req.params` | Node.js | 路徑遍歷 |
| `innerHTML = user_data` | JS | XSS |

### 🟡 中危信號

| 模式 | 說明 |
|------|------|
| JWT 無過期時間設定 | Token 永不失效 |
| 錯誤訊息包含堆疊追蹤 | 資訊洩漏 |
| 無 rate limiting 的驗證端點 | 暴力破解風險 |
| HTTP 而非 HTTPS 的內部服務 | 中間人攻擊 |
| CORS 設定 `*` | 跨站請求偽造風險 |
| Cookie 無 `HttpOnly` / `Secure` | Session 竊取 |

### 🟢 低危 / 建議改善

| 模式 | 說明 |
|------|------|
| 缺少 CSP Header | 降低 XSS 影響 |
| 缺少 HSTS | 強制 HTTPS |
| 依賴版本無固定 pin | 供應鏈攻擊風險 |
| Log 中記錄 PII | 個資洩漏風險 |

## 常見 CVE 影響套件（截至 2024）

| 套件 | 版本 | CVE | 嚴重度 |
|------|------|-----|--------|
| lodash | < 4.17.21 | CVE-2021-23337 | 高 |
| log4j | < 2.17.1 | CVE-2021-44228 | 嚴重 |
| moment.js | 任何版本 | 多個 ReDoS | 中 |
| axios | < 1.6.0 | CVE-2023-45857 | 中 |
| pillow（Python）| < 10.3.0 | CVE-2024-28219 | 高 |

> 建議使用 `npm audit` / `pip-audit` / `trivy` 取得最新 CVE 清單。

## OWASP Top 10 快速檢核清單（2021）

- [ ] A01：存取控制 — 所有 API 端點都有驗證與授權？
- [ ] A02：加密機制 — 敏感資料是否加密存儲與傳輸？
- [ ] A03：注入攻擊 — 所有輸入是否有參數化查詢或 sanitize？
- [ ] A04：不安全設計 — 威脅模型是否存在？
- [ ] A05：安全設定 — 生產環境是否關閉 debug mode？
- [ ] A06：易受攻擊元件 — 依賴是否定期掃描更新？
- [ ] A07：身份驗證 — 密碼是否用 bcrypt/argon2？MFA 是否支援？
- [ ] A08：軟體完整性 — CI/CD pipeline 是否有簽名驗證？
- [ ] A09：記錄監控 — 是否記錄安全相關事件（登入失敗等）？
- [ ] A10：SSRF — 外部 URL fetch 是否有白名單限制？