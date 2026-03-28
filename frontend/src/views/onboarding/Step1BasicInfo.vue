<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { useAuthStore } from "@/stores/auth";
import { checkUsername } from "@/api/user";

const { t } = useI18n();
const router = useRouter();
const auth = useAuthStore();

const form = ref({
  displayName: auth.user?.displayName || "",
  username: "",
});
const error = ref("");
const usernameChecking = ref(false);
const usernameAvailable = ref(null);
const loading = ref(false);

const checkUsernameAvailability = async () => {
  const username = form.value.username.trim();
  if (!username || username.length < 3) {
    usernameAvailable.value = null;
    return;
  }

  usernameChecking.value = true;
  try {
    const result = await checkUsername(username);
    usernameAvailable.value = result.available;
  } catch {
    usernameAvailable.value = false;
  } finally {
    usernameChecking.value = false;
  }
};

let debounceTimer;
const debouncedCheck = () => {
  clearTimeout(debounceTimer);
  debounceTimer = setTimeout(checkUsernameAvailability, 500);
};

const handleSubmit = async (e) => {
  e.preventDefault();
  error.value = "";

  if (!form.value.displayName.trim()) {
    error.value = t("validation.required");
    return;
  }

  if (!form.value.username.trim() || usernameAvailable.value === false) {
    error.value = t("onboarding.usernameTaken");
    return;
  }

  loading.value = true;
  try {
    await auth.setLocale(auth.locale);
    router.push("/onboarding/step2");
  } catch (err) {
    error.value = err?.response?.data?.message || t("errors.general");
  } finally {
    loading.value = false;
  }
};

const handleSkip = async () => {
  router.push("/onboarding/step2");
};
</script>

<template>
  <section data-testid="view-onboarding-step1" class="mx-auto w-full max-w-3xl space-y-8">
    <div class="space-y-3 text-center">
      <span class="inline-flex w-fit items-center rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold uppercase tracking-[0.22em] text-blue-700">
        {{ t("onboarding.step1Title") }}
      </span>
      <div class="space-y-2">
        <h2 class="text-2xl font-semibold tracking-tight text-slate-950 sm:text-3xl">
          {{ t("onboarding.step1Title") }}
        </h2>
        <p class="mx-auto max-w-2xl text-sm leading-7 text-slate-600">
          {{ t("onboarding.step1Description") }}
        </p>
      </div>
    </div>

    <div class="rounded-[28px] border border-blue-100 bg-blue-50/60 px-5 py-4 text-sm leading-7 text-blue-900">
      Set up your identity first. The username check runs in the background while you type.
    </div>

    <form @submit="handleSubmit" class="space-y-5 rounded-[28px] border border-slate-200 bg-white p-5 shadow-[0_24px_48px_rgba(15,23,42,0.04)] sm:p-6">
      <label class="block space-y-2">
        <span class="text-sm font-semibold text-slate-800">
          {{ t("onboarding.displayNameLabel") }}
        </span>
        <input
          v-model="form.displayName"
          type="text"
          required
          :placeholder="t('onboarding.displayNamePlaceholder')"
          class="h-12 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-slate-900 placeholder:text-slate-400 transition focus:border-blue-500 focus:bg-white focus:outline-none focus:ring-4 focus:ring-blue-500/10"
        />
      </label>

      <label class="block space-y-2">
        <span class="text-sm font-semibold text-slate-800">
          {{ t("onboarding.usernameLabel") }}
          <span class="text-blue-700">*</span>
        </span>
        <div class="relative">
          <input
            v-model="form.username"
            type="text"
            required
            minlength="3"
            :placeholder="t('onboarding.usernamePlaceholder')"
            class="h-12 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 pr-28 text-slate-900 placeholder:text-slate-400 transition focus:border-blue-500 focus:bg-white focus:outline-none focus:ring-4 focus:ring-blue-500/10"
            :class="usernameAvailable === false ? 'border-rose-300 focus:border-rose-400 focus:ring-rose-500/10' : usernameAvailable === true ? 'border-emerald-300 focus:border-emerald-400 focus:ring-emerald-500/10' : ''"
            @input="debouncedCheck"
          />
          <div class="absolute inset-y-0 right-4 flex items-center">
            <span v-if="usernameChecking" class="text-xs font-medium text-slate-500">
              {{ t("onboarding.usernameChecking") }}
            </span>
            <span v-else-if="usernameAvailable === true" class="rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-semibold text-emerald-700">
              {{ t("onboarding.usernameAvailable") }}
            </span>
            <span v-else-if="usernameAvailable === false" class="rounded-full bg-rose-50 px-2.5 py-1 text-xs font-semibold text-rose-700">
              {{ t("onboarding.usernameTaken") }}
            </span>
          </div>
        </div>
      </label>

      <p v-if="usernameAvailable === true" class="text-sm font-medium text-emerald-700">
        {{ t("onboarding.usernameAvailable") }}
      </p>
      <p v-if="usernameAvailable === false" class="text-sm font-medium text-rose-700">
        {{ t("onboarding.usernameTaken") }}
      </p>

      <p v-if="error" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
        {{ error }}
      </p>

      <div class="flex flex-col gap-3 pt-2 sm:flex-row sm:justify-end">
        <button
          type="button"
          class="inline-flex h-12 items-center justify-center rounded-full border border-slate-200 bg-white px-5 text-sm font-semibold text-slate-700 transition hover:border-slate-300 hover:bg-slate-50"
          @click="handleSkip"
        >
          {{ t("onboarding.skipStep") }}
        </button>
        <button
          type="submit"
          :disabled="loading || usernameAvailable === false"
          class="inline-flex h-12 items-center justify-center rounded-full bg-blue-600 px-5 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {{ loading ? t("common.loading") : t("onboarding.continue") }}
        </button>
      </div>
    </form>
  </section>
</template>
