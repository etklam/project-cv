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
const currentStep = computed(() => Math.max(currentStepIndex.value + 1, 1));
const totalSteps = 3;
const progress = computed(() => ((currentStep.value / totalSteps) * 100).toFixed(0));

const stepItems = computed(() => [
  { key: "step1", title: t("onboarding.step1Title"), description: t("onboarding.step1Description") },
  { key: "step2", title: t("onboarding.step2Title"), description: t("onboarding.step2Description") },
  { key: "step3", title: t("onboarding.step3Title"), description: t("onboarding.step3Description") },
]);

const currentStepItem = computed(() => stepItems.value[currentStepIndex.value] || stepItems.value[0]);
</script>

<template>
  <section
    data-testid="view-onboarding-layout"
    class="relative isolate min-h-[calc(100vh-5rem)] overflow-hidden bg-background px-4 py-6 text-on-background sm:px-6 lg:px-8 lg:py-8"
  >
    <div aria-hidden="true" class="absolute inset-0 -z-10 overflow-hidden">
      <div class="absolute left-[12%] top-0 h-80 w-80 rounded-full bg-primary-fixed/60 blur-3xl"></div>
      <div class="absolute right-[8%] top-20 h-96 w-96 rounded-full bg-tertiary-fixed/40 blur-3xl"></div>
      <div class="absolute bottom-0 left-1/3 h-80 w-80 rounded-full bg-secondary-container/40 blur-3xl"></div>
    </div>

    <div class="mx-auto flex max-w-7xl flex-col gap-6">
      <header class="rounded-[32px] border border-outline-variant/40 bg-white/80 px-5 py-4 shadow-[0_24px_48px_rgba(15,23,42,0.05)] backdrop-blur sm:px-6">
        <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
          <div class="space-y-2">
            <span class="inline-flex w-fit items-center rounded-full bg-primary-fixed px-3 py-1 text-xs font-bold uppercase tracking-[0.24em] text-on-primary-fixed">
              Workspace onboarding
            </span>
            <div>
              <h1 class="font-headline text-3xl font-extrabold tracking-tight text-on-surface sm:text-4xl">
                {{ currentStepItem.title }}
              </h1>
              <p class="mt-2 max-w-2xl text-sm leading-7 text-on-surface-variant">
                {{ currentStepItem.description }}
              </p>
            </div>
          </div>

          <div class="flex flex-col gap-3 sm:items-end">
            <span class="inline-flex w-fit items-center rounded-full border border-outline-variant/40 bg-surface-container-lowest px-4 py-2 text-sm font-semibold text-on-surface">
              {{ t("onboarding.progress", { current: currentStep, total: totalSteps }) }}
            </span>
            <div class="h-2 w-full overflow-hidden rounded-full bg-surface-container-high sm:w-64">
              <div class="h-full rounded-full bg-gradient-to-r from-primary to-primary-container transition-all duration-300" :style="{ width: `${progress}%` }"></div>
            </div>
          </div>
        </div>
      </header>

      <div class="grid gap-6 xl:grid-cols-[360px_minmax(0,1fr)]">
        <aside class="hidden flex-col justify-between rounded-[36px] border border-outline-variant/40 bg-surface-container-low px-7 py-8 shadow-[0_24px_48px_rgba(15,23,42,0.06)] xl:flex">
          <div class="space-y-10">
            <div class="space-y-4">
              <span class="inline-flex w-fit items-center rounded-full bg-tertiary-fixed px-3 py-1 text-xs font-bold uppercase tracking-[0.24em] text-on-tertiary-fixed-variant">
                {{ t("onboarding.progress", { current: currentStep, total: totalSteps }) }}
              </span>
              <div class="space-y-3">
                <h2 class="font-headline text-4xl font-extrabold leading-tight tracking-tight text-on-surface">
                  {{ currentStep === 2 ? "Crafting your professional story." : t("onboarding.title") }}
                </h2>
                <p class="text-base leading-8 text-on-surface-variant">
                  Finish the basics, frame your trajectory, then choose the visual system that will carry your resume.
                </p>
              </div>
            </div>

            <nav class="space-y-4">
              <div
                v-for="(item, index) in stepItems"
                :key="item.key"
                class="flex items-center gap-4"
                :class="index + 1 > currentStep ? 'opacity-45' : ''"
              >
                <div
                  class="flex h-10 w-10 items-center justify-center rounded-full text-sm font-extrabold"
                  :class="index + 1 < currentStep ? 'bg-primary text-on-primary' : index + 1 === currentStep ? 'bg-primary text-on-primary' : 'bg-surface-container-high text-on-surface-variant'"
                >
                  <span v-if="index + 1 < currentStep" class="material-symbols-outlined text-base">check</span>
                  <span v-else>{{ index + 1 }}</span>
                </div>
                <div class="min-w-0">
                  <p class="font-semibold" :class="index + 1 === currentStep ? 'text-primary' : 'text-on-surface'">
                    {{ item.title }}
                  </p>
                  <p class="text-sm leading-6 text-on-surface-variant">{{ item.description }}</p>
                </div>
              </div>
            </nav>
          </div>

          <div class="rounded-[28px] border border-outline-variant/30 bg-surface-container-lowest p-5 shadow-sm">
            <div class="flex items-center justify-between gap-4">
              <span class="text-[11px] font-bold uppercase tracking-[0.24em] text-on-surface-variant">Profile Momentum</span>
              <span class="text-xs font-black text-tertiary">450 PTS</span>
            </div>
            <div class="mt-4 flex h-2 gap-1 overflow-hidden rounded-full bg-surface-container-high">
              <div class="h-full w-1/4 rounded-full bg-gradient-to-r from-tertiary to-tertiary-fixed-dim"></div>
              <div class="h-full w-1/4 rounded-full bg-gradient-to-r from-tertiary to-tertiary-fixed-dim"></div>
              <div class="h-full w-1/4 rounded-full bg-surface-container-high"></div>
              <div class="h-full w-1/4 rounded-full bg-surface-container-high"></div>
            </div>
            <p class="mt-4 text-xs leading-6 text-on-surface-variant">
              Complete your background to unlock the strongest editorial recommendations before you enter the workspace.
            </p>
          </div>
        </aside>

        <div class="rounded-[36px] border border-outline-variant/40 bg-white/88 p-4 shadow-[0_24px_48px_rgba(15,23,42,0.06)] backdrop-blur sm:p-6 lg:p-8">
          <RouterView />
        </div>
      </div>
    </div>
  </section>
</template>
