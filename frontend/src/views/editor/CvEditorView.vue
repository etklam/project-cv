<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import CvTemplateRenderer from "@/components/cv-templates/CvTemplateRenderer.vue";
import { useTemplateCatalog } from "@/composables/useTemplateCatalog";
import { useCvStore } from "@/stores/cv";
import { exportCvPdf } from "@/api/export";

const { t } = useI18n();
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
const sectionsDraft = ref([]);
const sectionsMessage = ref("");
const sectionErrors = ref({});
const exportMessage = ref("");
const exportError = ref("");

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
  try {
    sectionsDraft.value = (cvStore.sections || []).map((section) => ({
      ...section,
      content: JSON.parse(JSON.stringify(section.content || {})),
    }));
  } catch (e) {
    console.error("Failed to parse section content:", e);
    // Fallback: use empty object for invalid content
    sectionsDraft.value = (cvStore.sections || []).map((section) => ({
      ...section,
      content: {},
    }));
  }
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
    loadError.value = t("editor.cvNotFound");
    return;
  }
  try {
    await cvStore.loadCv(cvId.value);
    applyCvToForm(cvStore.currentCv);
  } catch (requestError) {
    loadError.value =
      requestError?.response?.data?.message || requestError?.message || t("editor.error");
  }
};

const saveMetadata = async () => {
  saveMessage.value = "";
  loadError.value = "";
  if (!form.title.trim()) {
    loadError.value = t("editor.validation.titleRequired");
    return;
  }
  const normalizedSlug = normalizeSlug(form.slug);
  if (form.isPublic && !normalizedSlug) {
    loadError.value = t("editor.validation.publicSlugRequired");
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
    saveMessage.value = t("editor.saved");
  } catch (requestError) {
    loadError.value =
      requestError?.response?.data?.message || requestError?.message || t("errors.general");
  }
};

onMounted(() => {
  loadCvMetadata();
  loadTemplateOptions();
});

const updateSectionField = (index, updater) => {
  const next = [...sectionsDraft.value];
  next[index] = updater(next[index]);
  sectionsDraft.value = next;
};

const updateSectionContentFromJson = (index, jsonString) => {
  const trimmed = jsonString?.trim() || "{}";
  if (!trimmed) {
    sectionErrors.value[index] = "Content cannot be empty";
    return;
  }
  try {
    const parsed = JSON.parse(trimmed);
    sectionErrors.value[index] = "";
    updateSectionField(index, (s) => ({ ...s, content: parsed }));
  } catch (e) {
    sectionErrors.value[index] = `Invalid JSON: ${e.message}`;
  }
};

const saveSections = async () => {
  sectionsMessage.value = "";
  loadError.value = "";

  // Check for JSON parse errors before saving
  const hasErrors = Object.values(sectionErrors.value).some((err) => err);
  if (hasErrors) {
    loadError.value = t("editor.fixJsonErrors");
    return;
  }

  try {
    await cvStore.updateSections(cvId.value, { sections: sectionsDraft.value });
    sectionsMessage.value = t("editor.sectionsSaved");
  } catch (error) {
    loadError.value =
      error?.response?.data?.message || error?.message || t("errors.general");
  }
};

const exportPdf = async () => {
  exportMessage.value = "";
  exportError.value = "";
  try {
    await exportCvPdf(cvId.value);
    exportMessage.value = t("export.success");
  } catch (error) {
    exportError.value =
      error?.response?.data?.message || error?.message || t("export.failed");
  }
};

const currentUser = computed(() => cvStore.currentCv?.user || {});
const publicPath = computed(() => {
  if (activeCv.value?.isPublic && activeCv.value?.slug) {
    const username = currentUser.value?.username || "{username}";
    return `/u/${username}/${activeCv.value.slug}`;
  }
  return "";
});
</script>

