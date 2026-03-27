# Dev Plan

## Tech Stack

- **Backend**: Spring Boot 3.x + Spring Security + MyBatis-Plus
- **Frontend**: Vue 3 + Vite + Pinia + Vue Router + Tailwind CSS v3 + vue-i18n
- **i18n**: 英文（en）、簡中（zh-CN）、繁中（zh-TW），前端 vue-i18n，後端 message source 分離
- **Mobile（未來）**: Flutter — 因此前端架構需保持 thin client，所有業務邏輯放在後端 API
- **DB**: PostgreSQL
- **Storage**: Local filesystem（`./data/uploads/` 目錄，Spring 靜態資源伺服）
- **Auth**: 純 JWT（access token only），MVP 不用 Redis、不做 refresh token
  - Token 存 HttpOnly Cookie（防 XSS），Spring Security filter chain 驗證
  - Access token 有效期 7 天，過期需重新登入
- **CORS**: 白名單制，開發環境允許 `localhost:5173`（Vite dev server）

---

## Architecture Principle（Flutter 準備）

後端採 **API-first** 設計，確保 Flutter app 未來可以直接共用同一組 API：

1. **所有功能走 REST API**，不在 Vue 裡寫業務邏輯
2. **API 回傳純 JSON**，不渲染 HTML 頁面（public CV 頁面除外，見下方說明）
3. **API versioning**: `/api/v1/...`，未來 Flutter 可直接呼叫
4. **Public CV**: 短期用 Vue CSR 渲染 `/u/{username}`，未來 Flutter 用 WebView 或原生渲染

---

## Database Schema

### 共用欄位

多數業務 table 繼承 `BaseEntity`：`id`（BIGINT, auto increment）、`created_at`、`updated_at`、`is_deleted`（logical delete, MyBatis-Plus `@TableLogic`）。

例外：
- `credit_transactions` 為 append-only ledger，只保留 `created_at`，不做 `updated_at` / logical delete
- `templates` 為後台可控字典表，MVP 用 `is_active` 控制啟用狀態，不做 logical delete

### `users`

| 欄位 | 型態 | 約束 | 說明 |
|---|---|---|---|
| id | BIGINT | PK, auto | |
| email | VARCHAR(255) | UNIQUE, NOT NULL | 登入帳號 |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt |
| username | VARCHAR(50) | UNIQUE, NULL | 公開個人頁網址用，onboarding step1 完成後必填 |
| display_name | VARCHAR(100) | NOT NULL | 顯示名稱 |
| avatar_path | VARCHAR(500) | NULL | 相對路徑，如 `/uploads/1/avatar.jpg` |
| onboarding_draft | JSONB | NULL | onboarding step2 暫存資料，step3 建立第一份 CV 後清空 |
| onboarding_status | VARCHAR(20) | NOT NULL, DEFAULT 'STEP_1' | 表示「下一個必須完成的步驟」 |
| locale | VARCHAR(10) | NOT NULL, DEFAULT 'zh-TW' | 語系偏好：en / zh-CN / zh-TW |
| credit_balance | INT | NOT NULL, DEFAULT 0 | 剩餘 credit 數量 |
| created_at | TIMESTAMP | | |
| updated_at | TIMESTAMP | | |
| is_deleted | BOOLEAN | DEFAULT false | |

Index: `users_email_idx`（unique）、`users_username_idx`（unique；PostgreSQL 允許多個 NULL）

`onboarding_draft` 結構（存在 `users.onboarding_draft`，step2 暫存，step3 完成後清空）：
```json
{
  "summary": { "text": "擁有 5 年經驗的全端工程師..." },
  "experience": {
    "items": [
      {
        "company": "ABC Corp",
        "role": "Senior Engineer",
        "startDate": "2020-01",
        "endDate": "2023-06",
        "current": false,
        "description": "負責..."
      }
    ]
  },
  "education": {
    "items": [
      {
        "school": "NTU",
        "degree": "Bachelor",
        "major": "Computer Science",
        "startDate": "2016-09",
        "endDate": "2020-06"
      }
    ]
  },
  "skills": {
    "items": ["Java", "Spring Boot", "Vue.js", "PostgreSQL"]
  }
}
```

