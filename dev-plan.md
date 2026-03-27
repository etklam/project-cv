# Dev Plan

## Tech Stack

- **Backend**: Spring Boot 3.x + Kotlin + Spring Security + MyBatis-Plus
- **Frontend**: Vue 3 + Vite + Pinia + Vue Router + Tailwind CSS v3 + vue-i18n
- **PDF Renderer（Phase 3）**: Internal Node.js service + Puppeteer
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
2. **API 回傳純 JSON**，不渲染 HTML 頁面（public CV / print route 例外，見下方說明）
3. **API versioning**: `/api/v1/...`，未來 Flutter 可直接呼叫
4. **Public CV**: 短期用 Vue CSR 渲染 `/u/{username}`，未來 Flutter 用 WebView 或原生渲染
5. **PDF 匯出分層**: Spring 僅負責權限、扣款、審計與簽發短期 token；print route 負責畫面；renderer service 只做 PDF render
6. **Template 單一渲染入口**: editor preview、public page、print page 全部共用同一個 template registry 與 renderer wrapper

### Backend 實作規範

- 後端原始碼統一使用 Kotlin，路徑固定為 `src/main/kotlin`
- 所有 `*Service` 一律拆成 `interface` + `impl`；Controller 只依賴 service interface
- Service 命名規則：`FooService.kt` + `impl/FooServiceImpl.kt`
- 一律使用 constructor injection，不使用 field injection
- `Util`、`Config`、`Mapper`、`Controller` 可直接用 concrete class，不強制抽 interface

---

## Current Progress（2026-03-28）

此區塊是目前已驗證的實作快照；下方 Phase checklist 仍保留原始規劃拆解，尚未逐項回填。

### 已驗證落地

- 本地 Docker 開發環境可用：`db`、`backend`、`frontend`、`pdf-renderer` 皆為 healthy
- Reward / template MVP slice 已落地目前核心流程：template 列表、dashboard reward summary、promo / invite code redemption、credit balance / transactions 均已有實作
- Backend controller 回應契約已收斂為 typed DTO，auth / credit / template 不再用 ad-hoc `Map<String, Any?>`
- `CurrentUserResolver` 已集中目前開發期的 current-user fallback，移除多處分散的 `userId ?: 1L`
- Frontend reward / template 狀態已抽成 `useRewardCenter` 與 `useTemplateCatalog`，`DashboardView` 與 `Step3Template` 不再各自複製 fetch / filter 邏輯
- Template 新增路徑已比原先更可維護：後端 `templates` table / API 負責商業 metadata，前端 `templateRegistry` / template component 負責渲染；`Dashboard` 與 onboarding step3 共用同一套 template catalog 載入邏輯

### 驗證結果

- `backend`: `./gradlew test` 綠燈（2026-03-28）
- `frontend`: `npm test` 綠燈（2026-03-28）
- `docker compose ps` 顯示 `db`、`backend`、`frontend`、`pdf-renderer` 全部 healthy（2026-03-28）

### 已接受但延後處理的技術債

- Auth / security 仍是「目標架構」與「目前開發實作」並存：規劃上是 JWT + Cookie，但目前 controller / 測試仍保留 `X-User-Id` + `CurrentUserResolver` 的 dev fallback
- Flyway migration 歷史仍不健康：`V1` 到 `V9` 多為 placeholder，實際 reward / template / users bootstrap 主要落在 `V10__bootstrap_reward_and_template_schema.sql`
- `CreditService` 目前仍是 read-modify-write 更新 `users.credit_balance`，尚未加鎖或版本控制；未來 credit 流量增加時有 race condition 風險
- `RewardServiceImpl` 仍承擔 code resolve、redemption policy、ledger side effect、response shaping 等多重責任；新增 reward type / admin 流程前應先拆邊界
- Template 仍是跨 stack 雙來源：後端 seed / DB 管 metadata，前端 `templateRegistry` 管 renderer；在 template 數量變多前需要補 contract check，避免 registry key 與 DB `component_key` 漂移
- 測試仍偏 mock-heavy，且 test profile 未打開真實 Flyway / PostgreSQL integration；migration 與 SQL runtime 問題仍可能在 Docker 階段才暴露

---

## Database Schema

### 共用欄位

多數業務 table 繼承 `BaseEntity`：`id`（BIGINT, auto increment）、`created_at`、`updated_at`、`is_deleted`（logical delete, MyBatis-Plus `@TableLogic`）。

例外：
- `credit_transactions` 為 append-only ledger，只保留 `created_at`，不做 `updated_at` / logical delete
- `promo_code_redemptions`、`invite_code_redemptions` 為 append-only redemption log，只保留 `created_at`
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
| invite_code | VARCHAR(20) | UNIQUE, NOT NULL | 個人專屬邀請碼，註冊時自動產生，格式固定 `INV-XXXXXX` |
| created_at | TIMESTAMP | | |
| updated_at | TIMESTAMP | | |
| is_deleted | BOOLEAN | DEFAULT false | |

Index: `users_email_idx`（unique）、`users_username_idx`（unique；PostgreSQL 允許多個 NULL）、`users_invite_code_idx`（unique）

- `users.credit_balance` 是查詢用快照欄位；審計與追溯以 `credit_transactions` ledger 為準

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

### Promo / Invite Code 規則

- 目前 MVP 尚未有 checkout / payment 流程，因此 promo code 與 invite code 的效果統一為 **加贈 credits**，不直接折抵金額
- 邀請碼固定使用 `INV-` namespace；promo code 不可使用 `INV-` 開頭，避免 code collision
- 輸入時一律做 uppercase normalize；同一 promo code 同一 user 最多兌換一次
- 每個 user 最多只能兌換一次他人的 invite code，且不可兌換自己的 invite code
- MVP 不做後台管理 UI；`promo_codes` 先透過 Flyway / SQL 維護，之後再補 admin 後台
- Controller 不直接依 prefix 分流；由 `RewardCodeResolver` 統一做 normalize + resolve，前端可維持單一輸入框