<template>
  <section data-testid="view-editor" class="max-w-6xl mx-auto px-4 sm:px-6 py-6">
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
      <h1 class="text-2xl sm:text-3xl font-bold text-gray-900">{{ t("editor.title") }}</h1>
    </div>

    <div v-if="cvStore.loading" class="text-center py-8" data-testid="editor-loading">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      <p class="mt-2 text-gray-600">{{ t("editor.loading") }}</p>
    </div>
    <div v-else-if="loadError" class="bg-red-50 text-red-800 px-4 py-3 rounded-lg" data-testid="editor-error">{{ loadError }}</div>
    <form v-else class="editor-form space-y-4 max-w-2xl" data-testid="editor-form" @submit.prevent="saveMetadata">
      <div class="space-y-2">
        <label class="block text-sm font-medium text-gray-700">
          {{ t("editor.title") }}
          <span class="text-red-500">*</span>
        </label>
        <input
          v-model="form.title"
          class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          data-testid="editor-title-input"
        />
      </div>

      <div class="space-y-2">
        <label class="block text-sm font-medium text-gray-700">{{ t("editor.template") }}</label>
        <select
          v-model="form.templateKey"
          class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          data-testid="editor-template-select"
        >
          <option
            v-for="template in templates"
            :key="template.key"
            :value="template.key"
          >
            {{ template.displayName || template.key }}
          </option>
        </select>
      </div>

      <div class="flex items-center gap-3">
        <input
          v-model="form.isPublic"
          type="checkbox"
          id="is-public"
          class="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
          data-testid="editor-public-toggle"
        />
        <label for="is-public" class="text-sm font-medium text-gray-700 cursor-pointer">
          {{ t("editor.isPublic") }}
        </label>
      </div>
      <p class="text-xs text-gray-500 -mt-2">{{ t("editor.isPublicDescription") }}</p>

      <div class="space-y-2">
        <label class="block text-sm font-medium text-gray-700">{{ t("editor.slug") }}</label>
        <input
          v-model="form.slug"
          :disabled="!form.isPublic"
          class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
          data-testid="editor-slug-input"
          @blur="form.slug = normalizeSlug(form.slug)"
        />
        <p class="text-xs text-gray-500">{{ t("editor.slugDescription") }}</p>
      </div>

      <div v-if="templatesLoading" class="text-sm text-gray-600" data-testid="editor-templates-loading">
        {{ t("common.loading") }}
      </div>
      <div v-else-if="templatesError" class="text-sm text-red-600" data-testid="editor-templates-error">
        {{ templatesError }}
      </div>

      <button
        type="submit"
        :disabled="cvStore.saving"
        class="w-full sm:w-auto bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white font-semibold py-2 px-6 rounded-lg transition-colors"
        data-testid="editor-save-button"
      >
        {{ cvStore.saving ? t("editor.saving") : t("editor.saveMetadata") }}
      </button>
      <p v-if="saveMessage" class="text-green-600 text-sm" data-testid="editor-save-message">{{ saveMessage }}</p>
    </form>

    <p v-if="publicPath" class="mt-4 text-sm text-gray-600" data-testid="editor-public-path">
      {{ t("editor.publicLink", { url: publicPath }) }}
    </p>

    <div class="sections-editor mt-8">
      <h2 class="text-xl font-semibold text-gray-900 mb-4">{{ t("editor.sections") }}</h2>
      <div
        v-for="(section, index) in sectionsDraft"
        :key="section.id || index"
        class="section-editor bg-white border border-gray-200 rounded-lg p-4 mb-4"
        :data-testid="`section-editor-${section.sectionType}`"
      >
        <p class="section-editor__label font-semibold text-gray-900 mb-2">
          {{ t(`sections.${section.sectionType}.title`) }}
        </p>
        <template v-if="section.sectionType === 'summary'">
          <textarea
            v-model="section.content.text"
            rows="3"
            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
            data-testid="section-summary-input"
            @input="updateSectionField(index, (s) => ({ ...s, content: { ...s.content, text: section.content.text } }))"
          />
        </template>
        <template v-else-if="section.sectionType === 'skills'">
          <input
            :value="(section.content.items || []).join(', ')"
            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
            data-testid="section-skills-input"
            @input="
              (event) =>
                updateSectionField(index, (s) => ({
                  ...s,
                  content: { ...s.content, items: event.target.value.split(',').map((v) => v.trim()).filter(Boolean) },
                }))
            "
          />
        </template>
        <template v-else>
          <textarea
            :value="JSON.stringify(section.content)"
            rows="4"
            :class="[
              'w-full border rounded-lg px-3 py-2 font-mono text-sm focus:outline-none focus:ring-2',
              sectionErrors[index]
                ? 'border-red-300 focus:ring-red-500 focus:border-red-500'
                : 'border-gray-300 focus:ring-blue-500 focus:border-blue-500'
            ]"
            data-testid="section-generic-input"
            @input="(event) => updateSectionContentFromJson(index, event.target.value)"
          />
          <p v-if="sectionErrors[index]" class="text-red-600 text-xs mt-1" :data-testid="`section-error-${index}`">
            {{ sectionErrors[index] }}
          </p>
        </template>
      </div>
      <button
        type="button"
        :disabled="cvStore.saving"
        class="bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white font-semibold py-2 px-6 rounded-lg transition-colors"
        data-testid="editor-save-sections"
        @click="saveSections"
      >
        {{ cvStore.saving ? t('editor.saving') : t('editor.saveSections') }}
      </button>
      <p v-if="sectionsMessage" class="text-green-600 text-sm mt-2" data-testid="editor-sections-message">{{ sectionsMessage }}</p>
    </div>

    <div class="mt-8 pt-6 border-t border-gray-200">
      <button
        type="button"
        class="bg-green-600 hover:bg-green-700 text-white font-semibold py-2 px-6 rounded-lg transition-colors"
        data-testid="editor-export-button"
        @click="exportPdf"
      >
        {{ t("export.pdf") }}
      </button>
      <p v-if="exportMessage" class="text-green-600 text-sm mt-2" data-testid="editor-export-message">{{ exportMessage }}</p>
      <p v-if="exportError" class="text-red-600 text-sm mt-2" data-testid="editor-export-error">{{ exportError }}</p>
    </div>

    <div class="mt-8">
      <h2 class="text-xl font-semibold text-gray-900 mb-4">{{ t("editor.preview") }}</h2>
      <div class="bg-white border border-gray-200 rounded-lg p-4 overflow-auto">
        <CvTemplateRenderer
          :template-key="activeCv?.templateKey || form.templateKey || 'minimal'"
          :cv="activeCv"
          :sections="cvStore.sections"
          mode="editor-preview"
        />
      </div>
    </div>
  </section>
</template>
