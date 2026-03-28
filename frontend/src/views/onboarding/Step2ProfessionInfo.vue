<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const router = useRouter();

const form = ref({
  summary: "",
  skills: "",
});

const loading = ref(false);

const handleSubmit = async (e) => {
  e.preventDefault();
  loading.value = true;
  try {
    // Save to onboarding draft
    router.push("/onboarding/step3");
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const handleSkip = () => {
  router.push("/onboarding/step3");
};
</script>

<template>
  <section data-testid="view-onboarding-step2">
    <div class="text-center mb-8">
      <h2 class="text-xl sm:text-2xl font-bold text-gray-900">{{ t("onboarding.step2Title") }}</h2>
      <p class="text-gray-600 mt-2">{{ t("onboarding.step2Description") }}</p>
    </div>

    <form @submit="handleSubmit" class="space-y-5">
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">
          {{ t("onboarding.summaryLabel") }}
        </label>
        <textarea
          v-model="form.summary"
          rows="4"
          :placeholder="t('onboarding.summaryPlaceholder')"
          class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">
          {{ t("onboarding.skillsLabel") }}
        </label>
        <input
          v-model="form.skills"
          type="text"
          :placeholder="t('onboarding.skillsPlaceholder')"
          class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <p class="text-sm text-gray-500 mt-1">{{ t("common.optional") }}</p>
      </div>

      <div class="flex flex-col sm:flex-row gap-3 pt-4">
        <button
          type="button"
          @click="handleSkip"
          class="flex-1 border border-gray-300 hover:bg-gray-50 text-gray-700 font-semibold py-3 px-4 rounded-lg transition-colors"
        >
          {{ t("onboarding.skipStep") }}
        </button>
        <button
          type="submit"
          :disabled="loading"
          class="flex-1 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white font-semibold py-3 px-4 rounded-lg transition-colors"
        >
          {{ loading ? t("common.loading") : t("onboarding.continue") }}
        </button>
      </div>
    </form>
  </section>
</template>