### 業務常數設定（`application.yml`）

所有固定 bonus / cost 由設定檔管理，不直接寫死在 service。只有 template 成本與 promo code 獎勵留在 DB。

```yaml
app:
  credit:
    sign-up-bonus: 50
    create-cv-cost: 10
    pdf-export-cost: 15
  reward:
    invite:
      inviter-bonus: 20
      invitee-bonus: 20
```

### `promo_codes`

| 欄位 | 型態 | 約束 | 說明 |
|---|---|---|---|
| id | BIGINT | PK, auto | |
| code | VARCHAR(50) | UNIQUE, NOT NULL | 優惠碼本體，統一存 uppercase，如 `WELCOME2026` |
| campaign_key | VARCHAR(50) | NULL | 活動識別碼，方便後續報表 / 下架 / 批次管理 |
| reward_type | VARCHAR(30) | NOT NULL | MVP 先支援 `CREDIT_FIXED`，保留未來折扣型 reward 擴充空間 |
| reward_value | INT | NOT NULL | 兌換成功後增加的 credit 數 |
| max_redemptions | INT | NULL | 全站最多可被兌換次數；NULL=不限 |
| starts_at | TIMESTAMP | NULL | 生效時間；NULL=立即生效 |
| expires_at | TIMESTAMP | NULL | 失效時間；NULL=永不過期 |
| is_active | BOOLEAN | DEFAULT true | 是否啟用 |
| created_at | TIMESTAMP | | |
| updated_at | TIMESTAMP | | |
| is_deleted | BOOLEAN | DEFAULT false | |

Index: `promo_codes_code_idx`（unique）

Constraint: `promo_codes.code` 不可使用 `INV-%` namespace

### `promo_code_redemptions`

| 欄位 | 型態 | 約束 | 說明 |
|---|---|---|---|
| id | BIGINT | PK, auto | |
| promo_code_id | BIGINT | FK → promo_codes.id, NOT NULL | |
| user_id | BIGINT | FK → users.id, NOT NULL | |
| reward_value | INT | NOT NULL | 實際發放的 credit 數 |
| created_at | TIMESTAMP | | |

Index: `promo_code_redemptions_promo_user_idx`（unique, promo_code_id + user_id）、`promo_code_redemptions_user_id_idx`

### `invite_code_redemptions`

| 欄位 | 型態 | 約束 | 說明 |
|---|---|---|---|
| id | BIGINT | PK, auto | |
| inviter_user_id | BIGINT | FK → users.id, NOT NULL | 被輸入 invite code 的分享者 |
| invitee_user_id | BIGINT | FK → users.id, NOT NULL | 兌換 invite code 的新用戶 |
| invite_code | VARCHAR(20) | NOT NULL | 冗餘保存當下使用的 code |
| inviter_reward | INT | NOT NULL | 分享者拿到的 credit 數 |
| invitee_reward | INT | NOT NULL | 兌換者拿到的 credit 數 |
| created_at | TIMESTAMP | | |

Index: `invite_code_redemptions_invitee_idx`（unique, invitee_user_id）、`invite_code_redemptions_inviter_idx`

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
| reference_type | VARCHAR(30) | NULL | 關聯資源類型，如 `CV`, `PROMO_REDEMPTION`, `INVITE_REDEMPTION`, `EXPORT_JOB` |
| reference_id | BIGINT | NULL | 關聯資源 ID |
| description | VARCHAR(200) | NULL | 描述，如「建立履歷：軟體工程師」 |
| created_at | TIMESTAMP | | |

Index: `credit_transactions_user_id_idx`、`credit_transactions_user_created_idx`（user_id + created_at DESC）

**交易類型枚舉（type）**:

| type | amount | 說明 |
|---|---|---|
| `SIGN_UP_BONUS` | +50 | 註冊贈送 |
| `DAILY_LOGIN` | +5 | 每日登入獎勵（未來） |
| `CREATE_CV` | -10 | 建立新 CV（onboarding 第 1 份免費） |
| `SWITCH_TEMPLATE` | `-template.credit_cost` | 切換 template，實際扣款以所選 template 的 `credit_cost` 為準 |
| `PROMO_CODE_REDEEM` | +N | 兌換 promo code |
| `INVITE_CODE_REDEEM` | +N | 兌換他人的 invite code，發給 invitee |
| `INVITE_REFERRAL_BONUS` | +N | 自己的 invite code 被使用，發給 inviter |
| `PDF_EXPORT` | -15 | PDF 匯出 |
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

規則：
- `templates.component_key` 是前後端共用的穩定 contract key，必須和前端 `templateRegistry` 的 key 完全一致
- `templates` table 只負責商業 metadata（名稱、成本、啟用狀態、排序），不負責決定實際 import 路徑
- 新增 template 時禁止在 editor / public / print 各自寫 switch-case；統一由 registry resolve component

初始 seed data:

| component_key | display_name_i18n | credit_cost |
|---|---|---|
| minimal | `{"en":"Minimal","zh-CN":"简约","zh-TW":"簡約"}` | 0 |
| sidebar | `{"en":"Sidebar","zh-CN":"侧边栏","zh-TW":"側邊欄"}` | 0 |
| modern | `{"en":"Modern","zh-CN":"现代","zh-TW":"現代"}` | 5 |

### Template 擴充原則

- 單一來源：`src/components/cv-templates/templateRegistry.js`
- 單一渲染入口：`src/components/cv-templates/CvTemplateRenderer.vue`
- 單一 props contract：所有 template component 只接收 `{ cv, sections, mode }`
- `mode` 先定義為 `editor-preview | public | print`，讓同一個 template 可因應列印微調，但不分叉成不同 component
- onboarding、editor、public、print 頁面都只吃 `templateKey`，不可直接 import 特定 template component
- 若 `templateKey` 在 DB 存在但前端 registry 缺失，統一 fallback 到 `MissingTemplate.vue` 並記錄 error log

