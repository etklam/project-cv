<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import TemplateCard from "@/components/templates/TemplateCard.vue";
import { useTemplateCatalog } from "@/composables/useTemplateCatalog";

const { t } = useI18n();
const router = useRouter();

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
    // Complete onboarding with selected template
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
  <section data-testid="view-onboarding-step3">
    <div class="text-center mb-8">
      <h2 class="text-xl sm:text-2xl font-bold text-gray-900">{{ t("onboarding.step3Title") }}</h2>
      <p class="text-gray-600 mt-2">{{ t("onboarding.step3Description") }}</p>
    </div>

    <div v-if="templateCatalog.loading.value" class="text-center py-12">
      <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
      <p class="mt-4 text-gray-600">{{ t("common.loading") }}</p>
    </div>

    <div v-else-if="templateCatalog.error.value" class="bg-red-50 text-red-800 px-4 py-3 rounded-lg text-center" data-testid="templates-error">
      {{ templateCatalog.error.value }}
    </div>

    <div v-else>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-8" data-testid="template-grid">
        <template-card
          v-for="template in featuredTemplates"
          :key="template.key"
          :template="template"
          :selected="template.key === selectedTemplateKey"
          @click="selectTemplate(template.key)"
        />
      </div>

      <div v-if="error" class="bg-red-50 text-red-800 px-4 py-3 rounded-lg text-sm mb-6">
        {{ error }}
      </div>

      <div class="bg-gray-50 rounded-lg px-4 py-3 mb-6 flex items-center justify-between">
        <span class="text-gray-700">{{ t("onboarding.templateSelected") }}:</span>
        <span class="font-semibold text-blue-600" data-testid="selected-template">
          {{ selectedTemplateKey || t("common.select") }}
        </span>
      </div>

      <button
        @click="handleSubmit"
        :disabled="loading || !selectedTemplateKey"
        class="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white font-semibold py-3 px-4 rounded-lg transition-colors"
      >
        {{ loading ? t("common.loading") : t("onboarding.finish") }}
      </button>
    </div>
  </section>
</template>
