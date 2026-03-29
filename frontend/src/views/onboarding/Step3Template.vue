<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import TemplateCard from "@/components/templates/TemplateCard.vue";
import { useAuthStore } from "@/stores/auth";
import { submitStep3 } from "@/api/onboarding";
import { useTemplateCatalog } from "@/composables/useTemplateCatalog";

const { t } = useI18n();
const router = useRouter();
const auth = useAuthStore();

const selectedTemplateKey = ref(null);
const loading = ref(false);
const error = ref("");
const templateCatalog = useTemplateCatalog();
const featuredTemplates = computed(() => templateCatalog.visibleTemplates.value);

watch(
  () => templateCatalog.supportedTemplates.value.length,
  (length) => {
    if (!selectedTemplateKey.value && length) {
      selectedTemplateKey.value = templateCatalog.supportedTemplates.value[0].key;
    }
  },
  { immediate: true },
);

const selectTemplate = (key) => {
  selectedTemplateKey.value = key;
};

const handleSubmit = async () => {
  if (!selectedTemplateKey.value) {
    error.value = t("onboarding.selectTemplate");
    return;
  }

  loading.value = true;
  error.value = "";
  try {
    const data = await submitStep3({ templateKey: selectedTemplateKey.value });
    auth.applyUser(data.user);
    router.push("/dashboard");
  } catch (err) {
    error.value = err?.response?.data?.message || t("errors.general");
  } finally {
    loading.value = false;
  }
};

onMounted(templateCatalog.loadTemplates);
</script>

<template>
  <section data-testid="view-onboarding-step3" class="mx-auto w-full max-w-7xl space-y-8">
    <div class="grid gap-4 lg:grid-cols-[minmax(0,1fr)_260px]">
      <div class="rounded-[30px] border border-outline-variant/30 bg-surface-container-low px-6 py-6">
        <span class="inline-flex w-fit items-center rounded-full bg-primary-fixed px-3 py-1 text-xs font-bold uppercase tracking-[0.22em] text-on-primary-fixed">
          {{ t("onboarding.step3Title") }}
        </span>
        <h2 class="mt-4 font-headline text-3xl font-extrabold tracking-tight text-on-surface sm:text-4xl">
          Choose the visual system.
        </h2>
        <p class="mt-3 max-w-3xl text-sm leading-7 text-on-surface-variant">
          Pick the first presentation mode for your resume. You can change it later in the editor workspace, but this sets the default direction.
        </p>
      </div>

      <div class="rounded-[30px] border border-outline-variant/30 bg-surface-container-lowest px-5 py-5 shadow-sm">
        <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-on-surface-variant">Final step</p>
        <p class="mt-3 text-2xl font-extrabold tracking-tight text-on-surface">03</p>
        <p class="mt-2 text-sm leading-7 text-on-surface-variant">
          Select the template family that best matches your current trajectory.
        </p>
      </div>
    </div>

    <div v-if="templateCatalog.loading.value" class="rounded-[28px] border border-slate-200 bg-slate-50 px-6 py-16 text-center">
      <div class="mx-auto h-12 w-12 animate-spin rounded-full border-2 border-slate-200 border-t-blue-600"></div>
      <p class="mt-4 text-sm font-medium text-slate-600">{{ t("common.loading") }}</p>
    </div>

    <div
      v-else-if="templateCatalog.error.value"
      class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-center text-sm text-rose-700"
      data-testid="templates-error"
    >
      {{ templateCatalog.error.value }}
    </div>

    <div v-else class="grid gap-6 xl:grid-cols-[minmax(0,1fr)_320px]">
      <div class="space-y-4">
        <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-2" data-testid="template-grid">
          <TemplateCard
            v-for="template in featuredTemplates"
            :key="template.key"
            :template="template"
            :selected="template.key === selectedTemplateKey"
            @click="selectTemplate(template.key)"
          />
        </div>

        <div v-if="error" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
          {{ error }}
        </div>
      </div>

      <aside class="rounded-[32px] border border-outline-variant/30 bg-white p-6 shadow-[0_24px_48px_rgba(15,23,42,0.04)]">
        <p class="text-[11px] font-semibold uppercase tracking-[0.24em] text-slate-400">
          {{ t("onboarding.templateSelected") }}
        </p>
        <p class="mt-3 font-headline text-3xl font-extrabold tracking-tight text-slate-950" data-testid="selected-template">
          {{ selectedTemplateKey || t("onboarding.chooseTemplate") }}
        </p>
        <p class="mt-3 text-sm leading-7 text-slate-600">
          This becomes the starting point for your editor workspace and first public preview.
        </p>

        <div class="mt-6 rounded-[26px] border border-primary/10 bg-primary-fixed/35 p-4">
          <p class="text-xs font-semibold uppercase tracking-[0.2em] text-primary">Ready to publish</p>
          <p class="mt-2 text-sm leading-7 text-on-surface">
            The selected template will be used as your default resume workspace theme.
          </p>
        </div>

        <button
          class="mt-6 inline-flex h-12 w-full items-center justify-center rounded-[18px] bg-gradient-to-br from-primary to-primary-container px-6 text-sm font-semibold text-white shadow-lg transition hover:scale-[1.01] hover:shadow-xl disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="loading || !selectedTemplateKey"
          @click="handleSubmit"
        >
          {{ loading ? t("common.loading") : t("onboarding.finish") }}
        </button>
      </aside>
    </div>
  </section>
</template>
