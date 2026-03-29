import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import LoginView from "@/views/auth/LoginView.vue";
import RegisterView from "@/views/auth/RegisterView.vue";
import DashboardView from "@/views/dashboard/DashboardView.vue";
import CvEditorView from "@/views/editor/CvEditorView.vue";
import PrintCvView from "@/views/print/PrintCvView.vue";
import PublicProfileView from "@/views/public/PublicProfileView.vue";
import PublicCvView from "@/views/public/PublicCvView.vue";
import OnboardingLayout from "@/views/onboarding/OnboardingLayout.vue";
import Step1BasicInfo from "@/views/onboarding/Step1BasicInfo.vue";
import Step2ProfessionInfo from "@/views/onboarding/Step2ProfessionInfo.vue";
import Step3Template from "@/views/onboarding/Step3Template.vue";

// Admin routes (lazy loaded)
const AdminLayout = () => import("@/layouts/AdminLayout.vue");
const AdminDashboard = () => import("@/views/admin/AdminDashboard.vue");
const UserManagement = () => import("@/views/admin/UserManagement.vue");
const PromoCodeManagement = () => import("@/views/admin/PromoCodeManagement.vue");
const TemplateManagement = () => import("@/views/admin/TemplateManagement.vue");
const CreditTransactions = () => import("@/views/admin/CreditTransactions.vue");

export async function routeGuardDecision(to, auth) {
  if (!auth.initialized) {
    await auth.bootstrap();
  }

  if (!auth.isLoggedIn) {
    if (to.meta?.requiresAuth) return "/login";
    return true;
  }

  // Check if admin access is required
  if (to.meta?.requiresAdmin && auth.user?.role !== "ADMIN") {
    return "/dashboard";
  }

  // Redirect admins to admin dashboard if accessing user dashboard
  if (to.path === "/dashboard" && auth.user?.role === "ADMIN") {
    return "/admin/dashboard";
  }

  if (auth.user?.onboarding_status !== "DONE") {
    if (!to.path.startsWith("/onboarding")) {
      const stepMap = {
        STEP_1: "/onboarding/step1",
        STEP_2: "/onboarding/step2",
        STEP_3: "/onboarding/step3",
      };
      return stepMap[auth.user?.onboarding_status] || "/onboarding/step1";
    }

    return true;
  }

  if (to.path.startsWith("/onboarding")) {
    return "/dashboard";
  }

  return true;
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", redirect: "/dashboard" },
    { path: "/login", component: LoginView },
    { path: "/register", component: RegisterView },
    { path: "/dashboard", component: DashboardView, meta: { requiresAuth: true } },
    { path: "/editor/:id", component: CvEditorView, meta: { requiresAuth: true } },
    { path: "/print/cvs/:id", component: PrintCvView },
    { path: "/u/:email", component: PublicProfileView },
    { path: "/u/:email/:slug", component: PublicCvView },
    {
      path: "/onboarding",
      component: OnboardingLayout,
      meta: { requiresAuth: true },
      children: [
        { path: "step1", component: Step1BasicInfo },
        { path: "step2", component: Step2ProfessionInfo },
        { path: "step3", component: Step3Template },
      ],
    },
    // Admin routes
    {
      path: "/admin",
      component: AdminLayout,
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        { path: "", redirect: "/admin/dashboard" },
        { path: "dashboard", component: AdminDashboard },
        { path: "users", component: UserManagement },
        { path: "promocodes", component: PromoCodeManagement },
        { path: "templates", component: TemplateManagement },
        { path: "credits", component: CreditTransactions },
      ],
    },
  ],
});

router.beforeEach(async (to) => routeGuardDecision(to, useAuthStore()));

export default router;