### `cvs`

| 欄位 | 型態 | 約束 | 說明 |
|---|---|---|---|
| id | BIGINT | PK, auto | |
| user_id | BIGINT | FK → users.id, NOT NULL | |
| title | VARCHAR(200) | NOT NULL | 如「軟體工程師履歷」 |
| template_key | VARCHAR(50) | NOT NULL | 對應前端 template component |
| is_public | BOOLEAN | DEFAULT false | 是否公開 |
| slug | VARCHAR(100) | NULL | 公開網址用，NULL 表示不公開 |
| created_at | TIMESTAMP | | |
| updated_at | TIMESTAMP | | |
| is_deleted | BOOLEAN | DEFAULT false | |

Index: `cvs_user_id_idx`、`cvs_slug_idx`（unique, where slug IS NOT NULL）

### `cv_sections`

| 欄位 | 型態 | 約束 | 說明 |
|---|---|---|---|
| id | BIGINT | PK, auto | |
| cv_id | BIGINT | FK → cvs.id, NOT NULL | |
| section_type | VARCHAR(30) | NOT NULL | 見下方枚舉 |
| sort_order | INT | NOT NULL | 排序用，從 0 開始 |
| title | VARCHAR(200) | NULL | 自訂標題，如「工作經歷」 |
| content | JSONB | NOT NULL | 結構化內容，結構依 section_type 不同 |
| created_at | TIMESTAMP | | |
| updated_at | TIMESTAMP | | |
| is_deleted | BOOLEAN | DEFAULT false | |

Index: `cv_sections_cv_id_sort_idx`（cv_id + sort_order）

### Section Types & Content 結構

MVP 支援 4 種：

**`summary`** — 個人簡介
```json
{
  "text": "擁有 5 年經驗的全端工程師..."
}
```

**`experience`** — 工作經歷（陣列）
```json
{
  "items": [
    {
      "company": "ABC Corp",
      "role": "Senior Engineer",
      "startDate": "2020-01",
      "endDate": "2023-06",
      "current": false,
      "description": "負責..."
    }
  ]
}
```

**`education`** — 學歷（陣列）
```json
{
  "items": [
    {
      "school": "NTU",
      "degree": "Bachelor",
      "major": "Computer Science",
      "startDate": "2016-09",
      "endDate": "2020-06"
    }
  ]
}
```

**`skills`** — 技能
```json
{
  "items": ["Java", "Spring Boot", "Vue.js", "PostgreSQL"]
}
```

### `credit_transactions`

| 欄位 | 型態 | 約束 | 說明 |
|---|---|---|---|
| id | BIGINT | PK, auto | |
| user_id | BIGINT | FK → users.id, NOT NULL | |
| amount | INT | NOT NULL | 正數=收入，負數=支出 |
| balance_after | INT | NOT NULL | 交易後餘額（方便查詢，避免計算） |
| type | VARCHAR(30) | NOT NULL | 交易類型枚舉（見下方） |
| reference_id | BIGINT | NULL | 關聯資源 ID（如 cv_id） |
| description | VARCHAR(200) | NULL | 描述，如「建立履歷：軟體工程師」 |
| created_at | TIMESTAMP | | |

Index: `credit_transactions_user_id_idx`

**交易類型枚舉（type）**:

| type | amount | 說明 |
|---|---|---|
| `SIGN_UP_BONUS` | +50 | 註冊贈送 |
| `DAILY_LOGIN` | +5 | 每日登入獎勵（未來） |
| `CREATE_CV` | -10 | 建立新 CV（onboarding 第 1 份免費） |
| `SWITCH_TEMPLATE` | `-template.credit_cost` | 切換 template，實際扣款以所選 template 的 `credit_cost` 為準 |
| `PDF_EXPORT` | -15 | PDF 匯出（未來） |
| `LINKEDIN_IMPORT` | -10 | LinkedIn 匯入（未來） |
| `ADMIN_GRANT` | +N | 管理員手動加值 |

