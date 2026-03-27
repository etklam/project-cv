<script setup>
import { computed, onMounted, ref, watch } from "vue";
import TemplateCard from "@/components/templates/TemplateCard.vue";
import { useTemplateCatalog } from "@/composables/useTemplateCatalog";

const selectedTemplateKey = ref(null);
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

onMounted(templateCatalog.loadTemplates);
</script>

<template>
  <section data-testid="view-onboarding-step3">
    <header class="section-header">
      <h2>Choose a template</h2>
      <p>Select the visual style that best matches your story.</p>
    </header>

    <div v-if="templateCatalog.loading.value" class="placeholder" data-testid="templates-loading">
      Loading templates...
    </div>
    <div v-else-if="templateCatalog.error.value" class="placeholder" data-testid="templates-error">
      {{ templateCatalog.error.value }}
    </div>
    <div v-else class="template-grid" data-testid="template-grid">
      <template-card
        v-for="template in featuredTemplates"
        :key="template.key"
        :template="template"
        :selected="template.key === selectedTemplateKey"
        @click="selectTemplate(template.key)"
      />
    </div>
    <p class="selected-summary" data-testid="selected-template">
      Selected: {{ selectedTemplateKey || "—" }}
    </p>
  </section>
</template>

<style scoped>
.section-header h2 {
  font-size: 1.35rem;
  margin-bottom: 0.25rem;
}
.section-header p {
  color: #475569;
  margin: 0;
}
.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
  margin-top: 1rem;
}
.placeholder {
  padding: 1rem;
  border-radius: 0.75rem;
  border: 1px dashed #94a3b8;
  color: #475569;
  margin-top: 1rem;
}
.selected-summary {
  margin-top: 1rem;
  font-weight: 600;
  color: #1d4ed8;
}
</style>
