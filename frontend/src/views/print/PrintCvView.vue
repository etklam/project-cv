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
  <section
    data-testid="view-print"
    class="min-h-screen bg-[#FAFAFA] text-[#09090B]"
  >
    <div class="mx-auto flex min-h-screen w-full max-w-6xl flex-col px-4 py-6 sm:px-6 lg:px-8">
      <header class="rounded-3xl border border-gray-200 bg-white px-5 py-4 sm:px-6">
        <div class="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p class="text-xs font-semibold uppercase tracking-[0.24em] text-gray-400">
              Print preview
            </p>
            <h1 class="mt-2 text-xl font-semibold tracking-tight text-[#18181B] sm:text-2xl">
              {{ cv?.title || t("editor.preview") }}
            </h1>
          </div>
          <p class="text-sm text-gray-500">
            {{ t("editor.template") }}: {{ cv?.templateKey || "minimal" }}
          </p>
        </div>
      </header>

      <div v-if="loading" class="flex flex-1 items-center justify-center py-16">
        <div class="text-center">
          <div
            class="inline-block h-12 w-12 animate-spin rounded-full border-2 border-[#18181B] border-t-transparent"
          ></div>
          <p class="mt-4 text-sm text-gray-600">{{ t("common.loading") }}</p>
        </div>
      </div>

      <div v-else-if="error" class="flex flex-1 items-center justify-center py-16">
        <div
          class="w-full max-w-md rounded-3xl border border-red-200 bg-red-50 px-6 py-5 text-red-800"
          data-testid="print-error"
        >
          {{ error }}
        </div>
      </div>

      <div v-else-if="cv" class="flex-1 py-6 sm:py-8">
        <div class="mx-auto max-w-5xl">
          <div class="overflow-hidden rounded-[2rem] border border-gray-200 bg-white">
            <div class="border-b border-gray-100 px-5 py-4 sm:px-8">
              <p class="text-xs font-semibold uppercase tracking-[0.22em] text-gray-400">
                A4 paper canvas
              </p>
            </div>
            <div class="p-5 sm:p-8">
              <CvTemplateRenderer
                :template-key="cv.templateKey || 'minimal'"
                :cv="cv"
                :sections="sections"
                mode="print"
              />
            </div>
          </div>
        </div>
      </div>

      <div v-else class="flex flex-1 items-center justify-center py-16">
        <p class="text-sm text-gray-600" data-testid="print-empty">
          {{ t("editor.cvNotFound") }}
        </p>
      </div>
    </div>
  </section>
</template>

<style scoped>
@media print {
  section {
    background: white !important;
  }

  header {
    display: none;
  }

  .max-w-6xl {
    max-width: none;
  }

  .max-w-5xl {
    max-width: none;
  }

  .rounded-\[2rem\] {
    border: 0;
    border-radius: 0;
  }

  .border {
    border: 0;
  }

  .p-5,
  .p-8 {
    padding: 0;
  }

  .py-6,
  .py-8 {
    padding-top: 0;
    padding-bottom: 0;
  }
}
</style>