---

## API Specification

### Response Format

- 全部 API 使用統一包裝：`R<T> = { code, message, data }`
- 下方 Response 欄位描述的是 `data` 內容
- JWT 只寫入 HttpOnly Cookie，不會在 JSON body 回傳 `token`
- 檔案下載型 API（例如 PDF 匯出）例外，直接回傳 binary stream 與正確 `Content-Type`

### Auth（公開）

| Endpoint | Method | Request Body | Response | 說明 |
|---|---|---|---|---|
| `/api/v1/auth/register` | POST | `{ email, password, displayName }` | `{ user }` | 註冊成功後寫入 Cookie，建立 `SIGN_UP_BONUS` 交易，並自動產生 `inviteCode` |
| `/api/v1/auth/login` | POST | `{ email, password }` | `{ user }` | 登入，設定 HttpOnly Cookie |
| `/api/v1/auth/logout` | POST | — | — | 清除 Cookie |
| `/api/v1/auth/me` | GET | — | `{ user }` | 取得當前用戶資料（含 creditBalance, locale, inviteCode） |
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

### Reward / Redeem Code（需登入）

| Endpoint | Method | Request Body | Response | 說明 |
|---|---|---|---|---|
| `/api/v1/me/rewards/summary` | GET | — | `{ balance, inviteCode, inviteStats, promoRedemptions, inviteRedemption }` | 取得 reward dashboard 摘要（credit、invite 成果、promo redemption 狀態） |
| `/api/v1/me/rewards/redeem` | POST | `{ code }` | `{ type, code, creditedAmount, balanceAfter, inviterReward, inviteeReward, message }` | 單一入口兌換 promo code 或 invite code，由後端 resolver 自動判斷類型 |

### Upload（需登入）

| Endpoint | Method | Request Body | Response | 說明 |
|---|---|---|---|---|
| `/api/v1/upload` | POST | multipart/form-data | `{ path }` | 上傳檔案，回傳相對路徑 |

### Export（需登入）

| Endpoint | Method | Request Body | Response | 說明 |
|---|---|---|---|---|
| `/api/v1/me/cvs/{id}/export/pdf` | POST | — | PDF binary | 匯出 PDF，成功時扣 `PDF_EXPORT` 15 credits，不足回 402 |
| `/api/v1/exports/cvs/{id}/payload` | GET | `?token=...` | `{ cv, sections[], templateKey }` | 內部 print route 使用；token 為短效、一次性、只讀 |

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

## Implementation Phases

---

### Phase 1 — Skeleton & Auth

**目標**：前後端專案跑得起來，用戶可以註冊、登入、登出。所有後續 Phase 的基礎。

#### Backend

- [ ] Spring Boot 3.x + Kotlin 專案初始化（`me.hker` package）
- [ ] PostgreSQL + Flyway 設定
- [ ] 全部 Flyway migration scripts（V1~V9，所有 table 一次建好含 seed data）
- [ ] `common/` — `BaseEntity`, `R.kt`, `GlobalExceptionHandler`, `I18nMessageHelper`
- [ ] `config/` — `SecurityConfig`, `MyBatisPlusConfig`, `StaticResourceConfig`, `AppBusinessProperties`
- [ ] `module/auth/` — `AuthController`, `AuthService`, `AuthServiceImpl`, `JwtUtil`, `JwtAuthFilter`
- [ ] `module/user/` — `User` entity, `UserMapper`, `UserService`, `UserServiceImpl`
- [ ] `module/credit/` — `CreditService`, `CreditServiceImpl`（核心扣款/加值邏輯，固定金額全由 properties 注入）
- [ ] `module/template/` — `Template` entity, `TemplateMapper`, `TemplateService`, `TemplateServiceImpl`（讀取 seed data）
- [ ] i18n 資源檔 — `messages.properties`, `messages_zh_CN.properties`, `messages_zh_TW.properties`
- [ ] `application.yml` — DB 連線、JWT secret、upload path、CORS 設定

#### Auth API 實作

| Endpoint | 說明 |
|---|---|
| `POST /api/v1/auth/register` | 註冊 → BCrypt hash → JWT Cookie → `SIGN_UP_BONUS` +50 credit → 產生唯一 `invite_code` |
| `POST /api/v1/auth/login` | 驗證 → JWT Cookie |
| `POST /api/v1/auth/logout` | 清除 Cookie |
| `GET /api/v1/auth/me` | JwtAuthFilter 驗證 → 回傳 user（含 creditBalance, locale, inviteCode） |
| `PUT /api/v1/auth/change-locale` | 更新 `users.locale` |

#### Frontend

- [ ] Vue 3 + Vite 專案初始化
- [ ] Tailwind CSS v3 設定（含 brand colors）
- [ ] vue-i18n 設定（`src/i18n/`，三語系 locale JSON 檔）
- [ ] `api/client.js` — axios instance + Accept-Language interceptor
- [ ] `api/auth.js` — login, register, logout, me, changeLocale
- [ ] `stores/auth.js` — Pinia（user, isLoggedIn, creditBalance, locale, bootstrap）
- [ ] `router/index.js` — 基本 route 定義（auth, onboarding placeholder, dashboard placeholder）
- [ ] `views/auth/LoginView.vue` + `RegisterView.vue`
- [ ] `components/ui/` — Button, Input, Card 基礎組件

#### JWT Auth 設計

**Token 流程**：
1. 登入/註冊 → 產生 JWT → 寫入 HttpOnly Cookie → 回傳 user JSON
2. 每次請求 → JwtAuthFilter 從 Cookie 讀 token → 驗證 → SecurityContext
3. 前端啟動 → 呼叫 `/api/v1/auth/me` bootstrap auth store
4. 登出 → 清除 Cookie

