<script setup>
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import CvTemplateRenderer from "@/components/cv-templates/CvTemplateRenderer.vue";
import { getPublicCv } from "@/api/public";

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
      requestError?.response?.data?.message || requestError?.message || "Failed to load public CV";
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <section data-testid="view-public-cv">
    <div v-if="loading" data-testid="public-cv-loading">Loading public CV...</div>
    <div v-else-if="error" data-testid="public-cv-error">{{ error }}</div>
    <div v-else-if="cv">
      <header>
        <h1 data-testid="public-cv-title">{{ cv.title || slug }}</h1>
        <p data-testid="public-cv-owner">@{{ user?.username || username }}</p>
      </header>
      <CvTemplateRenderer
        :template-key="cv.templateKey || 'minimal'"
        :cv="cv"
        :sections="sections"
        mode="public"
      />
    </div>
    <div v-else data-testid="public-cv-empty">Public CV not found.</div>
  </section>
</template>
