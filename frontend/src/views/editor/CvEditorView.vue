<script setup>
import { computed, nextTick, onMounted, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import EditorFormPane from "@/components/editor/EditorFormPane.vue";
import EditorPreviewPane from "@/components/editor/EditorPreviewPane.vue";
import EditorStylePane from "@/components/editor/EditorStylePane.vue";
import EditorToolbar from "@/components/editor/EditorToolbar.vue";
import ContactEditor from "@/components/editor/sections/ContactEditor.vue";
import CustomSectionEditor from "@/components/editor/sections/CustomSectionEditor.vue";
import EducationEditor from "@/components/editor/sections/EducationEditor.vue";
import ExperienceEditor from "@/components/editor/sections/ExperienceEditor.vue";
import SkillsEditor from "@/components/editor/sections/SkillsEditor.vue";
import SummaryEditor from "@/components/editor/sections/SummaryEditor.vue";
import {
  buildMetadataPayload,
  buildPreviewCv,
  buildSectionsPayload,
  createEmptyDraft,
  normalizeCvToDraft,
} from "@/components/editor/editorDraftAdapters";
import { useEditorAutosave } from "@/composables/useEditorAutosave";
import { useTemplateCatalog } from "@/composables/useTemplateCatalog";
import { exportCvPdf } from "@/api/export";
import { useCvStore } from "@/stores/cv";

const { t } = useI18n();
const route = useRoute();
const cvStore = useCvStore();
const cvId = computed(() => route?.params?.id || "");

const loadError = ref("");
const saveMessage = ref("");
const sectionsMessage = ref("");
const exportMessage = ref("");
const exportError = ref("");
const activeTab = ref("preview");
const previewZoom = ref(85);
const editorDraft = ref(createEmptyDraft());
const hydratingDraft = ref(true);
const previewAppearance = ref({
  surface: "studio",
  accent: "ink",
  density: "comfortable",
});

const {
  supportedTemplates: templates,
  loading: templatesLoading,
  error: templatesError,
  loadTemplates: loadTemplateOptions,
} = useTemplateCatalog();

const autosave = useEditorAutosave(async () => {
  await saveDraft({ silent: true });
}, { delay: 800 });

function hydrateDraft(cv, sections) {
  hydratingDraft.value = true;
  editorDraft.value = normalizeCvToDraft(cv, sections);
  previewZoom.value = 85;
  saveMessage.value = "";
  sectionsMessage.value = "";
  loadError.value = "";
}

function resetContact() {
  const normalized = normalizeCvToDraft(cvStore.currentCv, cvStore.sections);
  editorDraft.value.contact = normalized.contact;
}

const activeCv = computed(() => buildPreviewCv(editorDraft.value, cvStore.currentCv || {}));
const previewSections = computed(() => buildSectionsPayload(editorDraft.value).sections);
const publicPath = computed(() => {
  if (activeCv.value?.isPublic && activeCv.value?.slug) {
    const username = cvStore.currentCv?.username || "{username}";
    return `/u/${username}/${activeCv.value.slug}`;
  }
  return "";
});
const draftCount = computed(() => previewSections.value.length);
const autosaveLabel = computed(() => {
  if (autosave.saving.value) {
    return "Autosaving...";
  }
  if (autosave.error.value) {
    return autosave.error.value;
  }
  if (autosave.lastSavedAt.value) {
    return "Autosaved just now";
  }
  return "Ready to edit";
});

const loadCvMetadata = async () => {
  loadError.value = "";
  if (!cvId.value) {
    loadError.value = t("editor.cvNotFound");
    return;
  }

  try {
    await cvStore.loadCv(cvId.value);
    hydrateDraft(cvStore.currentCv, cvStore.sections);
    await nextTick();
    autosave.lastSavedAt.value = new Date().toISOString();
  } catch (requestError) {
    loadError.value =
      requestError?.response?.data?.message || requestError?.message || t("editor.error");
  } finally {
    hydratingDraft.value = false;
  }
};

async function saveMetadata(options = {}) {
  const payload = buildMetadataPayload(editorDraft.value);
  saveMessage.value = "";
  loadError.value = "";

  if (!payload.title) {
    loadError.value = t("editor.validation.titleRequired");
    throw new Error(loadError.value);
  }

  if (payload.isPublic && !payload.slug) {
    loadError.value = t("editor.validation.publicSlugRequired");
    throw new Error(loadError.value);
  }

  await cvStore.updateMetadata(cvId.value, payload);

  if (!options.silent) {
    saveMessage.value = t("editor.saved");
  }
}

async function saveSections(options = {}) {
  sectionsMessage.value = "";
  loadError.value = "";
  await cvStore.updateSections(cvId.value, buildSectionsPayload(editorDraft.value));
  if (!options.silent) {
    sectionsMessage.value = t("editor.sectionsSaved");
  }
}

async function saveDraft(options = {}) {
  loadError.value = "";
  const payload = {
    ...buildMetadataPayload(editorDraft.value),
    sections: buildSectionsPayload(editorDraft.value).sections,
  };

  if (!payload.title) {
    loadError.value = t("editor.validation.titleRequired");
    throw new Error(loadError.value);
  }

  if (payload.isPublic && !payload.slug) {
    loadError.value = t("editor.validation.publicSlugRequired");
    throw new Error(loadError.value);
  }

  await cvStore.saveDraft(cvId.value, payload);

  if (!options.silent) {
    saveMessage.value = t("editor.saved");
    sectionsMessage.value = t("editor.sectionsSaved");
  }
}

async function exportPdf() {
  exportMessage.value = "";
  exportError.value = "";
  try {
    await exportCvPdf(cvId.value);
    exportMessage.value = t("export.success");
  } catch (error) {
    exportError.value =
      error?.response?.data?.message || error?.message || t("export.failed");
  }
}

function zoomIn() {
  previewZoom.value = Math.min(previewZoom.value + 5, 125);
}

function zoomOut() {
  previewZoom.value = Math.max(previewZoom.value - 5, 50);
}

watch(editorDraft, () => {
  if (hydratingDraft.value || !cvId.value) {
    return;
  }
  autosave.queueAutosave();
}, { deep: true });

onMounted(() => {
  loadCvMetadata();
  loadTemplateOptions();
});
</script>

<template>
  <section
    data-testid="view-editor"
    class="mx-auto max-w-[1600px] px-4 py-6 text-on-background sm:px-6 lg:px-8 lg:py-8"
  >
    <div class="space-y-6">
      <EditorToolbar
        :document-title="editorDraft.metadata.title"
        :autosave-label="autosaveLabel"
        :active-tab="activeTab"
        :is-public="editorDraft.metadata.isPublic"
        :export-running="false"
        @update:active-tab="activeTab = $event"
        @export="exportPdf"
        @publish="saveDraft"
      />

      <div
        v-if="cvStore.loading"
        data-testid="editor-loading"
        class="rounded-[2rem] border border-gray-200 bg-white px-6 py-16 text-center"
      >
        <div class="inline-block h-12 w-12 animate-spin rounded-full border-2 border-[#18181B] border-t-transparent"></div>
        <p class="mt-4 text-sm text-gray-600">{{ t("editor.loading") }}</p>
      </div>

      <div
        v-else-if="loadError"
        class="rounded-[2rem] border border-red-200 bg-red-50 px-5 py-4 text-red-800"
        data-testid="editor-error"
      >
        {{ loadError }}
      </div>

      <div
        v-else
        class="grid min-h-[calc(100vh-12rem)] gap-6 xl:grid-cols-[minmax(0,1.02fr)_minmax(540px,0.98fr)]"
      >
        <EditorFormPane>
          <div class="space-y-8">
            <header class="flex flex-col gap-3 border-b border-slate-100 pb-6 sm:flex-row sm:items-end sm:justify-between">
              <div>
                <p class="text-[11px] font-bold uppercase tracking-[0.28em] text-slate-500">
                  Editor canvas
                </p>
                <h2 class="mt-2 font-headline text-3xl font-extrabold tracking-tight text-on-surface">
                  Build the narrative
                </h2>
              </div>
              <div class="rounded-[24px] border border-slate-200 bg-surface-container-low px-4 py-3 text-sm text-slate-600">
                {{ draftCount }} live sections
              </div>
            </header>

            <EditorStylePane
              v-if="activeTab === 'style'"
              :templates="templates"
              :loading="templatesLoading"
              :error="templatesError"
              :metadata="editorDraft.metadata"
              :appearance="previewAppearance"
              @update:template-key="editorDraft.metadata.templateKey = $event"
              @update:appearance="previewAppearance = $event"
            />

            <form
              v-else
              data-testid="editor-form"
              class="space-y-6"
              @submit.prevent="saveMetadata"
            >
              <section class="space-y-4 rounded-[28px] border border-slate-200 bg-surface-container-lowest p-5">
                <div class="flex items-start justify-between gap-4">
                  <div>
                    <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-slate-500">
                      Document settings
                    </p>
                    <h3 class="mt-1 font-headline text-lg font-extrabold tracking-tight text-on-surface">
                      Metadata
                    </h3>
                  </div>
                  <span class="rounded-full bg-primary-fixed px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] text-on-primary-fixed-variant">
                    Workspace
                  </span>
                </div>

                <div class="space-y-2">
                  <label class="block text-sm font-medium text-slate-700">
                    {{ t("editor.title") }}
                  </label>
                  <input
                    v-model="editorDraft.metadata.title"
                    data-testid="editor-title-input"
                    class="h-12 w-full rounded-[24px] border border-slate-200 bg-surface-container-low px-4 text-sm text-on-surface focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/10"
                  />
                </div>

                <div class="grid gap-4 md:grid-cols-2">
                  <div class="space-y-2">
                    <label class="block text-sm font-medium text-slate-700">
                      {{ t("editor.slug") }}
                    </label>
                    <input
                      v-model="editorDraft.metadata.slug"
                      :disabled="!editorDraft.metadata.isPublic"
                      data-testid="editor-slug-input"
                      class="h-12 w-full rounded-[24px] border border-slate-200 bg-surface-container-low px-4 text-sm text-on-surface focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/10 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
                    />
                  </div>
                </div>

                <div class="flex flex-col gap-3 rounded-[24px] border border-slate-200 bg-white px-4 py-4 sm:flex-row sm:items-center sm:justify-between">
                  <label class="inline-flex items-center gap-3 text-sm font-medium text-slate-700">
                    <input
                      v-model="editorDraft.metadata.isPublic"
                      type="checkbox"
                      class="h-4 w-4 rounded border-slate-300 text-primary focus:ring-primary/20"
                      data-testid="editor-public-toggle"
                    />
                    {{ t("editor.isPublic") }}
                  </label>
                  <p class="text-xs text-slate-500">{{ t("editor.isPublicDescription") }}</p>
                </div>

                <div class="flex flex-col gap-3 border-t border-slate-100 pt-5 sm:flex-row sm:items-center sm:justify-between">
                  <button
                    type="submit"
                    class="inline-flex h-11 items-center justify-center rounded-full bg-on-surface px-6 text-sm font-semibold text-white transition hover:bg-slate-700"
                    data-testid="editor-save-button"
                  >
                    {{ t("editor.saveMetadata") }}
                  </button>
                  <p v-if="saveMessage" data-testid="editor-save-message" class="text-sm font-medium text-emerald-600">
                    {{ saveMessage }}
                  </p>
                </div>
              </section>
            </form>

            <div class="space-y-6 rounded-[28px] border border-slate-200 bg-surface-container-lowest p-5">
              <ContactEditor
                :model-value="editorDraft.contact.content"
                :saving="cvStore.saving"
                @update:model-value="editorDraft.contact.content = $event"
                @submit="saveSections"
                @reset="resetContact"
              />

              <SummaryEditor
                :model-value="editorDraft.summary.content.text"
                @update:model-value="editorDraft.summary.content.text = $event"
              />

              <ExperienceEditor
                :items="editorDraft.experience.content.items"
                @update:items="editorDraft.experience.content.items = $event"
              />

              <EducationEditor
                :items="editorDraft.education.content.items"
                @update:items="editorDraft.education.content.items = $event"
              />

              <SkillsEditor
                :items="editorDraft.skills.content.items"
                @update:items="editorDraft.skills.content.items = $event"
              />

              <CustomSectionEditor
                :sections="editorDraft.customSections"
                @update:sections="editorDraft.customSections = $event"
              />

              <div class="flex flex-col gap-3 border-t border-slate-100 pt-5 sm:flex-row sm:items-center sm:justify-between">
                <button
                  type="button"
                  class="inline-flex h-11 items-center justify-center rounded-full bg-primary px-6 text-sm font-semibold text-white transition hover:opacity-90"
                  data-testid="editor-save-sections"
                  @click="saveSections"
                >
                  {{ t("editor.saveSections") }}
                </button>
                <p v-if="sectionsMessage" data-testid="editor-sections-message" class="text-sm font-medium text-emerald-600">
                  {{ sectionsMessage }}
                </p>
              </div>
            </div>
          </div>
        </EditorFormPane>

        <div class="xl:sticky xl:top-24 xl:self-start">
          <EditorPreviewPane
            :active-tab="activeTab"
            :is-public="activeCv.isPublic"
            :public-path="publicPath"
            :zoom="previewZoom"
            :cv="activeCv"
            :sections="previewSections"
            :appearance="previewAppearance"
            :export-message="exportMessage"
            :export-error="exportError"
            @zoom-in="zoomIn"
            @zoom-out="zoomOut"
            @export="exportPdf"
          />
        </div>
      </div>
    </div>
  </section>
</template>
