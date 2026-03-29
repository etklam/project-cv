# Technical Debt Registry

> Last updated: 2026-03-29
> Project: Project CV
> Scope: active debt only; stale items moved to resolved notes

This file tracks currently active technical debt that was verified against the codebase on 2026-03-29.

---

## Executive Summary

| Severity | Count | Status |
|----------|-------|--------|
| CRITICAL | 1 | 🔴 Action Required |
| HIGH | 2 | 🟠 Planned |
| MEDIUM | 4 | 🟡 Backlog |
| LOW | 0 | 🟢 None tracked |

---

## Active Debt

### #31: PDF 导出无认证上下文传递给渲染器

**Severity:** CRITICAL

**Files:**
- `backend/src/main/kotlin/me/hker/module/export/service/impl/PdfExportServiceImpl.kt`
- `backend/src/main/kotlin/me/hker/module/export/integration/HttpPdfRendererClient.kt`
- `services/pdf-renderer/src/renderPdf.js`
- `frontend/src/views/print/PrintCvView.vue`

**Description:**
PDF 导出依赖受保护的前端页面 `/print/cvs/{id}`。后端只把 URL 交给 renderer，renderer 用全新的 Puppeteer session 直接 `page.goto(url)`，没有转发任何 cookie、JWT 或一次性 token。该页面随后又会请求需要认证的 `/me/cvs/:id` API。

**Why this is debt:**
- 本地手动测试可能“看起来能用”，但服务到服务的真实导出路径没有认证上下文
- 认证、渲染、计费横跨三个边界（backend / frontend / renderer），故障排查成本高
- 当前设计把一个后端能力建立在浏览器登录态页面之上，耦合过重

**Impact:**
🔴 生产环境中 PDF 导出高概率失败，最坏情况下用户拿到的是错误页或登录页截图

**Remediation:**
1. 生成短期有效的 signed export token，并把它传给 renderer
2. 让 `PrintCvView.vue` 或独立导出端点接受该 token，而不是依赖 cookie 登录态
3. 更稳妥的方案是让 renderer 直接接收后端整理好的 CV 数据，不再抓取受保护页面

**Estimated Effort:** 6 hours

---

### #32: Onboarding 流程端到端断裂

**Severity:** HIGH

**Files:**
- `frontend/src/router/index.js`
- `frontend/src/views/onboarding/Step1BasicInfo.vue`
- `frontend/src/views/onboarding/Step2ProfessionInfo.vue`
- `frontend/src/views/onboarding/Step3Template.vue`
- `backend/src/main/kotlin/me/hker/module/auth/dto/AuthDtos.kt`
- `backend/src/main/kotlin/me/hker/common/EntityDtos.kt`
- `backend/src/main/kotlin/me/hker/module/onboarding/service/impl/OnboardingServiceImpl.kt`

**Description:**
前端路由守卫依赖 `auth.user?.onboarding_status`，但 auth DTO 目前没有返回 onboarding 字段。Step 1/2/3 页面主要是前端路由跳转，后端 onboarding service 仍是固定 stub，前后端契约没有闭环。

**Impact:**
🟠 新用户引导流程不可靠，状态持久化、页面跳转和 bootstrap 返回值彼此不同步

**Remediation:**
1. 在 auth 返回 DTO 中显式加入 onboarding 状态字段
2. 为 onboarding 实现真实 controller/service/persistence
3. 让各 step 提交真实 API，而不是只做路由跳转
4. 为 bootstrap guard 增加契约测试

**Estimated Effort:** 8 hours

---

### #33: Admin 端点授权依赖手动调用 resolver

**Severity:** HIGH

**Files:**
- `backend/src/main/kotlin/me/hker/config/SecurityConfig.kt`
- `backend/src/main/kotlin/me/hker/module/admin/controller/*`

**Description:**
安全配置当前是 `/api/v1/me/**` 需要认证，但其余请求最终落到 `anyRequest().permitAll()`。`/api/v1/admin/**` 之所以没裸奔，是因为每个 admin controller 手动调用了 `adminUserResolver.resolveAdmin()`。

**Impact:**
🟠 只要未来新增一个 admin endpoint 忘记加 resolver，就会静默暴露管理功能

**Remediation:**
1. 在 `SecurityConfig.kt` 中直接保护 `/api/v1/admin/**`
2. 用 `@PreAuthorize` 或统一 security rule 取代手写 resolver 校验
3. 保留 resolver 只做“取当前 admin user”而不是“承担授权本身”

**Estimated Effort:** 3 hours

---

### #34: Editor UI 依赖后端未返回的字段

**Severity:** MEDIUM

**Files:**
- `frontend/src/views/editor/CvEditorView.vue`
- `frontend/src/stores/cv.js`
- `backend/src/main/kotlin/me/hker/module/cv/dto/CvDtos.kt`

**Description:**
编辑器尝试从 `cvStore.currentCv?.user?.username` 构建公开 URL，但当前 `CvDetailDto` 并没有 `user` 或 `username`。实际效果是 UI 会回退到 `{username}` 占位符。

**Impact:**
🟡 前后端契约漂移，继续扩展 editor 时会持续制造“前端假定后端有字段”的问题