### `templates`

| 欄位 | 型態 | 約束 | 說明 |
|---|---|---|---|
| id | BIGINT | PK, auto | |
| component_key | VARCHAR(50) | UNIQUE, NOT NULL | 對應前端 Vue component 名稱 |
| display_name_i18n | JSONB | NOT NULL | 多語系名稱，見下方結構 |
| preview_image | VARCHAR(500) | NULL | Template 縮圖路徑 |
| credit_cost | INT | NOT NULL, DEFAULT 0 | 使用此 template 的 credit 成本，0=免費；為 template 計費唯一來源 |
| is_active | BOOLEAN | DEFAULT true | 是否啟用 |
| sort_order | INT | NOT NULL, DEFAULT 0 | 排序 |
| created_at | TIMESTAMP | | |
| updated_at | TIMESTAMP | | |

`display_name_i18n` 結構：
```json
{ "en": "Minimal", "zh-CN": "简约", "zh-TW": "簡約" }
```

初始 seed data:

| component_key | display_name_i18n | credit_cost |
|---|---|---|
| minimal | `{"en":"Minimal","zh-CN":"简约","zh-TW":"簡約"}` | 0 |
| sidebar | `{"en":"Sidebar","zh-CN":"侧边栏","zh-TW":"側邊欄"}` | 0 |
| modern | `{"en":"Modern","zh-CN":"现代","zh-TW":"現代"}` | 5 |

---

## API Specification

### Response Format

- 全部 API 使用統一包裝：`R<T> = { code, message, data }`
- 下方 Response 欄位描述的是 `data` 內容
- JWT 只寫入 HttpOnly Cookie，不會在 JSON body 回傳 `token`

### Auth（公開）

| Endpoint | Method | Request Body | Response | 說明 |
|---|---|---|---|---|
| `/api/v1/auth/register` | POST | `{ email, password, displayName }` | `{ user }` | 註冊成功後寫入 Cookie，並建立 `SIGN_UP_BONUS` 交易 |
| `/api/v1/auth/login` | POST | `{ email, password }` | `{ user }` | 登入，設定 HttpOnly Cookie |
| `/api/v1/auth/logout` | POST | — | — | 清除 Cookie |
| `/api/v1/auth/me` | GET | — | `{ user }` | 取得當前用戶資料（含 creditBalance, locale） |
| `/api/v1/auth/change-locale` | PUT | `{ locale }` | `{ user }` | 切換語系 |

### Onboarding（需登入）

| Endpoint | Method | Request Body | Response | 說明 |
|---|---|---|---|---|
| `/api/v1/onboarding/status` | GET | — | `{ status, nextStep }` | 取得目前進度；`status` 與 `nextStep` 等值，皆表示下一個必須完成的步驟 |
| `/api/v1/onboarding/step1` | PUT | `{ displayName, username, avatarPath? }` | `{ user }` | 基本資料，必填；`avatarPath` 來自 upload API |
| `/api/v1/onboarding/step2` | PUT | `{ summary?, experience?[], education?[], skills?[] }` | `{ user, draft }` | 專業資訊，可 skip；內容寫入 `users.onboarding_draft` |
| `/api/v1/onboarding/step3` | PUT | `{ templateKey }` | `{ cv, sections[], user }` | 選 template，使用 `onboarding_draft` 建立第一份 CV，完成後清空 draft |
| `/api/v1/onboarding/skip-step2` | POST | — | `{ user }` | 跳過 step2，直接到 step3 |

### CV 管理（需登入）