**Cookie 設定**：
```
Name: token, HttpOnly: true, Secure: true(prod)/false(dev), SameSite: Lax, Path: /api, Max-Age: 604800
```

**JwtUtil**：
```kotlin
@Component
class JwtUtil(
    @Value("\${jwt.secret}") private val secret: String,
) {
    companion object {
        private const val EXPIRATION_MS = 7L * 24 * 60 * 60 * 1000
    }

    fun generateToken(userId: Long, email: String): String = TODO()
    fun getUserId(token: String): Long = TODO()
    fun validateToken(token: String): Boolean = TODO()
}
```

#### 驗收標準

- [ ] 用戶可以註冊、登入、登出
- [ ] 登入後 refresh 頁面仍保持登入狀態（bootstrap 機制）
- [ ] 未登入存取需 auth 的 API 回傳 401
- [ ] 註冊後 DB 自動建立 `SIGN_UP_BONUS` 交易，`credit_balance = 50`
- [ ] 註冊後自動產生唯一 `invite_code`
- [ ] 前後端 i18n 基礎可用（切換語系、Accept-Language header）

---

### Phase 2 — Onboarding

**目標**：新用戶完成註冊後，經過 3 步引導建立第一份 CV，完成後進入 Dashboard。

#### Backend

- [ ] `module/onboarding/` — `OnboardingController`, `OnboardingService`, `OnboardingServiceImpl`
- [ ] `module/upload/` — `UploadController`, `StorageService`, `LocalStorageService`
- [ ] `module/user/UserController` — `check-username` endpoint
- [ ] `module/template/TemplateController` — `GET /templates`（從 DB 讀取 + i18n）
- [ ] `module/cv/` — `Cv` entity, `CvMapper`, `CvService`, `CvServiceImpl`（onboarding step3 建立第一份 CV 用）

#### Onboarding API 實作

| Endpoint | 說明 |
|---|---|
| `GET /api/v1/onboarding/status` | 回傳 `{ status, nextStep }` |
| `PUT /api/v1/onboarding/step1` | 更新 displayName, username, avatarPath → status 改為 `STEP_2` |
| `PUT /api/v1/onboarding/step2` | 寫入 `onboarding_draft` → status 改為 `STEP_3` |
| `PUT /api/v1/onboarding/step3` | 用 draft 建立 CV + sections → 清空 draft → status 改為 `DONE` |
| `POST /api/v1/onboarding/skip-step2` | 跳過 step2 → status 直接改為 `STEP_3` |
| `GET /api/v1/users/check-username` | 檢查 username 可用性 |
| `GET /api/v1/templates` | 列出啟用中的 template（含 creditCost, i18n displayName） |
| `POST /api/v1/upload` | 上傳檔案至 `./data/uploads/{userId}/` |

#### Onboarding 流程

**Step 1：基本資料（必填）**
- 輸入 display name（預設為註冊時的 displayName，可改）
- 設定 username（公開頁網址用，需即時檢查唯一性）
- 上傳 avatar（可選；先呼叫 `/api/v1/upload`，再把 `avatarPath` 送到 step1）

**Step 2：專業資訊（可 skip）**
- 手動填寫 summary、experience、education、skills
- 後端先暫存在 `users.onboarding_draft`（key 統一用單數：summary, experience, education, skills）
- 可跳過，之後在 CV editor 裡補
- 未來：LinkedIn 匯入功能放在這步（見 TODO）

**Step 3：選擇 Template（必填）**
- 顯示 template 卡片列表（從 `/api/v1/templates` 取得，含 credit 成本標籤）
- 免費 template 顯示「免費」，付費 template 顯示其 `credit_cost`
- 選一個 template → 後端用 `onboarding_draft` 建立第一份 CV（免費）→ 清空 draft → onboarding 完成

**狀態枚舉**：`STEP_1` → `STEP_2` → `STEP_3` → `DONE`

`onboarding_status` 語意是「下一個必須完成的步驟」。

#### Frontend

- [ ] `api/onboarding.js` — step1, step2, step3, skip, status
- [ ] `api/upload.js` — file upload
- [ ] `api/template.js` — list templates
- [ ] `api/user.js` — checkUsername
- [ ] `views/onboarding/OnboardingLayout.vue` — step progress bar
- [ ] `views/onboarding/Step1BasicInfo.vue` — displayName, username, avatar
- [ ] `views/onboarding/Step2ProfessionInfo.vue` — summary, experience, education, skills
- [ ] `views/onboarding/Step3Template.vue` — template card grid + credit 標籤
- [ ] Router guard（onboarding 攔截 + bootstrap）

#### Onboarding Router Guard

```js
router.beforeEach(async (to) => {
  const auth = useAuthStore()

  if (!auth.initialized) {
    await auth.bootstrap()
  }

  if (!auth.isLoggedIn) {
    if (to.meta.requiresAuth) return '/login'
    return true
  }

  if (auth.user.onboarding_status !== 'DONE') {
    if (!to.path.startsWith('/onboarding')) {
      const stepMap = {
        STEP_1: '/onboarding/step1',
        STEP_2: '/onboarding/step2',
        STEP_3: '/onboarding/step3',
      }
      return stepMap[auth.user.onboarding_status] || '/onboarding/step1'
    }
    return true
  }

  if (to.path.startsWith('/onboarding')) {
    return '/dashboard'
  }

  return true
})
```

#### File Upload 設計

