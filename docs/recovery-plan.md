# Technical Debt Recovery Plan

> Project: Project CV
> Created: 2026-03-28
> Target Completion: 4 weeks

---

## Overview

This document provides a structured approach to resolving 30 identified technical debt items, organized into 4 phases based on severity and dependencies.

---

## Phase 1: Security & Data Integrity (Week 1)

**Goal:** Eliminate all CRITICAL security vulnerabilities and data integrity risks.

### 1.1 JWT Security (#1) - 4 hours

**Current State:**
```kotlin
// Base64 encoding, not a real signed JWT
fun generateToken(userId: Long, email: String): String {
    val payload = "$userId:$email:$secret"
    return Base64.getEncoder().encodeToString(payload.toByteArray())
}
```

**Target State:**
```kotlin
// Add to build.gradle.kts
implementation("io.jsonwebtoken:jjwt-api:0.12.3")
implementation("io.jsonwebtoken:jjwt-impl:0.12.3")
runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

// JwtUtil.kt
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtUtil(
    private val properties: JwtProperties
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(properties.secret.toByteArray())

    fun generateToken(userId: Long, email: String): String {
        val now = Date()
        val expiry = Date(now.time + properties.expirationMs)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key, Jwts.SIG.HS256)
            .compact()
    }

    fun validateToken(token: String): JwtPayload? {
        return try {
            val claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload

            JwtPayload(
                userId = claims.subject.toLong(),
                email = claims.get("email", String::class.java)
            )
        } catch (e: JwtException) {
            null
        }
    }
}

data class JwtPayload(val userId: Long, val email: String)
```

**Acceptance Criteria:**
- [ ] Token uses HS256 signing
- [ ] Invalid tokens return null
- [ ] Expired tokens are rejected
- [ ] Unit tests cover success and failure cases

---

### 1.2 Real Authentication (#2) - 16 hours

**Tasks:**

1. **Create User Entity** (2h)
```kotlin
// backend/src/main/kotlin/me/hker/module/auth/entity/User.kt
@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    val passwordHash: String,

    @Column(nullable = false)
    val locale: String = "zh-TW",

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column
    val localeOverrides: String? = null // JSON string
)
```

2. **Create UserRepository** (1h)
3. **Implement Password Hashing** (2h)
```kotlin
// Add to build.gradle.kts
implementation("at.favre.lib:bcrypt:0.10.2")

@Component
class PasswordEncoder {
    fun encode(rawPassword: String): String =
        BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray())

    fun verify(rawPassword: String, hash: String): Boolean =
        BCrypt.verifyer().verify(rawPassword.toCharArray(), hash).verified
}
```

4. **Implement Register/Login** (4h)
5. **Persist Locale** (1h)
6. **Tests** (6h)

**Acceptance Criteria:**
- [ ] Users are persisted to database
- [ ] Passwords are hashed with BCrypt
- [ ] Login validates password correctly
- [ ] Locale preferences persist
- [ ] Integration tests pass

---

### 1.3 Credit Transaction Wrappers (#3, #4) - 4 hours

**Refactor CvServiceImpl:**

```kotlin
@Service
@Transactional
class CvServiceImpl(
    private val cvRepository: CvRepository,
    private val creditService: CreditService,
    private val properties: AppBusinessProperties
) : CvService {

    override fun create(request: CreateCvRequest, userId: Long): CvDto {
        // Check credits first (don't deduct yet)
        val balance = creditService.getBalance(userId)
        if (balance < properties.createCvCost) {
            throw InsufficientCreditsException(
                required = properties.createCvCost,
                current = balance
            )
        }

        // Create CV first
        val cv = Cv(
            userId = userId,
            templateId = request.templateId,
            metadata = request.metadata
        )
        val saved = cvRepository.save(cv)

        // Deduct after successful DB write
        creditService.deductCredits(
            userId = userId,
            amount = properties.createCvCost,
            reason = "create_cv",
            resourceId = saved.id
        )

        return saved.toDto()
    }

    override fun updateMetadata(id: Long, request: UpdateMetadataRequest, userId: Long): CvDto {
        val cv = cvRepository.findById(id)
            .orElseThrow { CvNotFoundException(id) }

        if (cv.userId != userId) {
            throw AccessDeniedException("You don't own this CV")
        }

        // Check if slug change requires payment
        val cost = if (request.slug != null && request.slug != cv.slug) {
            properties.updateSlugCost
        } else {
            0
        }

        if (cost > 0) {
            val balance = creditService.getBalance(userId)
            if (balance < cost) {
                throw InsufficientCreditsException(cost, balance)
            }
        }

        // Update CV
        cv.apply {
            metadata = request.metadata
            request.slug?.let { slug = it }
        }
        val saved = cvRepository.save(cv)

        // Deduct after successful update
        if (cost > 0) {
            creditService.deductCredits(userId, cost, "update_slug", id)
        }

        return saved.toDto()
    }
}
```

