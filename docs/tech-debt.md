# Technical Debt Registry

> Last updated: 2026-03-28
> Project: Project CV

This document tracks all identified technical debt items with their severity, impact, and remediation plans.

---

## Executive Summary

| Severity | Count | Status |
|----------|-------|--------|
| CRITICAL | 5 | 🔴 Action Required |
| HIGH | 6 | 🟠 Planned |
| MEDIUM | 10 | 🟡 Backlog (Phase 3 completed) |
| LOW | 10 | 🟢 Optional |

---

## CRITICAL (需立即修复)

### #1: JWT 安全漏洞 - 伪签名实现

**File:** `backend/src/main/kotlin/me/hker/module/auth/JwtUtil.kt`

**Description:** JWT 不是真正的签名 JWT — 用 Base64 编码 `userId:email:secret`，没有 HS256/RS256 签名。知道 secret 就能伪造任意 token。

**Impact:** 🔴 安全严重漏洞 - 攻击者可伪造任意用户身份

**Remediation:**
```kotlin
// 使用 jjwt 或类似库实现真正的 JWT 签名
implementation("io.jsonwebtoken:jjwt-api:0.12.3")
implementation("io.jsonwebtoken:jjwt-impl:0.12.3")
runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

val key = Keys.hmacShaKeyFor(secret.toByteArray())
val jws = Jwts.builder()
    .subject(userId.toString())
    .claim("email", email)
    .issuedAt(Date())
    .expiration(Date(System.currentTimeMillis() + expirationMs))
    .signWith(key)
    .compact()
```

**Estimated Effort:** 4 hours

---

### #2: Mock 认证实现

**File:** `backend/src/main/kotlin/me/hker/module/auth/service/impl/AuthServiceImpl.kt`

**Description:**
- 返回硬编码 userId 1L
- 不验证密码
- localeOverrides 存在内存，重启后丢失
- 没有真正创建用户

**Impact:** 🔴 生产环境完全不可用

**Remediation:**
1. 实现 User 实体和 UserRepository
2. 实现真正的密码验证 (BCrypt)
3. 实现 locale 持久化
4. 实现真正的用户注册流程

**Estimated Effort:** 16 hours

---

### #3: Credit 扣款无事务包裹

**File:** `backend/src/main/kotlin/me/hker/module/cv/service/impl/CvServiceImpl.kt`

**Description:** `create()` 和 `updateMetadata()` 中先扣 credit 再写 DB，如果 DB 写入失败不会回滚 credit 扣款。

**Impact:** 🔴 用户付了钱但没得到服务

**Remediation:**
```kotlin
@Transactional
fun create(request: CreateCvRequest): CvDto {
    // 1. 先写 DB
    val cv = cvRepository.save(...)
    // 2. 再扣 credit
    creditService.deductCredits(userId, cost)
    return cv
}
```

**Estimated Effort:** 2 hours

---

### #4: PDF 导出后扣款不可回滚

**File:** `backend/src/main/kotlin/me/hker/module/export/service/impl/PdfExportServiceImpl.kt`

**Description:** `exportPdf()` 先扣款再调用 renderer，renderer 失败不会退回 credit。

**Impact:** 🔴 导出失败仍扣费

**Remediation:**
```kotlin
@Transactional
fun exportPdf(...): ByteArray {
    val credits = creditService.getBalance(userId)
    if (credits < cost) throw InsufficientCreditsException()

    // 1. 先生成 PDF
    val pdf = renderer.generate(cv, template)

    // 2. 成功后再扣款
    creditService.deductCredits(userId, cost, "pdf_export", cvId)

    return pdf
}
```

**Estimated Effort:** 2 hours

---

### #5: CV 删除无权限校验

**File:** `backend/src/main/kotlin/me/hker/module/cv/service/impl/CvServiceImpl.kt`

**Description:** `delete()` 只检查 userId 匹配，未校验用户是否为 CV 拥有者（两个不同用户可能 id 相同）。

**Impact:** 🔴 用户可能删除他人 CV

**Remediation:**
```kotlin
fun delete(id: Long, userId: Long) {
    val cv = cvRepository.findById(id)
        ?: throw CvNotFoundException(id)

    if (cv.userId != userId) {
        throw AccessDeniedException("You don't own this CV")
    }

    cvRepository.delete(cv)
}
```

**Estimated Effort:** 1 hour

---

## HIGH (应尽快修复)

