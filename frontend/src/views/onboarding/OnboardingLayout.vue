<script setup>
import { computed } from "vue";
import { useRoute } from "vue-router";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const route = useRoute();

const steps = ["step1", "step2", "step3"];
const currentStepIndex = computed(() => {
  const step = route.path.split("/").pop();
  return steps.indexOf(step);
});
const currentStep = computed(() => currentStepIndex.value + 1);
const totalSteps = 3;
const progress = computed(() => ((currentStep.value / totalSteps) * 100).toFixed(0));
</script>

<template>
  <section data-testid="view-onboarding-layout" class="min-h-screen bg-gray-50">
    <div class="max-w-3xl mx-auto px-4 sm:px-6 py-8">
      <!-- Progress Bar -->
      <div class="mb-8">
        <div class="flex items-center justify-between mb-2">
          <h1 class="text-xl sm:text-2xl font-bold text-gray-900">{{ t("onboarding.title") }}</h1>
          <span class="text-sm text-gray-600">{{ t("onboarding.progress", { current: currentStep, total: totalSteps }) }}</span>
        </div>
        <div class="w-full bg-gray-200 rounded-full h-2">
          <div
            class="bg-blue-600 h-2 rounded-full transition-all duration-300"
            :style="{ width: `${progress}%` }"
          ></div>
        </div>
      </div>

      <!-- Step Content -->
      <div class="bg-white rounded-xl shadow-lg p-6 sm:p-8">
        <RouterView />
      </div>
    </div>
  </section>
</template>
