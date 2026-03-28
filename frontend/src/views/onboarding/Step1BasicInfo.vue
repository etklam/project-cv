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
    // Call onboarding step1 API
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
  <section data-testid="view-onboarding-step1">
    <div class="text-center mb-8">
      <h2 class="text-xl sm:text-2xl font-bold text-gray-900">{{ t("onboarding.step1Title") }}</h2>
      <p class="text-gray-600 mt-2">{{ t("onboarding.step1Description") }}</p>
    </div>

    <form @submit="handleSubmit" class="space-y-5">
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">
          {{ t("onboarding.displayNameLabel") }}
        </label>
        <input
          v-model="form.displayName"
          type="text"
          required
          :placeholder="t('onboarding.displayNamePlaceholder')"
          class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">
          {{ t("onboarding.usernameLabel") }}
          <span class="text-red-500">*</span>
        </label>
        <div class="relative">
          <input
            v-model="form.username"
            type="text"
            required
            minlength="3"
            :placeholder="t('onboarding.usernamePlaceholder')"
            class="w-full px-4 py-3 pr-24 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            :class="usernameAvailable === false ? 'border-red-300' : usernameAvailable === true ? 'border-green-300' : 'border-gray-300'"
            @input="debouncedCheck"
          />
          <div class="absolute right-3 top-1/2 -translate-y-1/2">
            <span v-if="usernameChecking" class="text-sm text-gray-500">{{ t("onboarding.usernameChecking") }}</span>
            <span v-else-if="usernameAvailable === true" class="text-sm text-green-600">✓</span>
            <span v-else-if="usernameAvailable === false" class="text-sm text-red-600">✗</span>
          </div>
        </div>
        <p v-if="usernameAvailable === true" class="text-sm text-green-600 mt-1">
          {{ t("onboarding.usernameAvailable") }}
        </p>
        <p v-if="usernameAvailable === false" class="text-sm text-red-600 mt-1">
          {{ t("onboarding.usernameTaken") }}
        </p>
      </div>

      <div v-if="error" class="bg-red-50 text-red-800 px-4 py-3 rounded-lg text-sm">
        {{ error }}
      </div>

      <div class="flex gap-3 pt-4">
        <button
          type="submit"
          :disabled="loading || usernameAvailable === false"
          class="flex-1 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white font-semibold py-3 px-4 rounded-lg transition-colors"
        >
          {{ loading ? t("common.loading") : t("onboarding.continue") }}
        </button>
      </div>
    </form>
  </section>
</template>
