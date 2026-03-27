<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute } from "vue-router";
import CvTemplateRenderer from "@/components/cv-templates/CvTemplateRenderer.vue";
import { useTemplateCatalog } from "@/composables/useTemplateCatalog";
import { useCvStore } from "@/stores/cv";

const route = useRoute();
const cvStore = useCvStore();
const cvId = computed(() => route?.params?.id || "");

const loadError = ref("");
const saveMessage = ref("");
const {
  supportedTemplates: templates,
  loading: templatesLoading,
  error: templatesError,
  loadTemplates: loadTemplateOptions,
} = useTemplateCatalog();

const form = reactive({
  title: "",
  templateKey: "minimal",
  isPublic: false,
  slug: "",
});

function normalizeSlug(value) {
  return String(value || "")
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9-]+/g, "-")
    .replace(/-+/g, "-")
    .replace(/^-|-$/g, "");
}

function applyCvToForm(cv) {
  form.title = cv?.title || "";
  form.templateKey = cv?.templateKey || "minimal";
  form.isPublic = Boolean(cv?.isPublic);
  form.slug = cv?.slug || "";
}

const activeCv = computed(() => ({
  ...(cvStore.currentCv || {}),
  title: form.title || cvStore.currentCv?.title || "",
  templateKey: form.templateKey || cvStore.currentCv?.templateKey || "minimal",
  isPublic: form.isPublic,
  slug: form.slug || null,
}));

const loadCvMetadata = async () => {
  loadError.value = "";
  if (!cvId.value) {
    loadError.value = "Missing CV id";
    return;
  }
  try {
    await cvStore.loadCv(cvId.value);
    applyCvToForm(cvStore.currentCv);
  } catch (requestError) {
    loadError.value =
      requestError?.response?.data?.message || requestError?.message || "Failed to load CV";
  }
};

const saveMetadata = async () => {
  saveMessage.value = "";
  loadError.value = "";
  if (!form.title.trim()) {
    loadError.value = "Title is required";
    return;
  }
  const normalizedSlug = normalizeSlug(form.slug);
  if (form.isPublic && !normalizedSlug) {
    loadError.value = "Public CV requires a slug";
    return;
  }

  const payload = {
    title: form.title.trim(),
    templateKey: form.templateKey || "minimal",
    isPublic: form.isPublic,
    slug: form.isPublic ? normalizedSlug : null,
  };

  try {
    await cvStore.updateMetadata(cvId.value, payload);
    applyCvToForm(cvStore.currentCv);
    saveMessage.value = "Saved";
  } catch (requestError) {
    loadError.value =
      requestError?.response?.data?.message || requestError?.message || "Failed to save CV metadata";
  }
};

onMounted(() => {
  loadCvMetadata();
  loadTemplateOptions();
});
</script>

<template>
  <section data-testid="view-editor">
    <h1>Editor</h1>

    <div v-if="cvStore.loading" data-testid="editor-loading">Loading CV metadata...</div>
    <div v-else-if="loadError" data-testid="editor-error">{{ loadError }}</div>
    <form v-else class="editor-form" data-testid="editor-form" @submit.prevent="saveMetadata">
      <label>
        Title
        <input v-model="form.title" data-testid="editor-title-input" />
      </label>
      <label>
        Template
        <select v-model="form.templateKey" data-testid="editor-template-select">
          <option
            v-for="template in templates"
            :key="template.key"
            :value="template.key"
          >
            {{ template.displayName || template.key }}
          </option>
        </select>
      </label>
      <label class="editor-form__check">
        <input
          v-model="form.isPublic"
          type="checkbox"
          data-testid="editor-public-toggle"
        />
        Public CV
      </label>
      <label>
        Slug
        <input
          v-model="form.slug"
          :disabled="!form.isPublic"
          data-testid="editor-slug-input"
          @blur="form.slug = normalizeSlug(form.slug)"
        />
      </label>
      <div v-if="templatesLoading" data-testid="editor-templates-loading">Loading templates...</div>
      <div v-else-if="templatesError" data-testid="editor-templates-error">{{ templatesError }}</div>
      <button type="submit" :disabled="cvStore.saving" data-testid="editor-save-button">
        {{ cvStore.saving ? "Saving..." : "Save metadata" }}
      </button>
      <p v-if="saveMessage" data-testid="editor-save-message">{{ saveMessage }}</p>
    </form>

    <p v-if="activeCv?.isPublic && activeCv?.slug" data-testid="editor-public-path">
      Public path: /u/&lt;username&gt;/{{ activeCv.slug }}
    </p>

    <button type="button">Export PDF</button>
    <CvTemplateRenderer
      :template-key="activeCv?.templateKey || form.templateKey || 'minimal'"
      :cv="activeCv"
      :sections="cvStore.sections"
      mode="editor-preview"
    />
  </section>
</template>

<style scoped>
.editor-form {
  display: grid;
  gap: 0.75rem;
  max-width: 32rem;
  margin-bottom: 1rem;
}
.editor-form label {
  display: grid;
  gap: 0.3rem;
  font-weight: 600;
}
.editor-form input,
.editor-form select {
  border: 1px solid #cbd5e1;
  border-radius: 0.5rem;
  padding: 0.45rem 0.6rem;
  font-weight: 400;
}
.editor-form__check {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
</style>
