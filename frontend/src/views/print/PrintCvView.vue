<script setup>
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { getCv } from "@/api/cv";
import CvTemplateRenderer from "@/components/cv-templates/CvTemplateRenderer.vue";

const { t } = useI18n();
const route = useRoute();
const cvId = route?.params?.id;

const loading = ref(true);
const error = ref("");
const cv = ref(null);
const sections = ref([]);

onMounted(async () => {
  try {
    const payload = await getCv(cvId);
    cv.value = payload?.cv || null;
    sections.value = payload?.sections || [];
  } catch (requestError) {
    error.value =
      requestError?.response?.data?.message || requestError?.message || t("editor.error");
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <section data-testid="view-print" class="min-h-screen bg-gray-50">
    <div v-if="loading" class="flex items-center justify-center min-h-screen">
      <div class="text-center">
        <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        <p class="mt-4 text-gray-600">{{ t("common.loading") }}</p>
      </div>
    </div>
    <div v-else-if="error" class="flex items-center justify-center min-h-screen">
      <div class="bg-red-50 text-red-800 px-6 py-4 rounded-lg max-w-md" data-testid="print-error">
        {{ error }}
      </div>
    </div>
    <div v-else-if="cv" class="max-w-4xl mx-auto p-4 sm:p-8 bg-white shadow-lg my-8">
      <CvTemplateRenderer
        :template-key="cv.templateKey || 'minimal'"
        :cv="cv"
        :sections="sections"
        mode="print"
      />
    </div>
    <div v-else class="flex items-center justify-center min-h-screen">
      <p class="text-gray-600" data-testid="print-empty">{{ t("editor.cvNotFound") }}</p>
    </div>
  </section>
</template>

<style scoped>
@media print {
  section {
    background: white;
  }
  .max-w-4xl {
    max-width: 100%;
    box-shadow: none;
    margin: 0;
    padding: 0;
  }
}
</style>