### #6: Slug 没有唯一性检查

**File:** `backend/src/main/kotlin/me/hker/module/cv/service/impl/CvServiceImpl.kt`

**Description:** 公开 CV 设置 slug 时没有检查是否已被其他用户使用。

**Impact:** 🟠 用户可以覆盖他人公开链接

**Remediation:**
```kotlin
fun updateSlug(cvId: Long, slug: String, userId: Long) {
    // 检查 slug 是否被其他 CV 占用
    val existing = cvRepository.findBySlug(slug)
    if (existing.isPresent && existing.get().id != cvId) {
        throw SlugAlreadyTakenException(slug)
    }
    // ...
}
```

**Estimated Effort:** 2 hours

---

### #7: Export token 无实际验证

**File:** `backend/src/main/kotlin/me/hker/module/export/PdfExportController.kt`

**Description:** 用 X-User-Id header 直接传 userId，任何客户端可伪造。

**Impact:** 🟠 可绕过 export 限制

**Remediation:**
- 移除 X-User-Id header
- 使用 JWT token 验证身份
- 实现 export token 机制（短期有效的签名 token）

**Estimated Effort:** 4 hours

---

### #8: Redeem 无幂等保护

**File:** `backend/src/main/kotlin/me/hker/module/reward/service/impl/RewardServiceImpl.kt`

**Description:** 同一 code 快速连续兑换可能绕过 max_redemptions 检查。

**Impact:** 🟠 奖励码可被多次兑换

**Remediation:**
```kotlin
@Transactional(isolation = Isolation.SERIALIZABLE)
fun redeem(code: String, userId: Long) {
    // 使用数据库行级锁 + 唯一约束
    val redemption = RedemptionLock(userId, code)
    redemptionLockRepository.save(redemption)
    // ...
}
```

**Estimated Effort:** 3 hours

---

### #9: JSON.parse 无 try-catch

**File:** `frontend/src/views/editor/CvEditorView.vue`

**Description:** 用户在 textarea 输入非法 JSON 会导致页面崩溃。

**Impact:** 🟠 用户体验差，可能丢失数据

**Remediation:**
```javascript
try {
  const data = JSON.parse(rawInput.value)
  // 处理数据
} catch (e) {
  errorMessage.value = 'Invalid JSON: ' + e.message
  return
}
```

**Estimated Effort:** 1 hour

---

### #10: Mobile menu 按钮无功能

**File:** `frontend/src/App.vue`

**Description:** `@click="() => {}"` 空操作，没有实现移动端菜单。

**Impact:** 🟠 移动端导航不可用

**Estimated Effort:** 4 hours

---

## MEDIUM (建议修复)

### #11: Axios 缺少 response interceptor

**File:** `frontend/src/client.js`

**Description:** 401/402 等业务错误码没有统一处理。

**Impact:** 🟡 代码重复，错误处理不一致

**Remediation:**
```javascript
axios.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401) {
      await authStore.logout()
      router.push('/login')
    }
    if (error.response?.status === 402) {
      // 显示 credit 不足提示
    }
    return Promise.reject(error)
  }
)
```

**Estimated Effort:** 2 hours

---

### #12: API 函数返回类型不一致

**File:** `frontend/src/stores/cv.js`

**Description:** 不同 API 函数返回不同数据结构。

**Impact:** 🟡 容易出错，维护困难

**Estimated Effort:** 3 hours

---

### #13: 删除 CV 缺少确认对话框

**File:** `frontend/src/stores/cv.js`

**Impact:** 🟡 误删风险

**Estimated Effort:** 1 hour

---

### #14: 异常处理不完整

**File:** `backend/src/main/kotlin/me/hker/common/GlobalExceptionHandler.kt`

**Description:** 缺少 Spring 常见异常处理。

**Impact:** 🟡 错误信息不友好

**Estimated Effort:** 2 hours

---

### #15: Locale 解析过于简单

**File:** `backend/src/main/kotlin/me/hker/common/I18nMessageHelper.kt`

**Description:** 只取 header 第一段，未处理复杂 Accept-Language。

**Impact:** 🟡 语言检测不准确

**Estimated Effort:** 2 hours

---

### #16: Bootstrap 失败时不清除 locale

**File:** `frontend/src/stores/auth.js`

**Impact:** 🟡 可能残留旧设置

**Estimated Effort:** 1 hour

---

### #17: CV store 缺少 reset action

