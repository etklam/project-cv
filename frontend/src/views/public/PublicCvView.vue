<script setup>
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import CvTemplateRenderer from "@/components/cv-templates/CvTemplateRenderer.vue";
import { getPublicCv } from "@/api/public";

const { t } = useI18n();
const route = useRoute();
const username = route?.params?.username || "";
const slug = route?.params?.slug || "";

const loading = ref(true);
const error = ref("");
const user = ref(null);
const cv = ref(null);
const sections = ref([]);

onMounted(async () => {
  try {
    const result = await getPublicCv(username, slug);
    user.value = result.user || null;
    cv.value = result.cv || null;
    sections.value = result.sections || [];
  } catch (requestError) {
    error.value =
      requestError?.response?.data?.message || requestError?.message || t("public.notFound");
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <section data-testid="view-public-cv" class="min-h-screen bg-gray-50">
    <div v-if="loading" class="flex items-center justify-center min-h-screen">
      <div class="text-center">
        <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        <p class="mt-4 text-gray-600">{{ t("common.loading") }}</p>
      </div>
    </div>
    <div v-else-if="error" class="flex items-center justify-center min-h-screen">
      <div class="bg-red-50 text-red-800 px-6 py-4 rounded-lg max-w-md text-center" data-testid="public-cv-error">
        {{ error }}
      </div>
    </div>
    <div v-else-if="cv" class="max-w-4xl mx-auto px-4 sm:px-6 py-8">
      <header class="mb-6">
        <h1 class="text-2xl sm:text-3xl font-bold text-gray-900" data-testid="public-cv-title">
          {{ cv.title || slug }}
        </h1>
        <p class="text-gray-600 mt-1" data-testid="public-cv-owner">
          @{{ user?.username || username }}
        </p>
      </header>
      <div class="bg-white rounded-xl shadow-lg p-6 sm:p-8">
        <CvTemplateRenderer
          :template-key="cv.templateKey || 'minimal'"
          :cv="cv"
          :sections="sections"
          mode="public"
        />
      </div>
    </div>
    <div v-else class="flex items-center justify-center min-h-screen">
      <p class="text-gray-600" data-testid="public-cv-empty">{{ t("public.cvTitle") }} {{ t("public.notFound") }}</p>
    </div>
  </section>
</template>