| Endpoint | Method | Request Body | Response | 說明 |
|---|---|---|---|---|
| `/api/v1/me/cvs` | GET | — | `{ cvs[] }` | 列出我的 CV |
| `/api/v1/me/cvs` | POST | `{ title, templateKey }` | `{ cv }` | 新建 CV（扣 `CREATE_CV` credit；onboarding 第 1 份免費不走這支） |
| `/api/v1/me/cvs/{id}` | GET | — | `{ cv, sections[] }` | 取得 CV 完整內容 |
| `/api/v1/me/cvs/{id}` | PUT | `{ title?, templateKey?, isPublic?, slug? }` | `{ cv }` | 更新 CV metadata（換 template 時可能扣 credit） |
| `/api/v1/me/cvs/{id}` | DELETE | — | — | 刪除 CV（logical delete） |
| `/api/v1/me/cvs/{id}/sections` | PUT | `{ sections[{ sectionType, sortOrder, title, content }] }` | `{ sections[] }` | 批次更新 sections（整份取代） |

### Template（公開）

| Endpoint | Method | Request Body | Response | 說明 |
|---|---|---|---|---|
| `/api/v1/templates` | GET | — | `{ templates[] }` | 列出可用 template（含 creditCost, displayName 根據 locale 回傳） |

### Credit（需登入）

| Endpoint | Method | Request Body | Response | 說明 |
|---|---|---|---|---|
| `/api/v1/me/credits/balance` | GET | — | `{ balance }` | 查詢剩餘 credit |
| `/api/v1/me/credits/transactions` | GET | `?page=1&size=20` | `{ transactions[], page, size, total }` | 查詢交易紀錄（分頁） |

### Upload（需登入）

| Endpoint | Method | Request Body | Response | 說明 |
|---|---|---|---|---|
| `/api/v1/upload` | POST | multipart/form-data | `{ path }` | 上傳檔案，回傳相對路徑 |

### User / Public Utility（公開）

| Endpoint | Method | Request Body | Response | 說明 |
|---|---|---|---|---|
| `/api/v1/users/check-username` | GET | `?username=foo` | `{ available }` | 檢查 username 是否可用，供 onboarding step1 即時驗證 |

### Public CV（公開）

| Endpoint | Method | Request Body | Response | 說明 |
|---|---|---|---|---|
| `/api/v1/public/{username}` | GET | — | `{ user, cvs[] }` | 取得公開用戶資料與公開 CV 清單，供 `/u/{username}` 使用 |
| `/api/v1/public/{username}/{slug}` | GET | — | `{ cv, sections[], templateKey }` | 取得單份公開 CV |

---

## Onboarding 流程

新用戶註冊後必須完成的引導流程，完成前無法進入 dashboard。由 `onboarding_status` 欄位控制，前端 router guard 攔截。

### Step 1：基本資料（必填）
- 輸入 display name（預設為註冊時的 displayName，可改）
- 設定 username（公開頁網址用，需即時檢查唯一性）
- 上傳 avatar（可選；先呼叫 `/api/v1/upload`，再把 `avatarPath` 送到 step1）

### Step 2：專業資訊（可 skip）
- 手動填寫 summary、experience、education、skills
- 後端先暫存在 `users.onboarding_draft`（key 統一用單數：summary, experience, education, skills）
- 可跳過，之後在 CV editor 裡補
- 未來：LinkedIn 匯入功能放在這步（見 TODO）

### Step 3：選擇 Template（必填）
- 顯示 template 卡片列表（從 `/api/v1/templates` 取得，含 credit 成本標籤）
- 免費 template 顯示「免費」，付費 template 顯示其 `credit_cost`
- 選一個 template → 後端用 `onboarding_draft` 建立第一份 CV（免費）→ 清空 draft → onboarding 完成

### 狀態枚舉

`STEP_1` → `STEP_2` → `STEP_3` → `DONE`

