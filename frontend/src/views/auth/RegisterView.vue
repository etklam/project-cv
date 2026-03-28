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
    class="relative isolate min-h-[calc(100vh-11rem)] overflow-hidden px-4 py-8 sm:px-6 lg:px-8"
  >
    <div aria-hidden="true" class="absolute inset-0 -z-10 overflow-hidden">
      <div class="absolute left-[-6rem] top-16 h-96 w-96 rounded-full bg-amber-100/45 blur-3xl"></div>
      <div class="absolute right-0 top-0 h-80 w-80 rounded-full bg-blue-100/45 blur-3xl"></div>
      <div class="absolute bottom-0 right-16 h-72 w-72 rounded-full bg-slate-200/35 blur-3xl"></div>
    </div>

    <div class="mx-auto grid w-full max-w-7xl items-stretch gap-6 lg:min-h-[calc(100vh-12rem)] lg:grid-cols-[0.95fr_1.05fr]">
      <div class="order-2 flex items-center lg:order-1">
        <div class="w-full rounded-[32px] border border-slate-200 bg-white/90 p-6 shadow-[0_24px_48px_rgba(15,23,42,0.06)] backdrop-blur sm:p-8">
          <form @submit="handleSubmit" class="space-y-6">
            <div class="space-y-3">
              <span class="inline-flex w-fit items-center rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold uppercase tracking-[0.22em] text-blue-700">
                Create workspace
              </span>
              <div class="space-y-2">
                <h2 class="text-3xl font-semibold tracking-tight text-slate-950">
                  {{ t("auth.registerTitle") }}
                </h2>
                <p class="max-w-md text-sm leading-7 text-slate-600">
                  {{ t("auth.registerDescription") }}
                </p>
              </div>
            </div>

            <label class="block space-y-2">
              <span class="text-sm font-semibold text-slate-800">{{ t("auth.displayName") }}</span>
              <input
                v-model="form.displayName"
                type="text"
                required
                :placeholder="t('auth.displayName')"
                class="h-12 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-slate-900 placeholder:text-slate-400 transition focus:border-blue-500 focus:bg-white focus:outline-none focus:ring-4 focus:ring-blue-500/10"
              />
            </label>

            <label class="block space-y-2">
              <span class="text-sm font-semibold text-slate-800">{{ t("auth.email") }}</span>
              <input
                v-model="form.email"
                type="email"
                required
                :placeholder="t('auth.email')"
                class="h-12 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-slate-900 placeholder:text-slate-400 transition focus:border-blue-500 focus:bg-white focus:outline-none focus:ring-4 focus:ring-blue-500/10"
              />
            </label>

            <label class="block space-y-2">
              <span class="text-sm font-semibold text-slate-800">{{ t("auth.password") }}</span>
              <input
                v-model="form.password"
                type="password"
                required
                :placeholder="t('auth.password')"
                class="h-12 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-slate-900 placeholder:text-slate-400 transition focus:border-blue-500 focus:bg-white focus:outline-none focus:ring-4 focus:ring-blue-500/10"
              />
            </label>

            <p v-if="error" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
              {{ error }}
            </p>

            <button
              type="submit"
              :disabled="loading"
              class="inline-flex h-12 w-full items-center justify-center rounded-full bg-blue-600 px-5 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {{ loading ? t("common.loading") : t("auth.register") }}
            </button>

            <p class="text-center text-sm text-slate-600">
              {{ t("auth.alreadyHaveAccount") }}
              <RouterLink to="/login" class="font-semibold text-blue-700 transition hover:text-blue-800">
                {{ t("auth.login") }}
              </RouterLink>
            </p>
          </form>
        </div>
      </div>

      <aside class="order-1 hidden flex-col justify-between rounded-[32px] border border-slate-200 bg-gradient-to-br from-blue-950 via-blue-900 to-slate-950 p-8 text-white shadow-[0_24px_48px_rgba(30,64,175,0.18)] lg:flex">
        <div class="space-y-6">
          <span class="inline-flex w-fit items-center rounded-full border border-white/10 bg-white/5 px-3 py-1 text-xs font-semibold uppercase tracking-[0.22em] text-white/70">
            Create workspace
          </span>
          <div class="space-y-4">
            <h1 class="max-w-xl text-4xl font-semibold tracking-tight text-white">
              {{ t("auth.registerTitle") }}
            </h1>
            <p class="max-w-lg text-sm leading-7 text-white/72">
              {{ t("auth.registerDescription") }}
            </p>
          </div>
        </div>

        <div class="grid gap-3 sm:grid-cols-3">
          <div class="rounded-2xl border border-white/10 bg-white/5 p-4">
            <p class="text-xs font-semibold uppercase tracking-[0.2em] text-white/50">Profile</p>
            <p class="mt-2 text-sm font-medium text-white">Build once, reuse across every resume.</p>
          </div>
          <div class="rounded-2xl border border-white/10 bg-white/5 p-4">
            <p class="text-xs font-semibold uppercase tracking-[0.2em] text-white/50">Templates</p>
            <p class="mt-2 text-sm font-medium text-white">Start with curated layouts and edit fast.</p>
          </div>
          <div class="rounded-2xl border border-white/10 bg-white/5 p-4">
            <p class="text-xs font-semibold uppercase tracking-[0.2em] text-white/50">Export</p>
            <p class="mt-2 text-sm font-medium text-white">Unlock credits and PDF delivery from one place.</p>
          </div>
        </div>
      </aside>
    </div>
  </section>
</template>