**Refactor PdfExportServiceImpl:**

```kotlin
@Service
@Transactional
class PdfExportServiceImpl(
    private val renderer: PdfRenderer,
    private val creditService: CreditService,
    private val properties: AppBusinessProperties
) : PdfExportService {

    override fun exportPdf(cvId: Long, userId: Long): ByteArray {
        // Check credits
        val balance = creditService.getBalance(userId)
        if (balance < properties.pdfExportCost) {
            throw InsufficientCreditsException(
                required = properties.pdfExportCost,
                current = balance
            )
        }

        // Generate PDF first
        val pdf = renderer.generate(cvId)

        // Deduct only after successful generation
        creditService.deductCredits(
            userId = userId,
            amount = properties.pdfExportCost,
            reason = "pdf_export",
            resourceId = cvId
        )

        return pdf
    }
}
```

**Acceptance Criteria:**
- [ ] All credit operations are within @Transactional
- [ ] Failed DB writes don't deduct credits
- [ ] Failed PDF generations don't deduct credits
- [ ] Integration tests verify rollback behavior

---

### 1.5 CV Authorization (#5) - 1 hour

```kotlin
override fun delete(id: Long, userId: Long) {
    val cv = cvRepository.findById(id)
        .orElseThrow { CvNotFoundException(id) }

    // Explicit ownership check
    if (cv.userId != userId) {
        throw AccessDeniedException(
            "User $userId does not own CV $id (owned by ${cv.userId})"
        )
    }

    cvRepository.delete(cv)
}

override fun getById(id: Long, userId: Long): CvDto {
    val cv = cvRepository.findById(id)
        .orElseThrow { CvNotFoundException(id) }

    if (cv.userId != userId) {
        throw AccessDeniedException("Access denied")
    }

    return cv.toDto()
}
```

**Acceptance Criteria:**
- [ ] All CV operations check ownership
- [ ] Tests verify cross-user access is denied

---

## Phase 2: Business Logic (Week 2)

### 2.1 Slug Uniqueness (#6) - 2 hours

```kotlin
// Add unique constraint to Cv entity
@Entity
@Table(name = "cvs", uniqueConstraints = [
    UniqueConstraint(columnNames = ["slug"])
])
class Cv(
    // ...
    @Column(unique = true)
    var slug: String? = null
)

// Repository
interface CvRepository : JpaRepository<Cv, Long> {
    fun findBySlug(slug: String): Optional<Cv>
}

// Service
override fun updateSlug(id: Long, slug: String, userId: Long): CvDto {
    // Validate slug format
    if (!slug.matches(Regex("^[a-z0-9-]+$"))) {
        throw InvalidSlugException("Slug must contain only lowercase letters, numbers, and hyphens")
    }

    // Check uniqueness
    val existing = cvRepository.findBySlug(slug)
    if (existing.isPresent && existing.get().id != id) {
        throw SlugAlreadyTakenException(slug)
    }

    // ... rest of update logic
}
```

---

### 2.2 Export Token Security (#7) - 4 hours

