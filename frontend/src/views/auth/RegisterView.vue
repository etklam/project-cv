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

const handleSubmit = async (e) => {
  e.preventDefault();
  error.value = "";
  loading.value = true;

  try {
    await auth.register(form.value);
    router.push("/onboarding/step1");
  } catch (err) {
    error.value = err?.response?.data?.message || err?.message || t("auth.registerError");
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <section data-testid="view-register" class="min-h-screen flex items-center justify-center px-4 py-12">
    <div class="max-w-md w-full">
      <div class="bg-white rounded-xl shadow-lg p-8">
        <div class="text-center mb-8">
          <h1 class="text-2xl sm:text-3xl font-bold text-gray-900">{{ t("auth.registerTitle") }}</h1>
          <p class="text-gray-600 mt-2">{{ t("auth.registerDescription") }}</p>
        </div>

        <form @submit="handleSubmit" class="space-y-5">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              {{ t("auth.displayName") }}
            </label>
            <input
              v-model="form.displayName"
              type="text"
              required
              :placeholder="t('auth.displayName')"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              {{ t("auth.email") }}
            </label>
            <input
              v-model="form.email"
              type="email"
              required
              :placeholder="t('auth.email')"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              {{ t("auth.password") }}
            </label>
            <input
              v-model="form.password"
              type="password"
              required
              :placeholder="t('auth.password')"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div v-if="error" class="bg-red-50 text-red-800 px-4 py-3 rounded-lg text-sm">
            {{ error }}
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white font-semibold py-3 px-4 rounded-lg transition-colors"
          >
            {{ loading ? t("common.loading") : t("auth.register") }}
          </button>
        </form>

        <p class="text-center text-gray-600 mt-6">
          {{ t("auth.alreadyHaveAccount") }}
          <RouterLink to="/login" class="text-blue-600 hover:text-blue-700 font-medium">
            {{ t("auth.login") }}
          </RouterLink>
        </p>
      </div>
    </div>
  </section>
</template>