**後端**：
```kotlin
@Configuration
class StaticResourceConfig(
    @Value("\${app.upload.path:./data/uploads}") private val uploadPath: String,
) : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:$uploadPath/")
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

**路徑規則**：
- Avatar: `/uploads/{userId}/avatar.{ext}`
- 其他資產: `/uploads/{userId}/{uuid}.{ext}`
- DB 只存相對路徑，前端用 `baseURL + path` 組合完整 URL
- 上傳同名檔案直接覆蓋
- onboarding step1 只接受 `avatarPath`，不直接收 multipart 檔案

#### 驗收標準

- [ ] 註冊後自動導向 onboarding step1
- [ ] Step 1 設定 username 後可進入 step2（username 即時檢查唯一性）
- [ ] Step 2 可填寫或 skip
- [ ] Step 3 選 template 後自動建立第一份 CV（含 draft 資料寫入 sections）
- [ ] Onboarding 完成 → `status = DONE` → 導向 dashboard
- [ ] 已完成 onboarding 的用戶不能再進入 `/onboarding`
- [ ] 刷新頁面後正確導向當前步驟（不會從頭開始）
- [ ] Avatar 上傳可正常運作

---

### Phase 3 — CV Editor, Dashboard & Rewards

**目標**：用戶可以在 Dashboard 管理多份 CV、兌換 promo / invite code，並在 Editor 中編輯履歷與匯出 PDF。

#### Backend

- [ ] `module/cv/CvController` — CV CRUD（完整實作，含 credit 扣款）
- [ ] `module/cv/CvService` + `CvServiceImpl` — 建立 CV 扣 `CREATE_CV` credit、切換 template 扣 `SWITCH_TEMPLATE` credit
- [ ] `module/cv/PublicCvController` — 公開 CV API（Phase 4 實作前端）
- [ ] `module/credit/CreditController` — balance + transactions API
- [ ] `module/credit/CreditService` + `CreditServiceImpl` — balance、ledger、扣款
- [ ] `module/reward/RewardController` — reward summary + redeem code API
- [ ] `module/reward/RewardService` + `RewardServiceImpl` — redemption orchestration（事務、ledger、summary）
- [ ] `module/reward/RewardCodeResolver` — normalize + resolve promo / invite code
- [ ] `module/export/PdfExportController` — PDF export API
- [ ] `module/export/PdfExportService` + `PdfExportServiceImpl` — export token、renderer client、credit 扣款整合
- [ ] `integration/pdf/PdfRendererClient` — 呼叫內部 pdf-renderer service

#### CV API 實作

| Endpoint | 說明 |
|---|---|
| `GET /api/v1/me/cvs` | 列出我的 CV |
| `POST /api/v1/me/cvs` | 新建 CV（扣 `CREATE_CV` -10 credit；不足回 402） |
| `GET /api/v1/me/cvs/{id}` | 取得 CV + sections |
| `PUT /api/v1/me/cvs/{id}` | 更新 metadata（換 template 時按 `credit_cost` 扣 credit） |
| `DELETE /api/v1/me/cvs/{id}` | Logical delete |
| `PUT /api/v1/me/cvs/{id}/sections` | 批次更新 sections（整份取代） |

#### Credit API 實作

| Endpoint | 說明 |
|---|---|
| `GET /api/v1/me/credits/balance` | 查詢餘額 |
| `GET /api/v1/me/credits/transactions` | 交易紀錄（分頁） |

#### Reward API 實作

| Endpoint | 說明 |
|---|---|
| `GET /api/v1/me/rewards/summary` | 回傳 `{ balance, inviteCode, inviteStats, promoRedemptions, inviteRedemption }`，供 dashboard reward 卡片直接使用 |
| `POST /api/v1/me/rewards/redeem` | 接收 `{ code }`，成功後回傳 `{ type, code, creditedAmount, balanceAfter, inviterReward, inviteeReward, message }` |

#### Export API 實作

| Endpoint | 說明 |
|---|---|
| `POST /api/v1/me/cvs/{id}/export/pdf` | 產生並下載 PDF；成功後扣 `PDF_EXPORT` -15 credit，失敗不扣款 |
| `GET /api/v1/exports/cvs/{id}/payload?token=...` | print route 專用資料來源；token 短效且一次性 |

#### Credit 系統

**核心邏輯**：
- 新用戶註冊時建立 `SIGN_UP_BONUS` 交易
- Onboarding 第 1 份 CV 免費，之後每份 CV 成本由 `app.credit.create-cv-cost` 控制
- Template 切換由 `templates.credit_cost` 決定；0=免費，>0 扣對應數值
- PDF 匯出成本由 `app.credit.pdf-export-cost` 控制
- 扣 credit 前檢查餘額，不足回傳 `402 Payment Required`
- 所有交易記錄寫入 `credit_transactions`
- Service 禁止直接寫死 `50 / 10 / 15 / 20` 等數值，統一走 `AppBusinessProperties`

**CreditService**：
```kotlin
interface CreditService {
    fun hasEnoughCredits(userId: Long, amount: Int): Boolean

    @Transactional
    fun deduct(
        userId: Long,
        amount: Int,
        type: String,
        referenceType: String?,
        referenceId: Long?,
        description: String?,
    )

    @Transactional
    fun credit(
        userId: Long,
        amount: Int,
        type: String,
        referenceType: String?,
        referenceId: Long?,
        description: String?,
    )

    fun getBalance(userId: Long): Int
    fun getTransactions(userId: Long, page: Int, size: Int): IPage<CreditTransaction>
}