```kotlin
// Remove X-User-Id header approach
// Implement short-lived export tokens

@Component
class ExportTokenUtil(
    private val jwtUtil: JwtUtil
) {
    private val EXPORT_TOKEN_EXPIRY = 5 * 60 * 1000 // 5 minutes

    fun generateExportToken(cvId: Long, userId: Long): String {
        return jwtUtil.generateToken(userId, "", mapOf(
            "cvId" to cvId,
            "purpose" to "export",
            "exp" to (System.currentTimeMillis() + EXPORT_TOKEN_EXPIRY)
        ))
    }

    fun validateExportToken(token: String, cvId: Long): Boolean {
        val payload = jwtUtil.validateToken(token) ?: return false
        val tokenCvId = payload.claims["cvId"]?.asLong() ?: return false
        val purpose = payload.claims["purpose"]?.asString ?: return false

        return tokenCvId == cvId && purpose == "export"
    }
}

// Update controller
@PostMapping("/{id}/export")
fun exportPdf(
    @PathVariable id: Long,
    @RequestHeader("X-Export-Token") exportToken: String
): ResponseEntity<ByteArray> {
    if (!exportTokenUtil.validateExportToken(exportToken, id)) {
        return ResponseEntity.status(401).build()
    }
    // ...
}
```

---

### 2.3 Redeem Idempotency (#8) - 3 hours

```kotlin
// Add redemption tracking table
@Entity
@Table(name = "redemptions", uniqueConstraints = [
    UniqueConstraint(columnNames = ["user_id", "reward_code_id"])
])
class Redemption(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id")
    val userId: Long,

    @Column(name = "reward_code_id")
    val rewardCodeId: Long,

    @Column(name = "redeemed_at")
    val redeemedAt: LocalDateTime = LocalDateTime.now()
)

interface RedemptionRepository : JpaRepository<Redemption, Long> {
    fun existsByUserIdAndRewardCodeId(userId: Long, rewardCodeId: Long): Boolean
}

@Service
@Transactional(isolation = Isolation.SERIALIZABLE)
class RewardServiceImpl(
    private val redemptionRepository: RedemptionRepository,
    private val rewardCodeRepository: RewardCodeRepository,
    private val creditService: CreditService
) : RewardService {

    override fun redeem(code: String, userId: Long): RedeemResponse {
        val rewardCode = rewardCodeRepository.findByCode(code)
            .orElseThrow { RewardCodeNotFoundException(code) }

        // Check if already redeemed
        if (redemptionRepository.existsByUserIdAndRewardCodeId(userId, rewardCode.id!!)) {
            throw AlreadyRedeemedException(code)
        }

        // Check max redemptions
        val currentRedemptions = redemptionRepository.countByRewardCodeId(rewardCode.id!!)
        if (currentRedemptions >= rewardCode.maxRedemptions) {
            throw MaxRedemptionsReachedException(code)
        }

        // Record redemption
        redemptionRepository.save(Redemption(
            userId = userId,
            rewardCodeId = rewardCode.id!!
        ))

        // Award credits
        creditService.addCredits(
            userId = userId,
            amount = rewardCode.rewardAmount,
            reason = "reward_redeem",
            resourceId = rewardCode.id
        )

        return RedeemResponse(
            amount = rewardCode.rewardAmount,
            newBalance = creditService.getBalance(userId)
        )
    }
}
```

---

### 2.4 JSON Error Handling (#9) - 1 hour

```vue
<!-- CvEditorView.vue -->
<script setup>
import { ref, computed } from 'vue'

const rawInput = ref('{}')
const parseError = ref(null)
const cvData = ref(null)

const parsedCvData = computed(() => {
  try {
    const data = JSON.parse(rawInput.value)
    parseError.value = null
    return data
  } catch (e) {
    parseError.value = {
      message: e.message,
      line: e.message.match(/position (\d+)/)?.[1]
    }
    return null
  }
})

function handleSave() {
  if (parseError.value) {
    showErrorNotification('Please fix JSON errors before saving')
    return
  }
  // ... save logic
}
</script>

<template>
  <div class="cv-editor">
    <div v-if="parseError" class="error-banner">
      <strong>JSON Error:</strong> {{ parseError.message }}
    </div>
    <textarea v-model="rawInput" />
  </div>
</template>
```

