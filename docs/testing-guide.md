# Learning Platform Backend — 測試指南

本文件說明本專案的測試策略、測試結構、慣用寫法，以及如何為新功能補充測試。

---

## 目錄

1. [測試技術棧](#1-測試技術棧)
2. [測試目錄結構](#2-測試目錄結構)
3. [測試分層策略](#3-測試分層策略)
4. [Service 層測試](#4-service-層測試)
5. [Controller 層測試](#5-controller-層測試)
6. [測試命名慣例](#6-測試命名慣例)
7. [測試資料工廠模式](#7-測試資料工廠模式)
8. [常見測試場景範本](#8-常見測試場景範本)
9. [執行測試](#9-執行測試)
10. [各模組測試覆蓋一覽](#10-各模組測試覆蓋一覽)

---

## 1. 測試技術棧

| 工具 | 用途 |
|------|------|
| **JUnit 5** (`junit-jupiter`) | 測試框架，提供 `@Test`、`@BeforeEach` 等注解 |
| **Mockito** | 依賴 Mock / Stub，搭配 `@Mock`、`@InjectMocks` |
| **AssertJ** | 流式斷言 (`assertThat(...)`) |
| **Spring MockMvc** | Controller 層 HTTP 請求模擬（不啟動真實伺服器） |
| **Jackson ObjectMapper** | 序列化/反序列化請求 Body（含 `findAndRegisterModules()` 支援 `LocalDate`） |

---

## 2. 測試目錄結構

```
src/test/java/com/learning/api/
├── controller/
│   ├── AuthControllerTest.java
│   ├── BookingControllerTest.java
│   ├── ChatMessageControllerTest.java
│   ├── CheckoutControllerTest.java
│   ├── LessonFeedbackControllerTest.java
│   ├── OrderControllerTest.java
│   ├── ReviewControlTest.java
│   ├── TutorScheduleControllerTest.java
│   └── VideoRoomControllerTest.java
└── service/
    ├── AuthServiceTest.java
    ├── BookingServiceTest.java
    ├── ChatMessageServiceTest.java
    ├── CheckoutServiceTest.java
    ├── CourseServiceTest.java
    ├── MemberServiceTest.java
    ├── OrderServiceTest.java
    └── TutorScheduleServiceTest.java
```

---

## 3. 測試分層策略

本專案採用**兩層測試**，不啟動 Spring Context 或真實資料庫：

```
┌─────────────────────────────────────┐
│  Controller Test  (MockMvc)         │  ← 驗證 HTTP 路由、狀態碼、JSON 回應
├─────────────────────────────────────┤
│  Service Test     (Mockito only)    │  ← 驗證業務邏輯、異常處理、Repository 互動
└─────────────────────────────────────┘
         ↕  不啟動 Spring Context
```

**優點：** 執行快速、隔離性強、不需要資料庫環境。
**注意：** Controller Test 不走真實 `SecurityConfig`（JWT Filter 不啟用），安全規則需另行驗證。

---

## 4. Service 層測試

### 4.1 基本骨架

```java
@ExtendWith(MockitoExtension.class)
class XxxServiceTest {

    @Mock private XxxRepository xxxRepo;   // 所有依賴都 Mock
    @Mock private OtherRepository otherRepo;

    @InjectMocks
    private XxxService xxxService;         // 被測目標，由 Mockito 注入 Mock

    // ── 測試案例 ──────────────────────────────────────────────────────────────

    @Test
    void methodName_whenCondition_thenExpectedBehavior() {
        // Arrange
        when(xxxRepo.findById(1L)).thenReturn(Optional.of(someEntity));

        // Act
        SomeResult result = xxxService.doSomething(input);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(xxxRepo).save(any(XxxEntity.class));
    }
}
```

### 4.2 常用 Mockito 語法

| 場景 | 語法 |
|------|------|
| 回傳值 | `when(repo.findById(1L)).thenReturn(Optional.of(entity))` |
| 拋出例外 | `when(repo.findById(99L)).thenThrow(new NoSuchElementException())` |
| void 方法正常執行 | `doNothing().when(repo).delete(any())` |
| void 方法拋例外 | `doThrow(new RuntimeException()).when(repo).delete(any())` |
| 驗證方法被呼叫 | `verify(repo).save(any(Entity.class))` |
| 驗證方法未被呼叫 | `verifyNoInteractions(repo)` |
| 寬鬆型別匹配 | `any(ClassName.class)` |

### 4.3 異常場景測試

```java
@Test
void method_whenInvalidInput_throwsIllegalArgument() {
    when(userRepo.findByEmail("x@test.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.loginReq(req))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("帳號或密碼錯誤"); // 可選：驗證訊息內容
}
```

---

## 5. Controller 層測試

### 5.1 基本骨架

```java
@ExtendWith(MockitoExtension.class)
class XxxControllerTest {

    @Mock private XxxService xxxService;

    @InjectMocks
    private XxxController xxxController;

    private MockMvc mockMvc;
    // 加上 findAndRegisterModules() 以支援 LocalDate 序列化
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(xxxController)
                .setControllerAdvice(new GlobalExceptionHandler()) // 掛載全域例外處理
                .build();
    }

    @Test
    void endpoint_validRequest_returns200() throws Exception {
        when(xxxService.doSomething(any())).thenReturn(someResp);

        mockMvc.perform(post("/api/xxx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.field").value("expectedValue"));
    }
}
```

### 5.2 HTTP 方法對應

| 操作 | MockMvc 方法 |
|------|-------------|
| 新增 | `post("/api/xxx")` |
| 查詢全部 | `get("/api/xxx")` |
| 查詢單筆 | `get("/api/xxx/{id}", 1L)` |
| 更新 | `put("/api/xxx/{id}", 1L)` |
| 刪除 | `delete("/api/xxx/{id}", 1L)` |

### 5.3 狀態碼驗證

```java
.andExpect(status().isOk())          // 200
.andExpect(status().isCreated())     // 201
.andExpect(status().isBadRequest())  // 400
.andExpect(status().isUnauthorized()) // 401
.andExpect(status().isNotFound())    // 404
```

### 5.4 JSON 回應斷言

```java
// 驗證單一欄位
.andExpect(jsonPath("$.token").value("jwt-token-abc"))

// 驗證陣列長度
.andExpect(jsonPath("$.data", hasSize(3)))

// 驗證字串包含
.andExpect(jsonPath("$.msg", containsString("email")))

// 驗證欄位存在
.andExpect(jsonPath("$.id").exists())
```

### 5.5 GlobalExceptionHandler 整合

> **重要：** `standaloneSetup` 預設不載入 `@ControllerAdvice`。
> 需在 `setUp()` 加上 `.setControllerAdvice(new GlobalExceptionHandler())`，
> 否則例外測試（如 `IllegalArgumentException → 400`）會失敗。

---

## 6. 測試命名慣例

採用 **`methodName_whenCondition_thenExpectedBehavior`** 格式：

```
sendBooking_validRequest_returnsTrue
sendBooking_nullRequest_returnsFalse
loginReq_whenEmailNotFound_throwsIllegalArgument
login_wrongPassword_returns400WithMsg
checkout_insufficientBalance_throwsException
```

| 部分 | 說明 |
|------|------|
| `methodName` | 被測的 Service 方法名或端點行為 |
| `whenCondition` | 輸入條件或前置狀態 |
| `thenExpectedBehavior` | 預期結果（回傳值、狀態碼、例外） |

---

## 7. 測試資料工廠模式

使用私有工廠方法建立測試物件，避免重複程式碼：

```java
// Service Test 範例
private User makeStudent(Long id, long wallet) {
    User user = new User();
    user.setId(id);
    user.setName("Student");
    user.setEmail("student@test.com");
    user.setPassword("hashedpw");
    user.setWallet(wallet);
    return user;
}

private Course makeCourse(Long id, Long tutorId, int price) {
    Course course = new Course();
    course.setId(id);
    course.setTutorId(tutorId);
    course.setName("Test Course");
    course.setSubject(21);
    course.setDescription("Desc");
    course.setPrice(price);
    course.setActive(true);
    return course;
}
```

**原則：**
- 工廠方法回傳最小可用物件（只設必填欄位）
- 參數只帶「測試間有差異」的欄位（如 `id`、`wallet`）
- 固定值直接寫死在方法內（如 `"Test Course"`）

---

## 8. 常見測試場景範本

### 8.1 正常流程（Happy Path）

```java
@Test
void sendBooking_validRequest_returnsTrue() {
    Course course = makeCourse(1L, 2L);
    when(userRepository.existsById(3L)).thenReturn(true);
    when(courseRepo.findById(1L)).thenReturn(Optional.of(course));

    boolean result = bookingService.sendBooking(makeReq(3L, 1L, 5));

    assertThat(result).isTrue();
    verify(bookingRepo).save(any(Bookings.class));
}
```

### 8.2 邊界條件

```java
@Test
void sendBooking_lessonCountZero_returnsFalse() {
    // 0 堂課 → 直接返回 false，不呼叫 Repository
    boolean result = bookingService.sendBooking(makeReq(1L, 1L, 0));

    assertThat(result).isFalse();
    verifyNoInteractions(bookingRepo);
}
```

### 8.3 null 輸入防禦

```java
@Test
void sendBooking_nullRequest_returnsFalse() {
    assertThat(bookingService.sendBooking(null)).isFalse();
    verifyNoInteractions(bookingRepo);
}
```

### 8.4 Controller 例外轉 HTTP 400

```java
@Test
void register_duplicateEmail_returns400WithMsg() throws Exception {
    doThrow(new IllegalArgumentException("此 email 已被註冊"))
            .when(memberService).register(any(RegisterReq.class));

    mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg", containsString("email")));
}
```

### 8.5 驗證 Service 方法被正確呼叫

```java
@Test
void login_returnsTokenFromAuthService() throws Exception {
    when(authService.loginReq(any(LoginReq.class))).thenReturn(new LoginResp("token"));

    mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginReq)))
            .andExpect(status().isOk());

    // 驗證 Controller 確實呼叫了 Service
    verify(authService).loginReq(any(LoginReq.class));
}
```

---

## 9. 執行測試

```bash
# 執行全部測試
mvn test

# 執行指定測試類別
mvn test -Dtest=AuthServiceTest

# 執行指定測試方法
mvn test -Dtest=AuthServiceTest#loginReq_whenValidCredentials_returnsTokenOnly

# 執行所有 Service 層測試
mvn test -Dtest="*ServiceTest"

# 執行所有 Controller 層測試
mvn test -Dtest="*ControllerTest"

# 產生測試報告（結果在 target/surefire-reports/）
mvn surefire-report:report
```

> **提示：** 本專案測試使用 `spring.profiles.active=test` profile，
> 此 profile 下 `JwtFilter` 不會啟動，Controller 測試無需攜帶 JWT token。

---

## 10. 各模組測試覆蓋一覽

| 模組 | Service Test | Controller Test | 主要測試場景 |
|------|:---:|:---:|------|
| **Auth（認證）** | ✅ | ✅ | 登入成功/失敗、JWT 產生、email 不存在、密碼錯誤 |
| **Member（會員）** | ✅ | ✅（AuthController） | 註冊成功、email 重複、密碼加密 |
| **Booking（預約）** | ✅ | ✅ | 建立預約、null/零堂課防禦、課程不存在 |
| **Checkout（結帳）** | ✅ | ✅ | 正常結帳、餘額不足、課程不存在、時段衝突 |
| **Order（訂單）** | ✅ | ✅ | 查詢訂單列表、訂單狀態更新 |
| **Course（課程）** | ✅ | — | 課程 CRUD、分頁搜尋、N+1 防護 |
| **TutorSchedule（課表）** | ✅ | ✅ | 新增/查詢可用時段、衝突檢查 |
| **ChatMessage（聊天）** | ✅ | ✅ | 發送訊息、查詢歷史、檔案上傳 |
| **LessonFeedback（回饋）** | — | ✅ | 提交回饋、重複提交防禦 |
| **Review（評價）** | — | ✅ | 新增評價、查詢課程評價 |
| **VideoRoom（視訊）** | — | ✅ | 建立房間、加入房間、房間不存在 |

---

## 附錄：新增測試的快速清單

為新功能補充測試時，確認以下項目：

- [ ] **Service Test：** 正常流程（Happy Path）
- [ ] **Service Test：** 輸入為 null 或無效值（邊界條件）
- [ ] **Service Test：** Repository 回傳空（`Optional.empty()`）
- [ ] **Service Test：** 驗證 Repository `save()` 被呼叫（或不被呼叫）
- [ ] **Controller Test：** HTTP 狀態碼正確（200 / 400 / 404）
- [ ] **Controller Test：** JSON 回應欄位正確
- [ ] **Controller Test：** 例外轉 400 + 訊息正確（需掛 `GlobalExceptionHandler`）
- [ ] **命名：** 遵循 `method_whenCondition_thenResult` 格式
- [ ] **工廠方法：** 重複使用的測試物件提取為私有工廠方法