`onboarding_status` 的語意是「下一個必須完成的步驟」：
- 註冊完成後預設 `STEP_1`
- 完成 step1 後更新為 `STEP_2`
- 完成或跳過 step2 後更新為 `STEP_3`
- 完成 step3 後更新為 `DONE`

---

## Credit 系統

### 核心邏輯

- 新用戶註冊時建立一筆 **`SIGN_UP_BONUS` +50** 交易，並將 `users.credit_balance` 從 0 更新為 50
- Onboarding 建立第一份 CV **免費**，之後每份 **10 credits**
- Template 切換是否扣款由 `templates.credit_cost` 決定；`credit_cost = 0` 不扣，`> 0` 扣對應數值
- 扣 credit 前先檢查餘額，不足回傳 `402 Payment Required` + 錯誤訊息
- 所有交易記錄寫入 `credit_transactions`，確保可追溯

### CreditService 核心方法

```java
@Service
public class CreditService {
    // 檢查餘額是否足夠
    public boolean hasEnoughCredits(Long userId, int amount);

    // 扣 credit（事務性，扣餘額 + 寫交易紀錄）
    @Transactional
    public void deduct(Long userId, int amount, String type, Long referenceId, String description);

    // 加 credit（贈送、管理員加值等）
    @Transactional
    public void credit(Long userId, int amount, String type, String description);

    // 查詢餘額
    public int getBalance(Long userId);

    // 查詢交易紀錄（分頁）
    public IPage<CreditTransaction> getTransactions(Long userId, int page, int size);
}
```

### 前端整合

- Dashboard 右上角顯示 credit 餘額（從 `/api/v1/auth/me` 取得 `creditBalance`）
- 建立新 CV 時，如 credit 不足，跳出提示「Credit 不足，需要 10 credits」
- Template 選擇頁顯示每個 template 的 credit 成本標籤（免費 / `{creditCost} credits`）
- 切換 `credit_cost > 0` 的 template 時跳出確認對話框

---

## i18n 多語系設計

### 支援語系

| Code | 語言 |
|---|---|
| `en` | English |
| `zh-CN` | 简体中文 |
| `zh-TW` | 繁體中文 |

### 前端（vue-i18n）

```js
// src/i18n/index.js
import { createI18n } from 'vue-i18n'
import en from './locales/en.json'
import zhCN from './locales/zh-CN.json'
import zhTW from './locales/zh-TW.json'

const i18n = createI18n({
  legacy: false,           // 使用 Composition API 模式
  locale: localStorage.getItem('locale') || 'zh-TW',
  fallbackLocale: 'en',
  messages: { en, 'zh-CN': zhCN, 'zh-TW': zhTW }
})

export default i18n
```

Locale files 結構（以 `en.json` 為例）：
```json
{
  "common": {
    "save": "Save",
    "cancel": "Cancel",
    "delete": "Delete",
    "credit": "Credits",
    "creditBalance": "{count} Credits"
  },
  "auth": {
    "login": "Log In",
    "register": "Sign Up",
    "email": "Email",
    "password": "Password"
  },
  "onboarding": {
    "step1Title": "Basic Info",
    "step2Title": "Professional Info",
    "step3Title": "Choose Template"
  },
  "dashboard": {
    "myCvs": "My Resumes",
    "createNew": "Create New",
    "creditCost": "Costs {cost} credits"
  },
  "editor": {
    "addSection": "Add Section",
    "summary": "Summary",
    "experience": "Experience",
    "education": "Education",
    "skills": "Skills"
  },
  "credit": {
    "insufficient": "Insufficient credits. This action requires {cost} credits, you have {balance}.",
    "createCv": "Create Resume",
    "switchTemplate": "Switch Template"
  }
}
```

### 後端 i18n

後端不負責 UI 文字翻譯，但**錯誤訊息和 API 回傳的固定文字**需要 i18n：

- 用 `Accept-Language` header 判斷語系（前端每次 API 請求帶上當前 locale）
- Spring `MessageSource` 管理後端錯誤訊息：