---

### 2.5 Mobile Menu (#10) - 4 hours

```vue
<!-- App.vue -->
<script setup>
import { ref } from 'vue'

const mobileMenuOpen = ref(false)

function toggleMobileMenu() {
  mobileMenuOpen.value = !mobileMenuOpen.value
}

function closeMobileMenu() {
  mobileMenuOpen.value = false
}
</script>

<template>
  <nav class="navbar">
    <div class="nav-brand">Project CV</div>

    <!-- Desktop menu -->
    <div class="nav-links desktop-only">
      <router-link to="/dashboard">Dashboard</router-link>
      <!-- ... -->
    </div>

    <!-- Mobile menu button -->
    <button
      class="mobile-menu-button"
      @click="toggleMobileMenu"
      aria-label="Toggle menu"
    >
      <span :class="{ open: mobileMenuOpen }"></span>
      <span :class="{ open: mobileMenuOpen }"></span>
      <span :class="{ open: mobileMenuOpen }"></span>
    </button>

    <!-- Mobile menu dropdown -->
    <div class="mobile-menu" :class="{ open: mobileMenuOpen }">
      <router-link to="/dashboard" @click="closeMobileMenu">Dashboard</router-link>
      <!-- ... -->
    </div>
  </nav>
</template>

<style>
.mobile-menu-button {
  display: none;
}

@media (max-width: 768px) {
  .nav-links { display: none; }
  .mobile-menu-button { display: block; }

  .mobile-menu {
    display: none;
    flex-direction: column;
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background: white;
    box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  }

  .mobile-menu.open { display: flex; }
}
</style>
```

---

## Phase 3: Error Handling & UX (Week 3)

### 3.1 Axios Response Interceptor (#11) - 2 hours

```javascript
// client.js
import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  timeout: 30000
})

// Request interceptor - add auth token
api.interceptors.request.use(
  config => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// Response interceptor - handle errors
api.interceptors.response.use(
  response => response,
  async error => {
    const authStore = useAuthStore()

    if (error.response) {
      const { status, data } = error.response

      switch (status) {
        case 401:
          // Unauthorized - clear auth and redirect to login
          await authStore.logout()
          router.push('/login')
          break

        case 402:
          // Payment Required - insufficient credits
          authStore.showCreditWarning(data.message || 'Insufficient credits')
          break

        case 403:
          // Forbidden - show access denied
          console.error('Access denied:', data.message)
          break

        case 404:
          // Not Found - show friendly message
          console.error('Resource not found')
          break

        case 422:
          // Validation Error - show form errors
          return Promise.reject(data)

        case 500:
          // Server Error - show generic error
          console.error('Server error, please try again later')
          break
      }
    } else if (error.request) {
      // Network error
      console.error('Network error, please check your connection')
    }

    return Promise.reject(error)
  }
)

export default api
```

---

### 3.2 Delete Confirmation (#13) - 1 hour

```javascript
// stores/cv.js
async function deleteCv(id) {
  const cv = this.cvs.find(c => c.id === id)

  if (!confirm(
    this.t('cv.deleteConfirm', {
      name: cv?.title || this.t('cv.untitled')
    })
  )) {
    return
  }

  try {
    await api.delete(`/cvs/${id}`)
    this.cvs = this.cvs.filter(c => c.id !== id)
  } catch (error) {
    console.error('Failed to delete CV:', error)
    throw error
  }
}
```

---

### 3.3 Global Exception Handler (#14) - 2 hours

