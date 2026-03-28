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
  displayName: "",
});
const error = ref("");
const loading = ref(false);

const handleSubmit = async (event) => {
  event.preventDefault();
  error.value = "";
  loading.value = true;

  try {
    await auth.register(form.value);
    router.push("/onboarding/step1");
  } catch (requestError) {
    error.value =
      requestError?.response?.data?.message || requestError?.message || t("auth.registerError");
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <section
    data-testid="view-register"
    class="relative isolate min-h-[calc(100vh-5rem)] overflow-hidden bg-background px-4 py-6 text-on-background sm:px-6 lg:px-8 lg:py-8"
  >
    <div aria-hidden="true" class="absolute inset-0 -z-10 overflow-hidden">
      <div class="absolute left-[-4rem] top-16 h-96 w-96 rounded-full bg-tertiary-fixed/40 blur-3xl"></div>
      <div class="absolute right-0 top-0 h-80 w-80 rounded-full bg-primary-fixed/55 blur-3xl"></div>
      <div class="absolute bottom-0 right-16 h-72 w-72 rounded-full bg-secondary-container/35 blur-3xl"></div>
    </div>

    <div class="mx-auto grid w-full max-w-7xl gap-6 lg:min-h-[calc(100vh-10rem)] lg:grid-cols-[0.95fr_1.05fr]">
      <div class="order-2 flex items-center lg:order-1">
        <div class="w-full rounded-[36px] border border-outline-variant/40 bg-white/88 p-6 shadow-[0_24px_48px_rgba(15,23,42,0.06)] backdrop-blur sm:p-8 lg:p-10">
          <form @submit="handleSubmit" class="space-y-6">
            <div class="space-y-3">
              <span class="inline-flex w-fit items-center rounded-full bg-primary-fixed px-3 py-1 text-xs font-bold uppercase tracking-[0.22em] text-on-primary-fixed">
                Create workspace
              </span>
              <div class="space-y-2">
                <h2 class="font-headline text-3xl font-extrabold tracking-tight text-on-surface sm:text-4xl">
                  {{ t("auth.registerTitle") }}
                </h2>
                <p class="max-w-md text-sm leading-7 text-on-surface-variant">
                  Create the account that will carry your onboarding flow, editor drafts, and public resume routes.
                </p>
              </div>
            </div>

            <div class="grid gap-4 md:grid-cols-3">
              <div class="rounded-[24px] border border-outline-variant/30 bg-surface-container-low px-4 py-4">
                <p class="text-[11px] font-bold uppercase tracking-[0.2em] text-on-surface-variant">Profile</p>
                <p class="mt-2 text-sm font-medium text-on-surface">One identity across every resume</p>
              </div>
              <div class="rounded-[24px] border border-outline-variant/30 bg-surface-container-low px-4 py-4">
                <p class="text-[11px] font-bold uppercase tracking-[0.2em] text-on-surface-variant">Templates</p>
                <p class="mt-2 text-sm font-medium text-on-surface">Start with curated editorial layouts</p>
              </div>
              <div class="rounded-[24px] border border-outline-variant/30 bg-surface-container-low px-4 py-4">
                <p class="text-[11px] font-bold uppercase tracking-[0.2em] text-on-surface-variant">Flow</p>
                <p class="mt-2 text-sm font-medium text-on-surface">Onboard, edit, publish, export</p>
              </div>
            </div>

            <label class="block space-y-2">
              <span class="text-xs font-bold uppercase tracking-[0.22em] text-on-surface-variant">{{ t("auth.displayName") }}</span>
              <input
                v-model="form.displayName"
                data-testid="register-display-name"
                type="text"
                required
                :placeholder="t('auth.displayName')"
                class="h-14 w-full rounded-[24px] border border-outline-variant/40 bg-surface-container-low px-4 text-on-surface placeholder:text-outline transition focus:border-primary focus:bg-white focus:outline-none focus:ring-4 focus:ring-primary/10"
              />
            </label>

            <label class="block space-y-2">
              <span class="text-xs font-bold uppercase tracking-[0.22em] text-on-surface-variant">{{ t("auth.email") }}</span>
              <input
                v-model="form.email"
                data-testid="register-email"
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
                data-testid="register-password"
                type="password"
                required
                :placeholder="t('auth.password')"
                class="h-14 w-full rounded-[24px] border border-outline-variant/40 bg-surface-container-low px-4 text-on-surface placeholder:text-outline transition focus:border-primary focus:bg-white focus:outline-none focus:ring-4 focus:ring-primary/10"
              />
            </label>

            <p v-if="error" data-testid="register-error" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
              {{ error }}
            </p>

            <button
              type="submit"
              data-testid="register-submit"
              :disabled="loading"
              class="inline-flex h-14 w-full items-center justify-center rounded-[20px] bg-gradient-to-br from-primary to-primary-container px-5 text-sm font-semibold text-white shadow-lg transition hover:scale-[1.01] hover:shadow-xl disabled:cursor-not-allowed disabled:opacity-50"
            >
              {{ loading ? t("common.loading") : t("auth.register") }}
            </button>

            <p class="text-center text-sm text-on-surface-variant">
              {{ t("auth.alreadyHaveAccount") }}
              <RouterLink to="/login" class="font-semibold text-primary transition hover:opacity-80">
                {{ t("auth.login") }}
              </RouterLink>
            </p>
          </form>
        </div>
      </div>

      <aside class="order-1 hidden flex-col justify-between rounded-[36px] border border-outline-variant/40 bg-surface-container-low px-8 py-8 shadow-[0_24px_48px_rgba(15,23,42,0.06)] xl:flex">
        <div class="space-y-6">
          <span class="inline-flex w-fit items-center rounded-full bg-tertiary-fixed px-3 py-1 text-xs font-bold uppercase tracking-[0.22em] text-on-tertiary-fixed-variant">
            Create workspace
          </span>
          <div class="space-y-4">
            <h1 class="max-w-xl font-headline text-4xl font-extrabold tracking-tight text-on-surface">
              Start your resume system once.
            </h1>
            <p class="max-w-lg text-sm leading-7 text-on-surface-variant">
              Create a single account, move through onboarding, and keep every draft, public route, and export connected to the same workspace.
            </p>
          </div>
        </div>

        <div class="space-y-4">
          <div class="rounded-[28px] border border-outline-variant/30 bg-surface-container-lowest p-5">
            <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-on-surface-variant">What you unlock</p>
            <p class="mt-3 text-2xl font-extrabold tracking-tight text-on-surface">Editorial onboarding + live editor</p>
            <p class="mt-2 text-sm leading-7 text-on-surface-variant">
              Account creation immediately routes into onboarding so your first resume starts with structure, not a blank page.
            </p>
          </div>
          <div class="grid gap-3 sm:grid-cols-3">
            <div class="rounded-[24px] border border-outline-variant/30 bg-white/70 p-4">
              <p class="text-[11px] font-bold uppercase tracking-[0.2em] text-on-surface-variant">Identity</p>
              <p class="mt-2 text-sm font-medium text-on-surface">Public profile and username setup</p>
            </div>
            <div class="rounded-[24px] border border-outline-variant/30 bg-white/70 p-4">
              <p class="text-[11px] font-bold uppercase tracking-[0.2em] text-on-surface-variant">Direction</p>
              <p class="mt-2 text-sm font-medium text-on-surface">Professional context and template choice</p>
            </div>
            <div class="rounded-[24px] border border-outline-variant/30 bg-white/70 p-4">
              <p class="text-[11px] font-bold uppercase tracking-[0.2em] text-on-surface-variant">Output</p>
              <p class="mt-2 text-sm font-medium text-on-surface">Publish routes and PDF export</p>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </section>
</template>