```
src/main/resources/
├── messages.properties              # 預設（en）
├── messages_zh_CN.properties        # 簡中
└── messages_zh_TW.properties        # 繁中
```

```properties
# messages_zh_TW.properties
error.credit.insufficient=Credit 不足，需要 {0} credits，目前剩餘 {1}
error.auth.invalid_credentials=帳號或密碼錯誤
error.user.username_taken=此使用者名稱已被使用
```

### 語系切換流程

1. 前端 localStorage 存 `locale`，vue-i18n 即時切換 UI 語系
2. 呼叫 `PUT /api/v1/auth/change-locale` 將偏好存到 DB（`users.locale`）
3. 之後登入時自動從 user profile 讀取 locale 設定
4. API 請求統一帶 `Accept-Language: zh-TW` header（axios interceptor 處理）

---

## 專案結構

### Backend（Spring Boot + MyBatis-Plus）

```
src/main/java/me/hker/
├── config/
│   ├── SecurityConfig.java           # JWT filter chain, CORS whitelist
│   ├── MyBatisPlusConfig.java        # pagination plugin, logic delete
│   └── StaticResourceConfig.java     # /uploads/** 本地靜態資源
├── common/
│   ├── BaseEntity.java               # id, created_at, updated_at, is_deleted
│   ├── R.java                        # 統一回傳格式 { code, message, data }
│   ├── GlobalExceptionHandler.java
│   └── I18nMessageHelper.java        # 封裝 MessageSource，根據 Accept-Language 取訊息
├── module/
│   ├── auth/
│   │   ├── AuthController.java       # /api/v1/auth/**
│   │   ├── AuthService.java
│   │   ├── JwtUtil.java              # generate / parse / validate token
│   │   └── JwtAuthFilter.java        # OncePerRequestFilter, Cookie 讀 token
│   ├── user/
│   │   ├── UserController.java       # /api/v1/users/check-username
│   │   ├── UserService.java
│   │   ├── UserMapper.java           # BaseMapper<User>
│   │   └── User.java
│   ├── onboarding/
│   │   ├── OnboardingController.java # /api/v1/onboarding/**
│   │   └── OnboardingService.java
│   ├── cv/
│   │   ├── CvController.java         # /api/v1/me/cvs/**
│   │   ├── PublicCvController.java   # /api/v1/public/**
│   │   ├── CvService.java
│   │   ├── CvMapper.java
│   │   ├── CvSectionMapper.java
│   │   ├── Cv.java
│   │   └── CvSection.java
│   ├── template/
│   │   ├── TemplateController.java   # /api/v1/templates
│   │   ├── TemplateService.java
│   │   ├── TemplateMapper.java
│   │   └── Template.java
│   ├── credit/
│   │   ├── CreditController.java     # /api/v1/me/credits/**
│   │   ├── CreditService.java        # 扣款、加值、查詢
│   │   ├── CreditTransactionMapper.java
│   │   └── CreditTransaction.java
│   └── upload/
│       ├── UploadController.java     # /api/v1/upload
│       └── LocalStorageService.java  # saves to ./data/uploads/{userId}/
└── resources/
    ├── mapper/                       # MyBatis XML mapper files（複雜查詢用）
    ├── messages.properties           # i18n 預設（en）
    ├── messages_zh_CN.properties     # i18n 簡中
    ├── messages_zh_TW.properties     # i18n 繁中
    └── db/migration/                 # Flyway SQL scripts
        ├── V1__create_users.sql
        ├── V2__create_cvs.sql
        ├── V3__create_cv_sections.sql
        ├── V4__create_templates.sql
        ├── V5__create_credit_transactions.sql
        └── V6__seed_templates.sql
```

### Frontend（Vue 3 + Tailwind）