@Service
class CreditServiceImpl(
    private val userMapper: UserMapper,
    private val creditTransactionMapper: CreditTransactionMapper,
    private val businessProperties: AppBusinessProperties,
) : CreditService {
    override fun hasEnoughCredits(userId: Long, amount: Int): Boolean = TODO()

    @Transactional
    override fun deduct(
        userId: Long,
        amount: Int,
        type: String,
        referenceType: String?,
        referenceId: Long?,
        description: String?,
    ) = Unit

    @Transactional
    override fun credit(
        userId: Long,
        amount: Int,
        type: String,
        referenceType: String?,
        referenceId: Long?,
        description: String?,
    ) = Unit

    override fun getBalance(userId: Long): Int = TODO()
    override fun getTransactions(userId: Long, page: Int, size: Int): IPage<CreditTransaction> = TODO()
}
```

#### Reward / Redeem Code 系統

**核心邏輯**：
- 每個 user 在註冊時自動拿到一組可分享的 `invite_code`
- Dashboard 可提供單一輸入框，但 controller 只收 `code` + `codeType`
- `RewardCodeResolver` 流程：normalize uppercase → 依 `codeType=AUTO/PROMO/INVITE` 決定 lookup 策略 → 回傳 resolved code type 與目標資源
- Promo code 驗證順序：存在 → 啟用中 → 在有效期間內 → `COUNT(redemptions) < max_redemptions` → 使用者未兌換過
- Invite code 驗證順序：code 存在 → 不是自己的 code → 尚未兌換過其他 invite code
- Invite code 兌換成功時，同一 transaction 內同時發放 invitee 與 inviter credit，並寫入兩筆 `credit_transactions`
- 所有 code 比對都採 case-insensitive，DB 統一存 uppercase
- 邀請獎勵金額由 `app.reward.invite.*` 控制，不寫死在 service

**預設獎勵**：
- Invitee bonus：`app.reward.invite.invitee-bonus`
- Inviter bonus：`app.reward.invite.inviter-bonus`
- Promo code bonus：依 `promo_codes.reward_value` 決定

#### PDF 匯出設計

**MVP 方案**：
- Spring API 不直接內嵌 Node runtime；改由內部 `pdf-renderer` service 執行 Puppeteer
- 使用者點匯出後，Spring 先驗證 CV 擁有權與 credit 餘額，產生一次性、60 秒有效的 export token
- `pdf-renderer` 以短效 token 開啟前端 `print` route，該頁重用既有 template component 並向 `/api/v1/exports/cvs/{id}/payload` 讀取唯讀資料
- 成功產出 PDF 後才提交 `PDF_EXPORT` 扣款；render 失敗則 rollback，不扣 credits
- `pdf-renderer` 為 stateless service，不接 DB、不做 auth/credit 業務邏輯，只負責把 URL render 成 PDF

#### Frontend

- [ ] `api/cv.js` — CV CRUD + sections
- [ ] `api/credit.js` — balance, transactions
- [ ] `api/reward.js` — summary, redeem
- [ ] `api/export.js` — export PDF
- [ ] `stores/cv.js` — Pinia（currentCv, sections）
- [ ] `views/dashboard/DashboardView.vue` — CV 列表 + 新建按鈕 + credit 餘額 + redeem code 區塊 + invite code 卡片
- [ ] `views/editor/CvEditorView.vue` — section editor + live preview + export PDF
- [ ] `views/print/PrintCvView.vue` — PDF 專用列印頁，重用 template registry
- [ ] `components/cv-templates/CvTemplateRenderer.vue` — 統一 template renderer wrapper
- [ ] `components/cv-templates/MissingTemplate.vue` — registry 缺失時的 fallback
- [ ] `components/sections/` — SummarySection, ExperienceSection, EducationSection, SkillsSection
- [ ] `components/cv-templates/MinimalTemplate.vue` — 第一個 template
- [ ] `components/cv-templates/templateRegistry.js` — manifest + dynamic import + fallback resolve

#### CV Editor 資料流

1. 進入 editor → `GET /api/v1/me/cvs/{id}` 取得 CV + sections
2. 用戶編輯 section → 前端即時更新 local state（Pinia）
3. Live preview 統一透過 `CvTemplateRenderer` 依 `templateKey` 渲染，資料從 Pinia 讀取
4. 用戶點「儲存」→ `PUT /api/v1/me/cvs/{id}/sections` 一次送出所有 sections（整份取代）
5. Section 的排序用 drag & drop 修改 `sort_order`
6. 用戶點「匯出 PDF」→ `POST /api/v1/me/cvs/{id}/export/pdf` → 後端產 PDF 並回傳下載

#### Template 機制

**新增 Template 流程**：
1. 在 `src/components/cv-templates/` 新增 Vue component，props 固定接收 `{ cv, sections, mode }`
2. 在 `templateRegistry.js` 新增 manifest entry：`key`, `loader`, `supportsPrint`
3. 如需縮圖，新增 preview asset
4. 在 DB `templates` table 新增一筆 seed，`component_key` 必須與 registry `key` 完全一致
5. editor preview、public 頁、print 頁都透過 `CvTemplateRenderer` 自動生效，不需額外補 route-specific 邏輯

**維護規範**：
- 禁止在 `CvEditorView.vue`、`PublicCvView.vue`、`PrintCvView.vue` 直接 import 單一 template component
- 新增 template 允許修改的前端位置應盡量限制在：template component、registry、preview asset
- 成本、排序、上下架由 DB 控制；視覺渲染由前端 registry 控制，責任分離
- 至少有一個 contract check 驗證 `templates.component_key` 與前端 registry key 一致

#### 驗收標準

- [ ] Dashboard 顯示用戶所有 CV（含第一份 from onboarding）
- [ ] 可建立新 CV（credit 不足時提示）
- [ ] 可刪除 CV
- [ ] CV Editor 正確載入 sections，編輯後可儲存
- [ ] Live preview 即時反映編輯內容
- [ ] MinimalTemplate 可正確渲染 4 種 section type
- [ ] 建立 CV 時扣 10 credits，credit 不足回傳 402
- [ ] 切換付費 template 時扣 credit + 確認對話框
- [ ] Dashboard 顯示 credit 餘額
- [ ] 可成功兌換有效 promo code，餘額立即更新
- [ ] 可成功兌換他人的 invite code 一次；重複兌換與 self-invite 會被阻擋
- [ ] Dashboard 顯示自己的 invite code 與邀請成果摘要
- [ ] PDF 匯出成功時扣 15 credits，餘額不足回傳 402
- [ ] PDF render 失敗時不扣款
- [ ] 新增一個 template 時，不需改 editor/public/print 三個 view 的渲染分支
- [ ] DB 回傳的每個 `templateKey` 都能被 `templateRegistry` resolve；缺失時有 fallback 與錯誤紀錄

---

### Phase 4 — Public Pages & 多 Template

**目標**：CV 可以公開分享，使用者有更多 template 可選擇。

#### Backend

- [ ] `module/cv/PublicCvController` — 完整實作公開 API（Phase 3 已建立，此階段串接前端）

#### Public CV API

| Endpoint | 說明 |
|---|---|
| `GET /api/v1/public/{username}` | 取得公開用戶資料與公開 CV 清單 |
| `GET /api/v1/public/{username}/{slug}` | 取得單份公開 CV（含 sections + templateKey） |

#### Frontend

- [ ] `views/public/PublicProfileView.vue` — `/u/{username}` 公開履歷清單
- [ ] `views/public/PublicCvView.vue` — `/u/{username}/{slug}` 動態 template 渲染
- [ ] CV Editor 新增「公開/隱藏」切換 + slug 設定
- [ ] `components/cv-templates/SidebarTemplate.vue` — 第二個 template
- [ ] `components/cv-templates/ModernTemplate.vue` — 第三個 template（付費，5 credits）

#### Public CV 渲染

- `/u/{username}` 對應 `PublicProfileView.vue`，顯示使用者公開資訊與公開 CV 清單
- `/u/{username}/{slug}` 對應 `PublicCvView.vue`，統一透過 `CvTemplateRenderer` 根據 API 回傳的 `templateKey` 動態渲染
- 短期純 CSR，未來如需 SEO 可改用 Nuxt 或 SSR 方案

#### 驗收標準

- [ ] 用戶可設定 CV 為公開並自訂 slug
- [ ] 任何人可透過 `/u/{username}/{slug}` 查看公開 CV
- [ ] 三個 template（minimal, sidebar, modern）皆可正確渲染
- [ ] 未登入用戶也可瀏覽公開 CV
- [ ] 公開 CV 使用對應 template component 動態渲染
- [ ] 新增 template 後，public page 不需額外增加 if/else 或 switch-case

---

### Phase 5 — i18n 完整 & UI Polish

**目標**：三語系完整覆蓋所有頁面，UI 體驗打磨。

#### 前端 i18n

vue-i18n 設定（Phase 1 已建好基礎，此階段補齊所有翻譯 key）：

```js
// src/i18n/index.js
import { createI18n } from 'vue-i18n'
import en from './locales/en.json'
import zhCN from './locales/zh-CN.json'
import zhTW from './locales/zh-TW.json'