```kotlin
// GlobalExceptionHandler.kt
@RestControllerAdvice
@Order(1)
class GlobalExceptionHandler(
    private val messageHelper: I18nMessageHelper
) {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val locale = messageHelper.resolveLocale(request)
        val errors = ex.bindingResult.fieldErrors.associate {
            it.field to (messageHelper.getMessage(it.defaultMessage!!, locale)
                ?: it.defaultMessage!!)
        }
        return ResponseEntity.status(422).body(
            ErrorResponse(
                error = "validation_error",
                message = messageHelper.getMessage("validation.failed", locale),
                details = errors
            )
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleJsonParseException(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val locale = messageHelper.resolveLocale(request)
        return ResponseEntity.status(400).body(
            ErrorResponse(
                error = "invalid_json",
                message = messageHelper.getMessage("json.invalid", locale)
            )
        )
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(
        ex: AccessDeniedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val locale = messageHelper.resolveLocale(request)
        return ResponseEntity.status(403).body(
            ErrorResponse(
                error = "access_denied",
                message = messageHelper.getMessage("access.denied", locale)
            )
        )
    }
}

data class ErrorResponse(
    val error: String,
    val message: String,
    val details: Map<String, String>? = null
)
```

---

## Phase 4: Code Quality (Week 4)

### 4.1 API Response Consistency (#12) - 3 hours

Define standard response wrapper:

```kotlin
// backend/src/main/kotlin/me/hker/common/ApiResponse.kt
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: String? = null,
    val metadata: PaginationMetadata? = null
) {
    companion object {
        fun <T> ok(data: T): ApiResponse<T> = ApiResponse(true, data, null)
        fun <T> error(message: String): ApiResponse<T> = ApiResponse(false, null, message)

        fun <T> paginated(
            data: List<T>,
            page: Int,
            size: Int,
            total: Long
        ): ApiResponse<List<T>> = ApiResponse(
            success = true,
            data = data,
            metadata = PaginationMetadata(page, size, total)
        )
    }
}

data class PaginationMetadata(
    val page: Int,
    val size: Int,
    val total: Long,
    val totalPages: Int = (total / size).toInt() + if (total % size > 0) 1 else 0
)
```

---

### 4.2 Store Reset Actions (#17) - 1 hour

```javascript
// stores/cv.js
export const useCvStore = defineStore('cv', {
  state: () => ({
    currentCv: null,
    sections: {},
    // ...
  }),

  actions: {
    reset() {
      this.currentCv = null
      this.sections = {}
      this.lastSaved = null
    },

    async loadCv(id) {
      this.reset() // Clear previous state
      const cv = await api.get(`/cvs/${id}`)
      this.currentCv = cv.data
    }
  }
})
```

---

## Testing Strategy

### Unit Tests Required

1. **JWT Security** (#1)
   - Valid token generation
   - Token validation
   - Expired token rejection
   - Invalid token rejection

2. **Authentication** (#2)
   - Password hashing and verification
   - User registration
   - Login with correct password
   - Login with incorrect password

3. **Credit Operations** (#3, #4)
   - Successful credit deduction
   - Rollback on DB failure
   - Rollback on PDF generation failure

4. **Authorization** (#5)
   - Owner can access CV
   - Non-owner is denied

---

## Deployment Checklist

Before deploying to production:

### Phase 1 (Required for Production)
- [ ] JWT uses proper signing
- [ ] User authentication fully implemented
- [ ] Credit transactions are atomic
- [ ] All resources have authorization checks
- [ ] Database migrations reviewed
- [ ] Security audit completed

### Phase 2 (Required for Production)
- [ ] Slug uniqueness enforced
- [ ] Export tokens validated
- [ ] Reward redemption is idempotent
- [ ] JSON errors don't crash frontend

### Phase 3 & 4 (Can Iterate)
- [ ] Response interceptor active
- [ ] Delete confirmations in place
- [ ] Global exceptions handled
- [ ] API responses consistent

---

## Risk Mitigation

| Risk | Mitigation |
|------|------------|
| Breaking existing users | Test auth migration in staging first |
| Credit data corruption | Backup database before Phase 1 deployment |
| Frontend crashes | Add JSON validation before business logic |
| Performance regression | Load test before and after changes |

---

## Next Steps

1. **Immediate**: Start Phase 1, Issue #1 (JWT)
2. **This Week**: Complete Phase 1
3. **Next Week**: Begin Phase 2
4. **Track Progress**: Update this doc weekly