```
src/
├── main.js
├── App.vue
├── i18n/
│   ├── index.js                     # createI18n 設定
│   └── locales/
│       ├── en.json                  # 英文
│       ├── zh-CN.json               # 簡中
│       └── zh-TW.json               # 繁中
├── api/                            # API 層（方便未來 Flutter 共用相同 API contract）
│   ├── auth.js                     # login, register, logout, me, changeLocale
│   ├── onboarding.js               # step1, step2, step3, skip, status
│   ├── cv.js                       # CRUD + sections
│   ├── credit.js                   # balance, transactions
│   ├── template.js                 # list templates
│   ├── user.js                     # checkUsername
│   ├── upload.js                   # file upload
│   └── client.js                   # axios instance + Accept-Language interceptor
├── router/
│   └── index.js                    # beforeEach guard: check onboarding_status
├── stores/
│   ├── auth.js                     # Pinia: user, isLoggedIn, creditBalance, locale
│   └── cv.js                       # Pinia: currentCv, sections
├── views/
│   ├── auth/
│   │   ├── LoginView.vue
│   │   └── RegisterView.vue
│   ├── onboarding/
│   │   ├── OnboardingLayout.vue    # step progress bar (Tailwind)
│   │   ├── Step1BasicInfo.vue      # displayName, username, avatar
│   │   ├── Step2ProfessionInfo.vue  # summary, experience, education, skills（可 skip）
│   │   └── Step3Template.vue       # template card grid, pick one
│   ├── dashboard/
│   │   └── DashboardView.vue       # list of CVs, create new
│   ├── editor/
│   │   └── CvEditorView.vue        # section editor + live preview
│   └── public/
│       ├── PublicProfileView.vue   # /u/{username} — 公開履歷清單
│       └── PublicCvView.vue        # /u/{username}/{slug} — dynamic template render
├── components/
│   ├── cv-templates/
│   │   ├── templateRegistry.js     # auto-import all templates
│   │   ├── MinimalTemplate.vue
│   │   ├── SidebarTemplate.vue
│   │   └── ModernTemplate.vue      # 新增 template 只需加 Vue component + DB seed
│   ├── sections/                   # 每種 section 的 edit + display 組件
│   │   ├── SummarySection.vue
│   │   ├── ExperienceSection.vue
│   │   ├── EducationSection.vue
│   │   └── SkillsSection.vue
│   └── ui/                         # Button, Input, Card, Modal (Tailwind-based)
└── assets/
```

---

## JWT Auth 設計

### Token 流程

1. **登入/註冊**: 後端驗證成功 → 產生 JWT → 寫入 `HttpOnly` Cookie → 回傳 user JSON（不回傳 token）
2. **每次請求**: `JwtAuthFilter` 從 Cookie 讀取 token → 驗證 → 設定 `SecurityContext`
3. **前端啟動**: app 初始化時先呼叫 `/api/v1/auth/me` bootstrap auth store，避免 refresh 後誤判未登入
4. **登出**: 清除 Cookie

### Cookie 設定

```
Name: token
HttpOnly: true
Secure: true（production）/ false（development）
SameSite: Lax
Path: /api
Max-Age: 604800（7 天）
```

### JwtUtil 核心方法

```java
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    private static final long EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000; // 7 天

    public String generateToken(Long userId, String email);
    public Long getUserId(String token);
    public boolean validateToken(String token);
}
```

---

## CV Editor 資料流

### 編輯流程

1. 進入 editor → `GET /api/v1/me/cvs/{id}` 取得 CV + sections
2. 用戶編輯 section → 前端即時更新 local state（Pinia）
3. Live preview 用 `<component :is="templateComponent">` 渲染，資料從 Pinia 讀取
4. 用戶點「儲存」→ `PUT /api/v1/me/cvs/{id}/sections` 一次送出所有 sections（整份取代）
5. Section 的排序用 drag & drop 修改 `sort_order`

### Auto-save（可選）

用 `watch` + debounce（1.5 秒無操作後自動存），MVP 先做手動儲存，auto-save 列為後續優化。

---

## Template 機制

### 新增 Template 流程

