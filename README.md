# Learning Backend – 開發說明文件

### 後端 ( Backend )
* **語言:** Java
* **框架:** Spring Boot
* **資料庫存取:** Spring Data JPA
* **開發工具:** IntelliJ IDEA

### 前端 ( Frontend )
* **建置工具:** Vite
* **樣式:** SCSS, Bootstrap
* **HTTP 客戶端:** Axios

---

### 環境需求
* 請確認 JDK 版本為 17 或以上
* 資料庫連線設定為 MySQL 8+

### 專案啟動步驟
**Terminal 輸入（ 僅專案初次啟動 ）**
1. `git clone https://github.com/lianne928/learning-backend.git`
2. 確認 `pom.xml` 依賴已完整下載
3. 檢查 `application.properties` 中的資料庫連線設定

### 資料庫連線
1. 啟動 MySQL
2. 修改 `application.properties` 資料庫以及帳密（ 此檔案 **絕對禁止** 推送上 GitHub )

### Mail 設定
若要啟用寄信功能，請提供以下環境變數。後端目前在 `application.properties` 中已預留對應設定，未提供時不會阻止 API 啟動，但測試寄信功能不會啟用。

`.env` 對照範例：

```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-account@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS_ENABLE=true
MAIL_SMTP_CONNECTION_TIMEOUT=5000
MAIL_SMTP_TIMEOUT=5000
MAIL_SMTP_WRITE_TIMEOUT=5000
```

對應到後端設定：

* `MAIL_HOST` -> `spring.mail.host`
* `MAIL_PORT` -> `spring.mail.port`
* `MAIL_USERNAME` -> `spring.mail.username`
* `MAIL_PASSWORD` -> `spring.mail.password`
* `MAIL_SMTP_AUTH` -> `spring.mail.properties.mail.smtp.auth`
* `MAIL_SMTP_STARTTLS_ENABLE` -> `spring.mail.properties.mail.smtp.starttls.enable`
* `MAIL_SMTP_CONNECTION_TIMEOUT` -> `spring.mail.properties.mail.smtp.connectiontimeout`
* `MAIL_SMTP_TIMEOUT` -> `spring.mail.properties.mail.smtp.timeout`
* `MAIL_SMTP_WRITE_TIMEOUT` -> `spring.mail.properties.mail.smtp.writetimeout`

注意：Spring Boot 預設不會自動載入 `.env` 檔。你需要用 IDE Run Configuration、PowerShell 環境變數、系統環境變數，或其他啟動工具先把這些值載入環境，再啟動後端。

---

### Git 開發流程（ 請務必遵守 ）
一定要在自己的 branch 上操作<br>
0. `git branch` 如果看到 `* main` 請停下動作，先按以下步驟建立 / 切換到 自己的分支
> 如何建立自己的分支? 
> 1. `git checkout main` 確保你拉到 main 而不是其他
> 2. `git pull origin main` 完成拉取動作 
> 3. `git checkout -b feature/功能名稱` 例如 `git checkout -b feature/auth-login` 建立並切換到新分支

1. `git status`：查看檔案狀態 ( 好東西請常用 )
2. `git add .`：將所有變更加入暫存區（ `.` 代表所有檔案 ）

> **💡 如果有不推送的檔案不小心 add 了怎麼辦？**
> 1. `git status` 查看狀態
> 2. 畫面會提示 `use "git restore --staged <file>..." to unstage`，底下會列出所有 add 的檔案
> 3. 複製你不打算推送的檔案名稱
> 4. 輸入 `git restore --staged <你不想推送的檔案>` 將其移出暫存區

3. `git status`：再確認一次狀態，確保準備提交的檔案都正確
4. `git commit -m "<Type>: <Description>"`：提交變更，Commit 訊息請參考下方規則
5. `git push origin 分支名稱`：推送到遠端儲存庫

---

### Git Commit Message Rules

1. **原子化提交 ( Atomic Commits )**           <br>
   假設你今天同時修復了 Bug ( fix ) 又新增了功能 ( feat )，請分開成兩次提交

2. **常用類型標籤 ( Type Tags )**
    * `feat`: 新增功能
    * `fix`: 修復 Bug
    * `docs`: 文件更動 ( 如修改 README )
    * `refactor`: 重構程式碼 ( 不影響原本功能的優化 )
    * `chore`: 雜項、套件或環境設定更動 ( 如修改 pom.xml 或 vite.config.js )
    * `revert`: 回滾


> Commit 總結 : <br>
只能有相關的 commit <br>
每個 commit 不要更動太多檔案，會難以閱讀
---

### 不該推送的檔案與 .gitignore 規範
為了保持儲存庫乾淨並保護敏感資訊，以下檔案 **絕對不要** 推送上去（ 請務必確保它們已寫入 `.gitignore` 檔案中 ）：

1. **敏感設定與密碼**
    * 包含真實資料庫帳密的 `application.properties`
    * 前端的 `.env` 環境變數檔
2. **編譯與打包產生的目錄**
    * 後端 ( Java/Spring Boot )：`target/` 或 `out/` 目錄
    * 前端 ( Vite )：`node_modules/` 以及打包後的 `dist/` 目錄
3. **IDE 與開發工具產生的隱藏檔**
    * IntelliJ IDEA 的 `.idea/` 目錄與 `*.iml` 檔
    * VS Code 的 `.vscode/` 目錄
4. **作業系統產生的系統檔**
    * Mac 產生的 `.DS_Store`
    * Windows 產生的 `Thumbs.db`
   
以下為 後端開發 .gitignore 範本
````
# =========================
# Backend (Java / Spring Boot)
# =========================
target/
*.class
*.jar
*.war
*.ear
*.nar
hs_err_pid*
replay_pid*

# 敏感設定檔
application.properties

# =========================
# IDE & Editors
# =========================
# IntelliJ IDEA
.idea/
*.iml
*.iws
*.ipr
out/

# VS Code
.vscode/
*.code-workspace

# =========================
# OS Files
# =========================
# macOS
.DS_Store
.AppleDouble
.LSOverride

# Windows
Thumbs.db
ehthumbs.db
Desktop.ini
$RECYCLE.BIN/
````