const i18n = createI18n({
  legacy: false,
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
  "reward": {
    "redeem": "Redeem Code",
    "inviteCode": "Invite Code",
    "invalid": "Invalid or unavailable code.",
    "selfInvite": "You cannot redeem your own invite code."
  },
  "editor": {
    "addSection": "Add Section",
    "summary": "Summary",
    "experience": "Experience",
    "education": "Education",
    "skills": "Skills"
  },
  "export": {
    "pdf": "Export PDF",
    "failed": "PDF export failed. Please try again."
  },
  "credit": {
    "insufficient": "Insufficient credits. This action requires {cost} credits, you have {balance}.",
    "createCv": "Create Resume",
    "switchTemplate": "Switch Template"
  }
}
```

#### 後端 i18n

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
error.reward.code_invalid=此優惠碼或邀請碼無效
error.reward.self_invite=不可兌換自己的邀請碼
error.reward.invite_already_redeemed=你已經兌換過邀請碼
error.export.pdf_failed=PDF 匯出失敗，請稍後再試
```

#### 語系切換流程

1. 前端 localStorage 存 `locale`，vue-i18n 即時切換 UI 語系
2. 呼叫 `PUT /api/v1/auth/change-locale` 將偏好存到 DB（`users.locale`）
3. 之後登入時自動從 user profile 讀取 locale 設定
4. API 請求統一帶 `Accept-Language: zh-TW` header（axios interceptor 處理）

#### UI Polish

- [ ] 所有頁面 `<t(...)>` 或 `$t(...)` 取代硬編碼文字
- [ ] 語系切換 UI（header dropdown）
- [ ] Credit 餘額 + 交易紀錄頁面
- [ ] Loading states / error states / empty states
- [ ] Mobile responsive（Tailwind responsive classes）
- [ ] 表單驗證提示（i18n）

#### 驗收標準

- [ ] 三語系切換正確，無遺漏 key
- [ ] 後端錯誤訊息根據 Accept-Language 回傳正確語系
- [ ] 語系偏好持久化（登入後自動套用）
- [ ] Credit 交易紀錄頁面可查詢歷史
- [ ] 所有頁面在手機尺寸下可用

---

## 專案結構（完整參考）

### Backend（Spring Boot + Kotlin + MyBatis-Plus）

```
src/main/kotlin/me/hker/
├── config/
│   ├── SecurityConfig.kt             # JWT filter chain, CORS whitelist
│   ├── MyBatisPlusConfig.kt          # pagination plugin, logic delete
│   ├── StaticResourceConfig.kt       # /uploads/** 本地靜態資源
│   └── AppBusinessProperties.kt      # signup/create/export/invite rewards
├── common/
│   ├── BaseEntity.kt                 # id, created_at, updated_at, is_deleted
│   ├── R.kt                          # 統一回傳格式 { code, message, data }
│   ├── GlobalExceptionHandler.kt
│   └── I18nMessageHelper.kt          # 封裝 MessageSource，根據 Accept-Language 取訊息
├── module/
│   ├── auth/
│   │   ├── AuthController.kt         # /api/v1/auth/**
│   │   ├── JwtUtil.kt                # generate / parse / validate token
│   │   ├── JwtAuthFilter.kt          # OncePerRequestFilter, Cookie 讀 token
│   │   └── service/
│   │       ├── AuthService.kt
│   │       └── impl/
│   │           └── AuthServiceImpl.kt
│   ├── user/
│   │   ├── UserController.kt         # /api/v1/users/check-username
│   │   ├── entity/User.kt
│   │   ├── mapper/UserMapper.kt      # BaseMapper<User>
│   │   └── service/
│   │       ├── UserService.kt
│   │       └── impl/
│   │           └── UserServiceImpl.kt
│   ├── onboarding/
│   │   ├── OnboardingController.kt   # /api/v1/onboarding/**
│   │   └── service/
│   │       ├── OnboardingService.kt
│   │       └── impl/
│   │           └── OnboardingServiceImpl.kt
│   ├── cv/
│   │   ├── CvController.kt           # /api/v1/me/cvs/**
│   │   ├── PublicCvController.kt     # /api/v1/public/**
│   │   ├── entity/Cv.kt
│   │   ├── entity/CvSection.kt
│   │   ├── mapper/CvMapper.kt
│   │   ├── mapper/CvSectionMapper.kt
│   │   └── service/
│   │       ├── CvService.kt
│   │       └── impl/
│   │           └── CvServiceImpl.kt
│   ├── template/
│   │   ├── TemplateController.kt     # /api/v1/templates
│   │   ├── entity/Template.kt
│   │   ├── mapper/TemplateMapper.kt
│   │   └── service/
│   │       ├── TemplateService.kt
│   │       └── impl/
│   │           └── TemplateServiceImpl.kt
│   ├── credit/
│   │   ├── CreditController.kt       # /api/v1/me/credits/**
│   │   ├── entity/CreditTransaction.kt
│   │   ├── mapper/CreditTransactionMapper.kt
│   │   └── service/
│   │       ├── CreditService.kt      # 扣款、加值、查詢
│   │       └── impl/
│   │           └── CreditServiceImpl.kt
│   ├── reward/
│   │   ├── RewardController.kt       # /api/v1/me/rewards/**
│   │   ├── RewardCodeResolver.kt
│   │   ├── entity/PromoCode.kt
│   │   ├── entity/PromoCodeRedemption.kt
│   │   ├── entity/InviteCodeRedemption.kt
│   │   ├── mapper/PromoCodeMapper.kt
│   │   ├── mapper/PromoCodeRedemptionMapper.kt
│   │   ├── mapper/InviteCodeRedemptionMapper.kt
│   │   └── service/
│   │       ├── RewardService.kt
│   │       └── impl/
│   │           └── RewardServiceImpl.kt
│   ├── export/
│   │   ├── PdfExportController.kt    # /api/v1/me/cvs/*/export/pdf
│   │   └── service/
│   │       ├── PdfExportService.kt
│   │       └── impl/
│   │           └── PdfExportServiceImpl.kt
│   ├── integration/
│   │   └── pdf/
│   │       └── PdfRendererClient.kt
│   └── upload/
│       ├── UploadController.kt       # /api/v1/upload
│       └── service/
│           ├── StorageService.kt
│           └── impl/
│               └── LocalStorageService.kt  # saves to ./data/uploads/{userId}/
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
        ├── V6__create_promo_codes.sql
        ├── V7__create_promo_code_redemptions.sql
        ├── V8__create_invite_code_redemptions.sql
        └── V9__seed_templates.sql
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
│   ├── reward.js                   # summary, redeem
│   ├── export.js                   # export PDF
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
│   │   └── DashboardView.vue       # list of CVs, create new, reward card
│   ├── editor/
│   │   └── CvEditorView.vue        # section editor + live preview + export
│   ├── print/
│   │   └── PrintCvView.vue         # /print/cvs/:id?token=...
│   └── public/
│       ├── PublicProfileView.vue   # /u/{username} — 公開履歷清單
│       └── PublicCvView.vue        # /u/{username}/{slug} — dynamic template render
├── components/
│   ├── cv-templates/
│   │   ├── CvTemplateRenderer.vue  # single renderer for editor/public/print
│   │   ├── MissingTemplate.vue     # fallback when registry key is missing
│   │   ├── templateRegistry.js     # manifest + dynamic imports + fallback
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

### PDF Renderer（Node + Puppeteer）

```
services/
└── pdf-renderer/
    ├── package.json
    └── src/
        ├── server.js               # internal HTTP endpoint for render requests
        └── renderPdf.js            # opens print route and returns PDF buffer
```

---

## TODO（未來規劃）

- [ ] **LinkedIn 匯入**: 用戶貼上 LinkedIn public profile URL → 後端解析（需評估可行方案：LinkedIn API / 匯出檔上傳 / scraping），預估扣 10 credits
- [ ] **Flutter App**: 共用後端 API，WebView 渲染 CV template 或 Flutter 原生 UI
- [ ] **Refresh Token + Redis**: 引入 Redis 做 token blacklist 和 refresh token 管理
- [ ] **CV Auto-save**: debounce 自動儲存
- [ ] **SEO**: Public CV 頁面改用 SSR（Nuxt 或 Spring Thymeleaf）
- [ ] **OAuth 登入**（Google、GitHub）
- [ ] **Promo Code 後台管理**: 建立 admin UI 管理 campaign、code 批次匯入與停用
- [ ] **PDF 匯出版型調校**: 分頁控制、字型嵌入、長內容截斷與 watermark
- [ ] **Credit 加值**: 付費購買 credit（串接第三方支付）
- [ ] **每日登入獎勵**: 每日首次登入贈送 5 credits
