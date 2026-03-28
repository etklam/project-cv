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
  <section data-testid="view-onboarding-step2" class="mx-auto w-full max-w-3xl space-y-8">
    <div class="space-y-3 text-center">
      <span class="inline-flex w-fit items-center rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold uppercase tracking-[0.22em] text-blue-700">
        {{ t("onboarding.step2Title") }}
      </span>
      <div class="space-y-2">
        <h2 class="text-2xl font-semibold tracking-tight text-slate-950 sm:text-3xl">
          {{ t("onboarding.step2Title") }}
        </h2>
        <p class="mx-auto max-w-2xl text-sm leading-7 text-slate-600">
          {{ t("onboarding.step2Description") }}
        </p>
      </div>
    </div>

    <div class="rounded-[28px] border border-blue-100 bg-blue-50/60 px-5 py-4 text-sm leading-7 text-blue-900">
      Add a short summary and skills set. You can skip and come back later.
    </div>

    <form @submit="handleSubmit" class="space-y-5 rounded-[28px] border border-slate-200 bg-white p-5 shadow-[0_24px_48px_rgba(15,23,42,0.04)] sm:p-6">
      <label class="block space-y-2">
        <span class="text-sm font-semibold text-slate-800">
          {{ t("onboarding.summaryLabel") }}
        </span>
        <textarea
          v-model="form.summary"
          rows="5"
          :placeholder="t('onboarding.summaryPlaceholder')"
          class="min-h-40 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-slate-900 placeholder:text-slate-400 transition focus:border-blue-500 focus:bg-white focus:outline-none focus:ring-4 focus:ring-blue-500/10"
        />
      </label>

      <label class="block space-y-2">
        <span class="text-sm font-semibold text-slate-800">
          {{ t("onboarding.skillsLabel") }}
        </span>
        <input
          v-model="form.skills"
          type="text"
          :placeholder="t('onboarding.skillsPlaceholder')"
          class="h-12 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-slate-900 placeholder:text-slate-400 transition focus:border-blue-500 focus:bg-white focus:outline-none focus:ring-4 focus:ring-blue-500/10"
        />
        <p class="text-xs font-medium text-slate-500">{{ t("common.optional") }}</p>
      </label>

      <div class="grid gap-3 pt-2 sm:grid-cols-2">
        <button
          type="button"
          @click="handleSkip"
          class="inline-flex h-12 items-center justify-center rounded-full border border-slate-200 bg-white px-5 text-sm font-semibold text-slate-700 transition hover:border-slate-300 hover:bg-slate-50"
        >
          {{ t("onboarding.skipStep") }}
        </button>
        <button
          type="submit"
          :disabled="loading"
          class="inline-flex h-12 items-center justify-center rounded-full bg-blue-600 px-5 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {{ loading ? t("common.loading") : t("onboarding.continue") }}
        </button>
      </div>
    </form>
  </section>
</template>