**File:** `frontend/src/stores/cv.js`

**Impact:** 🟡 状态污染

**Estimated Effort:** 1 hour

---

### #18: Redeem 后没有刷新 CV 列表

**File:** `frontend/src/composables/useRewardCenter.js`

**Impact:** 🟡 UI 不同步

**Estimated Effort:** 1 hour

---

### #19: Magic numbers

**File:** `backend/src/main/kotlin/me/hker/config/AppBusinessProperties.kt`

**Impact:** 🟡 可维护性差

**Estimated Effort:** 1 hour

---

### #20: 空 catch 吞掉 JSON 解析错误

**File:** `backend/src/main/kotlin/me/hker/module/template/service/impl/TemplateServiceImpl.kt`

**Impact:** 🟡 调试困难

**Estimated Effort:** 1 hour

---

## LOW (可选修复)

### #21-30: 代码质量问题

包括硬编码、测试不一致、缺失功能等。

详见完整列表...

---

## Recovery Plan

### Phase 1: Security & Data Integrity (Week 1)

| Issue | Effort | Priority |
|-------|--------|----------|
| #1 JWT Security | 4h | P0 |
| #2 Mock Auth | 16h | P0 |
| #3 Credit Transaction | 2h | P0 |
| #4 Export Refund | 2h | P0 |
| #5 CV Authorization | 1h | P0 |

**Total:** 25 hours (~3-4 days)

**Success Criteria:**
- [ ] JWT 使用正确的签名算法
- [ ] 用户认证完全实现
- [ ] Credit 操作具备事务保护
- [ ] 所有资源操作有权限校验

---

### Phase 2: Business Logic (Week 2)

| Issue | Effort | Priority | Status |
|-------|--------|----------|--------|
| #6 Slug Uniqueness | 2h | P1 | ✅ Fixed |
| #7 Export Token | 4h | P1 | ✅ Fixed |
| #8 Idempotency | 3h | P1 | ✅ Fixed |
| #9 JSON Error Handling | 1h | P2 | ✅ Fixed |
| #10 Mobile Menu | 4h | P2 | ✅ Fixed |

**Total:** 14 hours (~2 days)

**Completed:**
- #6: `ensureSlugAvailable()` added to `CvServiceImpl` (lines 202-213)
- #7: Removed `X-User-Id` header from `PdfExportController`, now uses `CurrentUserResolver`
- #8: Added `FOR UPDATE` pessimistic locks in `PromoCodeMapper` and `UserMapper`, `REPEATABLE_READ` isolation
- #9: `try-catch` added in `updateSectionContentFromJson()` (lines 126-132)
- #10: Mobile menu toggle implemented with hamburger/close icons and auto-close on navigation

---

### Phase 3: Error Handling & UX (Week 3)

| Issue | Effort | Priority | Status |
|-------|--------|----------|--------|
| #11 Response Interceptor | 2h | P2 | ✅ Fixed |
| #13 Delete Confirmation | 1h | P2 | ✅ Fixed |
| #14 Exception Handler | 2h | P2 | ✅ Fixed |
| #16-18 State Management | 3h | P3 | ✅ Fixed |

**Total:** 8 hours (~1 day)

**Completed:**
- #11: Added axios response interceptor in `client.js` - handles 401 (redirect to login), 402 (emit custom event)
- #13: Added delete confirmation dialog in `DashboardView.vue` with cancel/confirm buttons
- #14: Added handlers for `AccessDeniedException`, `MethodArgumentTypeMismatchException`, `HttpMessageNotReadableException`
- #16: Fixed locale persistence - `bootstrap()` now correctly persists server locale to localStorage
- #17: Added `reset()` action and `isLoaded` getter to `cv.js` store
- #18: `loadMyCvs()` is now called after successful redemption in `useRewardCenter()`

---

### Phase 4: Code Quality (Week 4)

Medium #12, #15, #17, #18, #19, #20 + Low priority items

**Total:** ~10 hours

---

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| JWT 伪造被利用 | High | Critical | 立即修复 #1 |
| Credit 数据不一致 | Medium | High | 立即修复 #3, #4 |
| 用户数据泄露 | Medium | Critical | 立即修复 #2, #5 |

---

## Notes

- 所有 CRITICAL 问题应在进入生产前解决
- MEDIUM 问题可在迭代中逐步解决
- LOW 问题可作为 onboarding 任务分配给新成员
