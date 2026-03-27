# 更新版 Dev Plan

### Tech Stack（更新）

- **Backend**: Spring Boot 3.x + Spring Security + MyBatis-Plus
- **Frontend**: Vue 3 + Vite + Pinia + Vue Router + Tailwind CSS v3
- **DB**: PostgreSQL + Redis（session cache）
- **Storage**: Local filesystem（`/uploads/` 目錄，Spring 靜態資源伺服）
- **Auth**: JWT only（email/password，無 OAuth）
- **LinkedIn 匯入**: 用戶手動貼上 LinkedIn public profile URL → 後端 scrape 解析

---

### Onboarding 流程

這是新用戶註冊後必須完成的引導流程，完成前無法進入 dashboard。由 `onboarding_status` 欄位控制，前端 router guard 攔截。---

### Database Schema（MyBatis-Plus 版本）

MyBatis-Plus 用 `@TableName` / `@TableId` 對應，所有 entity 繼承 `BaseEntity`（`created_at`, `updated_at`, logic delete flag）。---

### 專案結構

**Backend（Spring Boot + MyBatis-Plus）**

```
src/main/java/com/yourcv/
├── config/
│   ├── SecurityConfig.java         # JWT filter chain, no OAuth
│   ├── MyBatisPlusConfig.java      # pagination plugin, logic delete
│   └── StaticResourceConfig.java   # /uploads/** 本地靜態資源
├── common/
│   ├── BaseEntity.java             # created_at, updated_at, is_deleted
│   ├── R.java                      # 統一回傳格式
│   └── GlobalExceptionHandler.java
├── module/
│   ├── auth/
│   │   ├── AuthController.java     # /api/auth/register, /login, /me
│   │   ├── JwtUtil.java
│   │   └── JwtAuthFilter.java
│   ├── user/
│   │   ├── UserController.java     # /api/me/profile (update)
│   │   ├── UserService.java
│   │   ├── UserMapper.java         # MyBatis-Plus BaseMapper<User>
│   │   └── User.java               # @TableName("users")
│   ├── onboarding/
│   │   ├── OnboardingController.java  # /api/onboarding/**
│   │   ├── OnboardingService.java
│   │   ├── LinkedInScraperService.java  # Jsoup scrape + parse
│   │   └── OnboardingState.java
│   ├── cv/
│   │   ├── CvController.java       # /api/me/cvs/**
│   │   ├── PublicCvController.java # /api/u/{username}/**
│   │   ├── CvService.java
│   │   ├── CvMapper.java
│   │   ├── CvSectionMapper.java
│   │   └── Cv.java / CvSection.java
│   ├── template/
│   │   ├── TemplateController.java # /api/templates
│   │   ├── TemplateMapper.java
│   │   └── Template.java           # component_key matches Vue component name
│   └── upload/
│       ├── UploadController.java   # /api/upload (avatar, assets)
│       └── LocalStorageService.java # saves to /uploads/{userId}/
└── resources/
    ├── mapper/                     # XML mapper files
    └── db/migration/               # Flyway SQL scripts
```

**Frontend（Vue 3 + Tailwind）**

```
src/
├── main.js
├── router/
│   └── index.js          # beforeEach guard: check onboarding_status
├── stores/
│   ├── auth.js            # Pinia: user, token, isLoggedIn
│   └── cv.js
├── views/
│   ├── auth/
│   │   ├── LoginView.vue
│   │   └── RegisterView.vue
│   ├── onboarding/
│   │   ├── OnboardingLayout.vue   # step progress bar (Tailwind)
│   │   ├── Step1BasicInfo.vue
│   │   ├── Step2LinkedIn.vue      # paste URL → call API → preview parsed data
│   │   └── Step3Template.vue      # template card grid, pick one
│   ├── dashboard/
│   │   └── DashboardView.vue      # list of CVs
│   ├── editor/
│   │   └── CvEditorView.vue       # section editor + live preview
│   └── public/
│       └── PublicProfileView.vue  # /{username} — dynamic template render
├── components/
│   ├── cv-templates/
│   │   ├── templateRegistry.js    # auto-import all templates
│   │   ├── MinimalTemplate.vue
│   │   ├── SidebarTemplate.vue
│   │   └── ModernTemplate.vue     # 新增 template 只需加這裡
│   ├── sections/                  # 每種 section 的 display + edit 組件
│   └── ui/                        # Button, Input, Card (Tailwind-based)
└── assets/
```

---

### LinkedIn 匯入設計

LinkedIn 不提供公開 API，所以採用 **Jsoup 前端 URL 解析**方案：

```java
@Service
public class LinkedInScraperService {
    // 用戶貼上 public profile URL，後端用 Jsoup fetch + parse
    // LinkedIn public page 含有結構化的 JSON-LD (schema.org/Person)
    // 解析後回傳 LinkedInProfileDTO 供前端 preview + 確認匯入

    public LinkedInProfileDTO scrape(String profileUrl) {
        Document doc = Jsoup.connect(profileUrl)
            .userAgent("Mozilla/5.0 ...")
            .timeout(8000)
            .get();

        // 優先嘗試 JSON-LD script tag
        Elements jsonLd = doc.select("script[type=application/ld+json]");
        // Fallback: CSS selector 解析 .experience-section 等
        // 回傳: name, title, summary, experiences[], educations[], skills[]
    }
}
```

前端 `Step2LinkedIn.vue` 流程：用戶貼 URL → call `POST /api/onboarding/linkedin-import` → 後端 scrape 回傳預覽資料 → 用戶確認或編輯 → 寫入 `cv_sections`。

注意事項：LinkedIn 會對頻繁 scraping 封鎖 IP，建議加 retry delay + 告知用戶失敗時改手動填寫（graceful fallback）。

---

### Local Storage 設定

```java
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    @Value("${app.upload.path:/uploads}")
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
    path: ./data/uploads   # 開發環境本地路徑
    max-size: 5MB
```

Avatar 路徑儲存格式：`/uploads/{userId}/avatar.jpg`，`users.avatar_path` 只存相對路徑，前端用 `baseURL + avatar_path` 組合完整 URL。

---

### Onboarding Router Guard（Vue）

```js
// router/index.js
router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (!auth.isLoggedIn) {
    if (to.meta.requiresAuth) return '/login'
    return true
  }
  // 已登入但 onboarding 未完成
  if (auth.user.onboarding_status !== 'DONE' && !to.path.startsWith('/onboarding')) {
    return '/onboarding'
  }
  // 已完成 onboarding 不能再進入 onboarding
  if (auth.user.onboarding_status === 'DONE' && to.path.startsWith('/onboarding')) {
    return '/dashboard'
  }
})
```

`onboarding_status` 枚舉：`PENDING` → `STEP_1` → `STEP_2` → `STEP_3` → `DONE`，每完成一步後端即更新，前端刷新後 guard 允許繼續前進。

---

### Tailwind 設定重點

```js
// tailwind.config.js
export default {
  content: ['./index.html', './src/**/*.{vue,js}'],
  theme: {
    extend: {
      colors: {
        brand: { 500: '#4F46E5', 600: '#4338CA' }  // 主色調自訂
      }
    }
  }
}
```

CV template 組件使用 Tailwind 寫樣式，不同 template 只是不同的 Tailwind class 組合，不需要額外 CSS 檔案。編輯器的 live preview 用 `<component :is="...">` 即時切換，因為都是 Tailwind class，切換不會有 CSS scope 污染問題。
