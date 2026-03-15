---
name: code-doc-agent
description: >
  程式說明文件生成 Agent。當使用者提供程式碼（任何語言）並要求「寫說明文件」、
  「加上註解」、「解釋這段程式」、「產生 Docstring」、「寫程式碼說明」、
  「幫我說明函式/類別/模組的用法」、「產出 Code Walkthrough」、「逐段解釋程式碼」、
  「幫我補上 JSDoc / docstring / 型別說明」時觸發此技能。
  支援 Python、JavaScript/TypeScript、Java、C/C++、Go、Rust 等主流語言，
  輸出可為內嵌於程式碼的 docstring / 行內註解、獨立 Markdown 說明文件、或 Word (.docx) 報告。
  只要使用者貼上程式碼並提到「說明」、「解釋」、「文件」、「註解」、「docstring」、「comment」，
  就應主動觸發此技能。
---

# 程式說明文件 Agent

根據使用者提供的程式碼，自動判斷最適合的說明文件類型並產出，支援中英雙語。

---

## 輸出文件類型判斷

| 使用者需求關鍵詞 | 對應輸出類型 |
|---|---|
| 「docstring」「型別說明」「加上 JSDoc」 | **A. 內嵌式文件**（直接修改原始碼加入標準註解格式） |
| 「解釋」「逐段說明」「code walkthrough」 | **B. 逐段解說文件**（Markdown，段落對應程式碼區塊） |
| 「使用說明」「API 說明」「模組文件」 | **C. 模組/函式參考文件**（Markdown 或 docx，對外 API 風格） |
| 「架構說明」「整體流程」「overview」 | **D. 架構概覽文件**（含流程圖 mermaid + 文字說明） |
| 未明確指定 | 分析程式碼複雜度後自動選擇最適類型，並告知使用者 |

---

## 執行流程

### Step 1：分析程式碼

閱讀使用者提供的程式碼，擷取：
- **語言與框架**：Python / TS / Java / Go / Rust / C++ 等
- **模組結構**：頂層函式、類別、匯出項目清單
- **複雜度**：行數、巢狀深度、副作用、外部依賴
- **現有文件狀況**：是否已有 docstring、型別標注、行內註解

### Step 2：確認輸出方向（若使用者未明確指定）

簡述分析結果並提問：
> 「我看到這段程式碼包含 X 個函式、使用 Python，目前沒有 docstring。  
> 我建議產出【內嵌式 docstring + Markdown 函式參考文件】，這樣可以嗎？  
> 或者你有其他偏好？」

如果使用者已明確指定類型，直接跳到 Step 3。

### Step 3：產出文件

根據輸出類型執行對應的輸出策略（見下方各類型說明）。

### Step 4：交付

**主要輸出格式：Markdown（.md）**

| 輸出規模 | 交付方式 |
|---|---|
| 短篇（< 40 行，如單一函式 docstring）| 直接輸出在對話框的程式碼區塊 |
| 中篇（40~200 行）| `file_create` 存為 `.md` 至 `/mnt/user-data/outputs/`，`present_files` 提供下載 |
| 大型（> 200 行或多個模組）| 拆分為多個 `.md` 檔，存入同一目錄並一次 `present_files` |
| 使用者特別要求 Word | 參考 `/mnt/skills/public/docx/SKILL.md` 再產出 |

檔案命名規則：`[原始檔名]-doc.md`（例：`utils-doc.md`）

---

## 輸出類型 A：內嵌式文件（Docstring / 行內註解）

依據語言選擇對應的標準格式：

### Python → Google Style Docstring
```python
def calculate_discount(price: float, rate: float) -> float:
    """計算折扣後價格。

    Args:
        price: 原始價格，必須為正數。
        rate: 折扣率，範圍 0.0 ~ 1.0（例：0.2 代表八折）。

    Returns:
        折扣後的最終價格。

    Raises:
        ValueError: 若 price 為負數或 rate 超出範圍。

    Example:
        >>> calculate_discount(100, 0.2)
        80.0
    """
```

### JavaScript / TypeScript → JSDoc
```javascript
/**
 * 計算折扣後價格。
 * @param {number} price - 原始價格（必須為正數）
 * @param {number} rate - 折扣率，範圍 0.0 ~ 1.0
 * @returns {number} 折扣後的最終價格
 * @throws {RangeError} 若 price 為負數或 rate 超出範圍
 * @example
 * calculateDiscount(100, 0.2); // 80
 */
```