1. 在 `src/components/cv-templates/` 新增 Vue component，props 接收 `{ cv, sections }`
2. 在 `templateRegistry.js` 註冊（`defineAsyncComponent`）
3. 在 DB `templates` table 新增一筆 seed（`component_key` = component 檔名）
4. 前端 template 列表自動從 API 取得，無需改前端邏輯

### Public CV 渲染

- `/u/{username}` 對應 `PublicProfileView.vue`，顯示使用者公開資訊與公開 CV 清單
- `/u/{username}/{slug}` 對應 `PublicCvView.vue`，根據 API 回傳的 `templateKey` 動態載入對應 component，props 傳入 CV 資料
- 短期純 CSR，未來如需 SEO 可改用 Nuxt 或 SSR 方案

---

## File Upload 設計

### 後端設定

```java
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    @Value("${app.upload.path:./data/uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
```

```yaml
# application.yml
app:
  upload:
    path: ./data/uploads
    max-size: 5MB
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 5MB
```

### 路徑規則

- Avatar: `/uploads/{userId}/avatar.{ext}`
- 其他資產: `/uploads/{userId}/{uuid}.{ext}`
- DB 只存相對路徑，前端用 `baseURL + path` 組合完整 URL
- 上傳同名檔案直接覆蓋
- onboarding step1 只接受 `avatarPath`，不直接收 multipart 檔案

---

## Onboarding Router Guard（Vue）

```js
// router/index.js
router.beforeEach(async (to) => {
  const auth = useAuthStore()

  // 初次載入先用 /auth/me bootstrap 使用者狀態
  if (!auth.initialized) {
    await auth.bootstrap()
  }

  // 未登入
  if (!auth.isLoggedIn) {
    if (to.meta.requiresAuth) return '/login'
    return true
  }

  // 已登入但 onboarding 未完成 → 導向對應步驟
  if (auth.user.onboarding_status !== 'DONE') {
    if (!to.path.startsWith('/onboarding')) {
      // 根據狀態導向正確步驟
      const stepMap = {
        STEP_1: '/onboarding/step1',
        STEP_2: '/onboarding/step2',
        STEP_3: '/onboarding/step3',
      }
      return stepMap[auth.user.onboarding_status] || '/onboarding/step1'
    }
    return true
  }

  // 已完成 onboarding → 不能再進入 onboarding
  if (to.path.startsWith('/onboarding')) {
    return '/dashboard'
  }

  return true
})
```

---

## Tailwind 設定

```js
// tailwind.config.js
export default {
  content: ['./index.html', './src/**/*.{vue,js}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#EEF2FF',
          500: '#4F46E5',
          600: '#4338CA',
          700: '#3730A3',
        }
      }
    }
  }
}
```

CV template 組件使用 Tailwind 寫樣式，不同 template 只是不同的 Tailwind class 組合。Editor 的 live preview 用 `<component :is="...">` 切換，不會有 CSS scope 污染。

---

## TODO（未來規劃）

- [ ] **LinkedIn 匯入**: 用戶貼上 LinkedIn public profile URL → 後端解析（需評估可行方案：LinkedIn API / 匯出檔上傳 / scraping），預估扣 10 credits
- [ ] **Flutter App**: 共用後端 API，WebView 渲染 CV template 或 Flutter 原生 UI
- [ ] **Refresh Token + Redis**: 引入 Redis 做 token blacklist 和 refresh token 管理
- [ ] **CV Auto-save**: debounce 自動儲存
- [ ] **SEO**: Public CV 頁面改用 SSR（Nuxt 或 Spring Thymeleaf）
- [ ] **OAuth 登入**（Google、GitHub）
- [ ] **PDF 匯出**: 前端 html2canvas + jsPDF 或後端 Puppeteer，預估扣 15 credits
- [ ] **Credit 加值**: 付費購買 credit（串接第三方支付）
- [ ] **每日登入獎勵**: 每日首次登入贈送 5 credits
