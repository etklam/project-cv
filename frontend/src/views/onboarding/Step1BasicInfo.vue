<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { useAuthStore } from "@/stores/auth";
import { submitStep1 } from "@/api/onboarding";
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
    const data = await submitStep1({
      displayName: form.value.displayName.trim(),
      username: form.value.username.trim(),
    });
    auth.applyUser(data.user);
    router.push("/onboarding/step2");
  } catch (err) {
    error.value = err?.response?.data?.message || t("errors.general");
  } finally {
    loading.value = false;
  }
};

const handleSkip = async () => {
  if (!form.value.username.trim()) {
    error.value = t("onboarding.usernameTaken");
    return;
  }
  router.push("/onboarding/step2");
};
</script>

<template>
  <section data-testid="view-onboarding-step1" class="mx-auto w-full max-w-4xl space-y-8">
    <div class="grid gap-4 lg:grid-cols-[minmax(0,1fr)_240px]">
      <div class="rounded-[30px] border border-primary/10 bg-gradient-to-br from-primary-fixed/80 to-white px-6 py-6">
        <span class="inline-flex w-fit items-center rounded-full bg-white/90 px-3 py-1 text-xs font-bold uppercase tracking-[0.22em] text-primary">
          {{ t("onboarding.step1Title") }}
        </span>
        <h2 class="mt-4 font-headline text-3xl font-extrabold tracking-tight text-on-surface sm:text-4xl">
          Set your public identity.
        </h2>
        <p class="mt-3 max-w-2xl text-sm leading-7 text-on-surface-variant">
          Start with the signals people see first. We check username availability while you type so the path to publishing stays clear.
        </p>
      </div>

      <div class="rounded-[30px] border border-outline-variant/40 bg-surface-container-lowest px-5 py-5 shadow-sm">
        <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-on-surface-variant">Live checks</p>
        <p class="mt-3 text-2xl font-extrabold tracking-tight text-on-surface">01</p>
        <p class="mt-2 text-sm leading-7 text-on-surface-variant">
          Username validation runs in the background and keeps your public route usable.
        </p>
      </div>
    </div>

    <form @submit="handleSubmit" class="space-y-6 rounded-[32px] border border-outline-variant/40 bg-white/95 p-6 shadow-[0_24px_48px_rgba(15,23,42,0.04)] sm:p-7">
      <label class="block space-y-2">
        <span class="text-xs font-bold uppercase tracking-[0.22em] text-on-surface-variant">
          {{ t("onboarding.displayNameLabel") }}
        </span>
        <input
          v-model="form.displayName"
          type="text"
          required
          :placeholder="t('onboarding.displayNamePlaceholder')"
          class="h-14 w-full rounded-[24px] border border-outline-variant/40 bg-surface-container-low px-4 text-on-surface placeholder:text-outline transition focus:border-primary focus:bg-white focus:outline-none focus:ring-4 focus:ring-primary/10"
        />
      </label>

      <label class="block space-y-2">
        <span class="text-xs font-bold uppercase tracking-[0.22em] text-on-surface-variant">
          {{ t("onboarding.usernameLabel") }}
          <span class="text-primary">*</span>
        </span>
        <div class="relative">
          <input
            v-model="form.username"
            type="text"
            required
            minlength="3"
            :placeholder="t('onboarding.usernamePlaceholder')"
            class="h-14 w-full rounded-[24px] border border-outline-variant/40 bg-surface-container-low px-4 pr-32 text-on-surface placeholder:text-outline transition focus:border-primary focus:bg-white focus:outline-none focus:ring-4 focus:ring-primary/10"
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

      <div class="flex flex-col gap-3 border-t border-outline-variant/20 pt-4 sm:flex-row sm:justify-end">
        <button
          type="button"
          class="inline-flex h-12 items-center justify-center rounded-full border border-outline-variant/40 bg-white px-5 text-sm font-semibold text-on-surface-variant transition hover:border-outline hover:bg-surface-container-low"
          @click="handleSkip"
        >
          {{ t("onboarding.skipStep") }}
        </button>
        <button
          type="submit"
          :disabled="loading || usernameAvailable === false"
          class="inline-flex h-12 items-center justify-center rounded-[18px] bg-gradient-to-br from-primary to-primary-container px-6 text-sm font-semibold text-on-primary shadow-lg transition hover:scale-[1.01] hover:shadow-xl disabled:cursor-not-allowed disabled:opacity-50"
        >
          {{ loading ? t("common.loading") : t("onboarding.continue") }}
        </button>
      </div>
    </form>
  </section>
</template>