**Remediation:**
1. 明确在 CV detail payload 中返回 `username`
2. 或让前端不显示无法保证正确的数据
3. 为 editor payload 增加契约测试

**Estimated Effort:** 2 hours

---

### #35: Autosave 每次都序列化整份草稿并进行双写

**Severity:** MEDIUM

**Files:**
- `frontend/src/views/editor/CvEditorView.vue`
- `frontend/src/composables/useEditorAutosave.js`

**Description:**
editor 的 watch source 使用 `JSON.stringify(...)` 比较整份 metadata + sections，触发后 autosave 顺序调用 `saveMetadata()` 和 `saveSections()` 两次 API。

**Impact:**
🟡 文档变大后性能会恶化，且一次 autosave 内出现部分成功、部分失败的机会更高

**Remediation:**
1. 合并 metadata / sections 为单个保存端点
2. 改成 dirty tracking，而不是整个草稿序列化
3. 对高频编辑区域做更细粒度的 debounce

**Estimated Effort:** 4 hours

---

### #36: 测试套件本身存在债务，无法保护关键路径

**Severity:** MEDIUM

**Files:**
- `frontend/tests/`
- `backend/src/test/`

**Description:**
当前测试集不是绿色状态。`frontend npm test` 失败在 dashboard view tests；`backend ./gradlew test` 因 `InviteCodeRewardRedemptionServiceImplTest` 缺少 `userMapper` 参数而无法编译。与此同时，PDF export auth 和 onboarding contract 这类高风险路径仍缺少足够保护。

**Impact:**
🟡 CI / local test signal 不可信，开发者容易忽略失败，也难以判断新改动是否安全

**Remediation:**
1. 先恢复测试集为全绿
2. 补上 PDF export / onboarding / admin security 的契约测试
3. 把测试失败设为合并阻断条件

**Estimated Effort:** 4 hours

---

### #37: Auth cookie 策略仍写死开发配置

**Severity:** MEDIUM

**Files:**
- `backend/src/main/kotlin/me/hker/module/auth/service/impl/AuthServiceImpl.kt`

**Description:**
auth cookie 目前固定为 `secure(false)`、`sameSite("Lax")`、`path("/api")`，没有环境化配置。对本地开发方便，但生产部署策略被硬编码在 service 里。

**Impact:**
🟡 部署到不同环境时容易出现安全性和兼容性问题，且配置变更需要改代码

**Remediation:**
1. 将 cookie `secure` / `sameSite` / `path` 提取到配置
2. 区分 local / staging / production 默认值
3. 为登录 cookie 行为增加集成测试

**Estimated Effort:** 2 hours

---

## Recommended Order

### Phase 1: Production Blockers

| Issue | Effort | Priority |
|-------|--------|----------|
| #31 PDF export auth context | 6h | P0 |
| #32 Onboarding end-to-end | 8h | P0 |
| #33 Admin authorization model | 3h | P0 |

**Total:** 17 hours

### Phase 2: Contract and Reliability

| Issue | Effort | Priority |
|-------|--------|----------|
| #34 Editor contract drift | 2h | P1 |
| #35 Autosave architecture | 4h | P1 |
| #36 Test suite health | 4h | P1 |
| #37 Cookie config hardcoding | 2h | P2 |

**Total:** 12 hours

---

## Recently Resolved / Stale Entries

The following items were present in older versions of this document but are no longer accurate as active debt:

- `#1 JWT 伪签名实现`
  Current `JwtUtil.kt` uses `jjwt`, HMAC signing, and signed-claim parsing.
- `#2 Mock 认证实现`
  `AuthServiceImpl.kt` now creates users, stores password hashes, validates passwords, and persists locale.
- `#3 Credit 扣款无事务包裹`
  `CvServiceImpl` is transactional and `create()` now deducts after DB work succeeds.
- `#5 CV 删除无权限校验`
  `findOwnedCv()` now performs explicit ownership checks.
- `#6 Slug 没有唯一性检查`
  `ensureSlugAvailable()` exists and is called in create/update flows.
- `#7 Export token 无实际验证`
  Old `X-User-Id` concern is outdated; current active problem is tracked as `#31`.
- `#9 JSON.parse 无 try-catch`
  This appears to reference an older editor implementation and is no longer grounded in current code.
- `#10 Mobile menu 按钮无功能`
  Mobile menu toggle exists in `frontend/src/App.vue`.
- `#11 Axios 缺少 response interceptor`
  `frontend/src/api/client.js` already has 401/402 interceptor handling.
- `#17 CV store 缺少 reset action`
  `frontend/src/stores/cv.js` already has `reset()`.
- `#18 Redeem 后没有刷新 CV 列表`
  `DashboardView.vue` calls `loadMyCvs()` after successful redeem.

Resolved items should stay here for auditability, but should not be counted in the active debt summary.

---

## Notes

- This registry only counts debt that was verified in the current codebase.
- If an item is fixed, move it to `Recently Resolved / Stale Entries` instead of leaving it in active counts.
- Do not mark an item `✅ Fixed` in a roadmap section while keeping the same item open in the active registry.