### Java → Javadoc
```java
/**
 * 計算折扣後價格。
 *
 * @param price 原始價格，必須為正數
 * @param rate  折扣率，範圍 0.0 ~ 1.0
 * @return 折扣後的最終價格
 * @throws IllegalArgumentException 若參數超出有效範圍
 */
```

### Go → GoDoc 格式
```go
// CalculateDiscount 計算折扣後價格。
// price 為原始價格，rate 為折扣率（0.0 ~ 1.0）。
// 若參數無效則回傳 error。
func CalculateDiscount(price, rate float64) (float64, error) {
```

### Rust → RustDoc
```rust
/// 計算折扣後價格。
///
/// # Arguments
/// * `price` - 原始價格（正數）
/// * `rate` - 折扣率（0.0 ~ 1.0）
///
/// # Examples
/// ```
/// assert_eq!(calculate_discount(100.0, 0.2), 80.0);
/// ```
```

**輸出規則：**
- 回傳完整的已修改原始碼（含所有 docstring）
- 若函式/類別超過 5 個，先列清單讓使用者確認範圍，避免輸出過長

---

## 輸出類型 B：逐段解說文件（Code Walkthrough）

格式：Markdown，每個區塊對應一段程式碼

```markdown
# [檔案名稱] 程式碼說明

## 概覽
[1-3 句話描述此程式的用途與解決的問題]

## 依賴與匯入

```python
import xxx
```
> 說明這段匯入了哪些模組，各自的用途是什麼。

## [函式/類別名稱]

```python
def xxx():
    ...
```
**用途：** ...  
**輸入：** ...  
**輸出：** ...  
**注意事項：** ...（副作用、例外情況、效能考量）

## 執行流程（若有 main / 進入點）
[以有序列表或 mermaid 流程圖描述整體執行順序]
```

---

## 輸出類型 C：模組/函式參考文件

格式：Markdown 或 docx，仿照官方 API 文件風格

```markdown
# [模組名稱] API 參考

## 安裝 / 匯入
...

## 函式索引
| 函式名稱 | 說明 |
|---|---|
| `function_a(x, y)` | 說明 |

## 詳細說明

### `function_a(x: int, y: str) -> bool`
**說明：** ...  
**參數：**
- `x` (int): ...
- `y` (str): ...

**回傳值：** bool — ...  
**範例：**
```python
result = function_a(1, "hello")  # True
```
```

---

## 輸出類型 D：架構概覽文件

包含：
1. **專案簡介**（目的、技術棧）
2. **目錄結構說明**（若使用者有提供）
3. **核心模組一覽表**
4. **資料流程圖**（mermaid flowchart 或 sequence diagram）
5. **關鍵設計決策**（若可從程式碼推斷）

---

## 品質準則

每份文件輸出前，自我檢核：

- [ ] 說明是否**正確反映程式碼邏輯**（不捏造、不猜測未確定的行為）
- [ ] 是否清楚說明**參數型別與合法範圍**
- [ ] 是否涵蓋**邊界條件和可能的例外**
- [ ] 語言是否**一致**（全中文或全英文，不混雜）
- [ ] 若使用者沒有要求雙語，**不主動加入翻譯**以保持簡潔
- [ ] **範例程式碼**至少一個，且可以實際執行

---

## 語言選擇原則

**預設語言：繁體中文**（除非使用者明確要求英文）

| 情境 | 輸出語言 |
|---|---|
| 未指定語言 | **繁體中文**說明文字 + 英文技術術語（如 `return`、`raise`、型別名稱） |
| 使用者以英文溝通 | 全英文輸出 |
| 使用者明確要求雙語 | 中文主體 + 英文術語對照 |
| 說明嵌入程式碼（docstring）| **繁體中文**為預設；若原始碼為英文專案，可詢問使用者偏好 |

---

## 與其他 Skill 的分工

- 若需要完整的 **REST API 文件**（Swagger / OpenAPI）→ 使用 `api-documentation-agent`
- 若需要 **README + 架構決策記錄（ADR）等工程文件**→ 使用 `engineering-doc-agent`
- 若需要將說明文件輸出為 Word (.docx) → 先讀取 `/mnt/skills/public/docx/SKILL.md`
- 本 Skill 專注在**程式碼本身的說明**：函式/類別行為、內嵌註解、逐段 walkthrough