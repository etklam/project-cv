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
</script>

<template>
  <section
    data-testid="view-onboarding-layout"
    class="relative isolate min-h-[calc(100vh-11rem)] overflow-hidden px-4 py-8 sm:px-6 lg:px-8"
  >
    <div aria-hidden="true" class="absolute inset-0 -z-10 overflow-hidden">
      <div class="absolute left-1/2 top-0 h-80 w-80 -translate-x-1/2 rounded-full bg-blue-100/45 blur-3xl"></div>
      <div class="absolute left-0 top-24 h-96 w-96 rounded-full bg-slate-200/40 blur-3xl"></div>
      <div class="absolute bottom-0 right-0 h-80 w-80 rounded-full bg-amber-100/35 blur-3xl"></div>
    </div>

    <div class="mx-auto grid w-full max-w-7xl gap-6 lg:grid-cols-[300px_minmax(0,1fr)]">
      <aside class="hidden flex-col rounded-[32px] border border-slate-200 bg-white/90 p-6 shadow-[0_24px_48px_rgba(15,23,42,0.06)] backdrop-blur lg:flex">
        <div class="space-y-5">
          <div class="space-y-3">
            <span class="inline-flex w-fit items-center rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold uppercase tracking-[0.22em] text-blue-700">
              Profile setup
            </span>
            <div class="space-y-2">
              <h2 class="text-2xl font-semibold tracking-tight text-slate-950">
                {{ t("onboarding.title") }}
              </h2>
              <p class="text-sm leading-7 text-slate-600">
                Finish the basics, add your background, then choose the visual system for your resume.
              </p>
            </div>
          </div>

          <div class="space-y-2">
            <div
              v-for="(item, index) in stepItems"
              :key="item.key"
              class="rounded-2xl border px-4 py-4 transition"
              :class="index + 1 === currentStep ? 'border-blue-200 bg-blue-50' : 'border-slate-200 bg-slate-50/80'"
            >
              <p class="text-[11px] font-semibold uppercase tracking-[0.22em] text-slate-400">
                Step {{ index + 1 }}
              </p>
              <p class="mt-1 font-semibold text-slate-950">{{ item.title }}</p>
              <p class="mt-1 text-sm leading-6 text-slate-600">{{ item.description }}</p>
            </div>
          </div>
        </div>

        <div class="mt-6 rounded-[28px] border border-slate-200 bg-slate-950 p-5 text-white">
          <p class="text-[11px] font-semibold uppercase tracking-[0.24em] text-blue-200">Progress</p>
          <div class="mt-4 flex items-end justify-between gap-4">
            <div>
              <p class="text-sm text-white/70">Current step</p>
              <p class="mt-1 text-3xl font-semibold tracking-tight">{{ currentStep }}</p>
            </div>
            <p class="text-sm text-white/70">of {{ totalSteps }}</p>
          </div>
          <div class="mt-4 h-2 overflow-hidden rounded-full bg-white/10">
            <div class="h-full rounded-full bg-blue-400 transition-all duration-300" :style="{ width: `${progress}%` }"></div>
          </div>
        </div>
      </aside>

      <div class="space-y-6">
        <div class="rounded-[32px] border border-slate-200 bg-white/90 p-6 shadow-[0_24px_48px_rgba(15,23,42,0.06)] backdrop-blur sm:p-8">
          <div class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
            <div class="space-y-3">
              <span class="inline-flex w-fit items-center rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold uppercase tracking-[0.22em] text-blue-700">
                Workspace onboarding
              </span>
              <div class="space-y-2">
                <h1 class="text-3xl font-semibold tracking-tight text-slate-950 sm:text-4xl">
                  {{ t("onboarding.title") }}
                </h1>
                <p class="max-w-2xl text-sm leading-7 text-slate-600">
                  Finish the basics, add your background, then choose the visual system for your resume.
                </p>
              </div>
            </div>

            <span class="inline-flex w-fit items-center rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700">
              {{ t("onboarding.progress", { current: currentStep, total: totalSteps }) }}
            </span>
          </div>

          <div class="mt-6 h-2 overflow-hidden rounded-full bg-slate-100">
            <div
              class="h-full rounded-full bg-blue-600 transition-all duration-300"
              :style="{ width: `${progress}%` }"
            ></div>
          </div>
        </div>

        <div class="rounded-[32px] border border-slate-200 bg-white/95 p-4 shadow-[0_24px_48px_rgba(15,23,42,0.06)] backdrop-blur sm:p-6 lg:p-8">
          <RouterView />
        </div>
      </div>
    </div>
  </section>
</template>
