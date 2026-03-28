<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { useAuthStore } from "@/stores/auth";

const { t } = useI18n();
const router = useRouter();
const auth = useAuthStore();

const form = ref({
  email: "",
  password: "",
});
const error = ref("");
const loading = ref(false);

const handleSubmit = async (event) => {
  event.preventDefault();
  error.value = "";
  loading.value = true;

  try {
    await auth.login(form.value);
    router.push("/dashboard");
  } catch (requestError) {
    error.value = requestError?.response?.data?.message || requestError?.message || t("auth.loginError");
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <section
    data-testid="view-login"
    class="relative isolate min-h-[calc(100vh-5rem)] overflow-hidden bg-background px-4 py-6 text-on-background sm:px-6 lg:px-8 lg:py-8"
  >
    <div aria-hidden="true" class="absolute inset-0 -z-10 overflow-hidden">
      <div class="absolute left-[12%] top-0 h-80 w-80 rounded-full bg-primary-fixed/65 blur-3xl"></div>
      <div class="absolute right-[4%] top-24 h-96 w-96 rounded-full bg-tertiary-fixed/45 blur-3xl"></div>
      <div class="absolute bottom-0 left-1/3 h-72 w-72 rounded-full bg-secondary-container/35 blur-3xl"></div>
    </div>

    <div class="mx-auto grid w-full max-w-7xl gap-6 lg:min-h-[calc(100vh-10rem)] lg:grid-cols-[1.02fr_0.98fr]">
      <aside class="hidden flex-col justify-between rounded-[36px] border border-outline-variant/40 bg-surface-container-low px-8 py-8 shadow-[0_24px_48px_rgba(15,23,42,0.06)] xl:flex">
        <div class="space-y-6">
          <span class="inline-flex w-fit items-center rounded-full bg-primary-fixed px-3 py-1 text-xs font-bold uppercase tracking-[0.22em] text-on-primary-fixed">
            Workspace sign in
          </span>
          <div class="space-y-4">
            <h1 class="max-w-xl font-headline text-4xl font-extrabold tracking-tight text-on-surface">
              Return to the editorial workspace.
            </h1>
            <p class="max-w-lg text-sm leading-7 text-on-surface-variant">
              Review drafts, refine templates, publish updates, and keep the same resume system moving without friction.
            </p>
          </div>
        </div>

        <div class="space-y-4">
          <div class="rounded-[28px] border border-outline-variant/30 bg-surface-container-lowest p-5">
            <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-on-surface-variant">Workflow</p>
            <p class="mt-3 text-2xl font-extrabold tracking-tight text-on-surface">Draft, preview, publish.</p>
            <p class="mt-2 text-sm leading-7 text-on-surface-variant">
              Everything stays inside one system: onboarding, editing, public routes, and export.
            </p>
          </div>
          <div class="grid gap-3 sm:grid-cols-3">
            <div class="rounded-[24px] border border-outline-variant/30 bg-white/70 p-4">
              <p class="text-[11px] font-bold uppercase tracking-[0.2em] text-on-surface-variant">Templates</p>
              <p class="mt-2 text-sm font-medium text-on-surface">Curated visual systems</p>
            </div>
            <div class="rounded-[24px] border border-outline-variant/30 bg-white/70 p-4">
              <p class="text-[11px] font-bold uppercase tracking-[0.2em] text-on-surface-variant">Preview</p>
              <p class="mt-2 text-sm font-medium text-on-surface">Live document workspace</p>
            </div>
            <div class="rounded-[24px] border border-outline-variant/30 bg-white/70 p-4">
              <p class="text-[11px] font-bold uppercase tracking-[0.2em] text-on-surface-variant">Publish</p>
              <p class="mt-2 text-sm font-medium text-on-surface">Public route and PDF export</p>
            </div>
          </div>
        </div>
      </aside>

      <div class="flex items-center">
        <div class="w-full rounded-[36px] border border-outline-variant/40 bg-white/88 p-6 shadow-[0_24px_48px_rgba(15,23,42,0.06)] backdrop-blur sm:p-8 lg:p-10">
          <form @submit="handleSubmit" class="space-y-6">
            <div class="space-y-3">
              <span class="inline-flex w-fit items-center rounded-full bg-primary-fixed px-3 py-1 text-xs font-bold uppercase tracking-[0.22em] text-on-primary-fixed">
                {{ t("auth.login") }}
              </span>
              <div class="space-y-2">
                <h2 class="font-headline text-3xl font-extrabold tracking-tight text-on-surface sm:text-4xl">
                  {{ t("auth.loginTitle") }}
                </h2>
                <p class="max-w-md text-sm leading-7 text-on-surface-variant">
                  Use the same account that owns your dashboard, editor workspace, and onboarding progress.
                </p>
              </div>
            </div>

            <div class="grid gap-4 md:grid-cols-2">
              <div class="rounded-[24px] border border-outline-variant/30 bg-surface-container-low px-4 py-4">
                <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-on-surface-variant">Account</p>
                <p class="mt-2 text-sm font-medium text-on-surface">Resume workspace owner</p>
              </div>
              <div class="rounded-[24px] border border-outline-variant/30 bg-surface-container-low px-4 py-4">
                <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-on-surface-variant">Access</p>
                <p class="mt-2 text-sm font-medium text-on-surface">Dashboard, editor, and public publishing</p>
              </div>
            </div>

            <label class="block space-y-2">
              <span class="text-xs font-bold uppercase tracking-[0.22em] text-on-surface-variant">{{ t("auth.email") }}</span>
              <input
                v-model="form.email"
                data-testid="login-email"
                type="email"
                required
                :placeholder="t('auth.email')"
                class="h-14 w-full rounded-[24px] border border-outline-variant/40 bg-surface-container-low px-4 text-on-surface placeholder:text-outline transition focus:border-primary focus:bg-white focus:outline-none focus:ring-4 focus:ring-primary/10"
              />
            </label>

            <label class="block space-y-2">
              <span class="text-xs font-bold uppercase tracking-[0.22em] text-on-surface-variant">{{ t("auth.password") }}</span>
              <input
                v-model="form.password"
                data-testid="login-password"
                type="password"
                required
                :placeholder="t('auth.password')"
                class="h-14 w-full rounded-[24px] border border-outline-variant/40 bg-surface-container-low px-4 text-on-surface placeholder:text-outline transition focus:border-primary focus:bg-white focus:outline-none focus:ring-4 focus:ring-primary/10"
              />
            </label>

            <p v-if="error" data-testid="login-error" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
              {{ error }}
            </p>

            <button
              type="submit"
              data-testid="login-submit"
              :disabled="loading"
              class="inline-flex h-14 w-full items-center justify-center rounded-[20px] bg-gradient-to-br from-primary to-primary-container px-5 text-sm font-semibold text-white shadow-lg transition hover:scale-[1.01] hover:shadow-xl disabled:cursor-not-allowed disabled:opacity-50"
            >
              {{ loading ? t("common.loading") : t("auth.login") }}
            </button>

            <p class="text-center text-sm text-on-surface-variant">
              {{ t("auth.dontHaveAccount") }}
              <RouterLink to="/register" class="font-semibold text-primary transition hover:opacity-80">
                {{ t("auth.register") }}
              </RouterLink>
            </p>
          </form>
        </div>
      </div>
    </div>
  </section>
</template>
