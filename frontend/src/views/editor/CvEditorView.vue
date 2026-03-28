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

const draftCount = computed(() => sectionsDraft.value.length);
</script>

<template>
  <section
    data-testid="view-editor"
    class="mx-auto max-w-7xl px-4 py-6 text-[#09090B] sm:px-6 lg:px-8 lg:py-10"
  >
    <div class="space-y-6">
      <header class="rounded-[2rem] border border-gray-200 bg-white px-5 py-5 sm:px-6 lg:px-8">
        <div class="flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
          <div class="space-y-3">
            <p class="text-xs font-semibold uppercase tracking-[0.28em] text-gray-400">
              Editor workspace
            </p>
            <div>
              <h1 class="text-3xl font-semibold tracking-tight text-[#18181B] sm:text-4xl">
                {{ t("editor.title") }}
              </h1>
              <p class="mt-3 max-w-2xl text-sm leading-7 text-gray-600 sm:text-base">
                {{ t("editor.metadata") }}
              </p>
            </div>
          </div>

          <div class="flex flex-wrap gap-3">
            <div class="rounded-2xl border border-gray-200 bg-[#FAFAFA] px-4 py-3">
              <p class="text-xs font-medium uppercase tracking-[0.18em] text-gray-400">
                {{ t("editor.sections") }}
              </p>
              <p class="mt-1 text-lg font-semibold text-[#18181B]">{{ draftCount }}</p>
            </div>
            <div class="rounded-2xl border border-gray-200 bg-[#FAFAFA] px-4 py-3">
              <p class="text-xs font-medium uppercase tracking-[0.18em] text-gray-400">
                {{ t("editor.template") }}
              </p>
              <p class="mt-1 text-lg font-semibold text-[#18181B]">
                {{ form.templateKey || "minimal" }}
              </p>
            </div>
          </div>
        </div>
      </header>

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

      <div v-else class="grid gap-6 xl:grid-cols-[minmax(0,1.08fr)_minmax(22rem,0.92fr)]">
        <div class="space-y-6">
          <form
            v-if="!loadError"
            class="space-y-6"
            data-testid="editor-form"
            @submit.prevent="saveMetadata"
          >
            <section class="rounded-[2rem] border border-gray-200 bg-white p-5 sm:p-6">
              <div class="flex items-start justify-between gap-4">
                <div>
                  <p class="text-xs font-semibold uppercase tracking-[0.24em] text-gray-400">
                    Document settings
                  </p>
                  <h2 class="mt-2 text-xl font-semibold tracking-tight text-[#18181B]">
                    {{ t("editor.metadata") }}
                  </h2>
                </div>
                <span
                  class="rounded-full border border-[#18181B]/10 bg-[#FAFAFA] px-3 py-1 text-xs font-semibold text-[#18181B]"
                >
                  Draft
                </span>
              </div>

              <div class="mt-6 grid gap-5">
                <div class="space-y-2">
                  <label class="block text-sm font-medium text-gray-700">
                    {{ t("editor.title") }}
                    <span class="text-[#EC4899]">*</span>
                  </label>
                  <input
                    v-model="form.title"
                    class="h-11 w-full rounded-2xl border border-gray-200 bg-white px-4 text-sm text-[#18181B] placeholder:text-gray-400 focus:border-[#18181B] focus:outline-none focus:ring-2 focus:ring-[#18181B]/10"
                    data-testid="editor-title-input"
                  />
                </div>

                <div class="grid gap-5 sm:grid-cols-2">
                  <div class="space-y-2">
                    <label class="block text-sm font-medium text-gray-700">
                      {{ t("editor.template") }}
                    </label>
                    <select
                      v-model="form.templateKey"
                      class="h-11 w-full rounded-2xl border border-gray-200 bg-white px-4 text-sm text-[#18181B] focus:border-[#18181B] focus:outline-none focus:ring-2 focus:ring-[#18181B]/10"
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

                  <div class="space-y-2">
                    <label class="block text-sm font-medium text-gray-700">
                      {{ t("editor.slug") }}
                    </label>
                    <input
                      v-model="form.slug"
                      :disabled="!form.isPublic"
                      class="h-11 w-full rounded-2xl border border-gray-200 bg-white px-4 text-sm text-[#18181B] placeholder:text-gray-400 focus:border-[#18181B] focus:outline-none focus:ring-2 focus:ring-[#18181B]/10 disabled:cursor-not-allowed disabled:bg-gray-100 disabled:text-gray-400"
                      data-testid="editor-slug-input"
                      @blur="form.slug = normalizeSlug(form.slug)"
                    />
                  </div>
                </div>

                <div class="flex flex-col gap-3 rounded-2xl border border-gray-200 bg-[#FAFAFA] p-4 sm:flex-row sm:items-center sm:justify-between">
                  <div class="flex items-center gap-3">
                    <input
                      v-model="form.isPublic"
                      type="checkbox"
                      id="is-public"
                      class="h-4 w-4 rounded border-gray-300 text-[#18181B] focus:ring-[#18181B]/20"
                      data-testid="editor-public-toggle"
                    />
                    <label for="is-public" class="cursor-pointer text-sm font-medium text-gray-700">
                      {{ t("editor.isPublic") }}
                    </label>
                  </div>
                  <p class="text-xs text-gray-500">{{ t("editor.isPublicDescription") }}</p>
                </div>

                <p class="text-xs leading-6 text-gray-500">{{ t("editor.slugDescription") }}</p>

                <div v-if="templatesLoading" class="text-sm text-gray-600" data-testid="editor-templates-loading">
                  {{ t("common.loading") }}
                </div>
                <div v-else-if="templatesError" class="text-sm text-red-600" data-testid="editor-templates-error">
                  {{ templatesError }}
                </div>

                <div class="flex flex-col gap-3 border-t border-gray-100 pt-5 sm:flex-row sm:items-center sm:justify-between">
                  <button
                    type="submit"
                    :disabled="cvStore.saving"
                    class="inline-flex h-11 items-center justify-center rounded-full bg-[#18181B] px-6 text-sm font-semibold text-white transition-colors hover:bg-[#3F3F46] disabled:cursor-not-allowed disabled:opacity-50"
                    data-testid="editor-save-button"
                  >
                    {{ cvStore.saving ? t("editor.saving") : t("editor.saveMetadata") }}
                  </button>
                  <p v-if="saveMessage" class="text-sm font-medium text-emerald-600" data-testid="editor-save-message">
                    {{ saveMessage }}
                  </p>
                </div>
              </div>
            </section>
          </form>

          <section class="rounded-[2rem] border border-gray-200 bg-white p-5 sm:p-6">
            <div class="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
              <div>
                <p class="text-xs font-semibold uppercase tracking-[0.24em] text-gray-400">
                  Section editor
                </p>
                <h2 class="mt-2 text-xl font-semibold tracking-tight text-[#18181B]">
                  {{ t("editor.sections") }}
                </h2>
              </div>
              <p class="text-sm text-gray-500">{{ t("common.optional") }}</p>
            </div>

            <div class="mt-6 space-y-4">
              <div
                v-for="(section, index) in sectionsDraft"
                :key="section.id || index"
                class="rounded-2xl border border-gray-200 bg-[#FAFAFA] p-4 sm:p-5"
                :data-testid="`section-editor-${section.sectionType}`"
              >
                <div class="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
                  <p class="font-semibold tracking-tight text-[#18181B]">
                    {{ t(`sections.${section.sectionType}.title`) }}
                  </p>
                  <span class="text-xs uppercase tracking-[0.18em] text-gray-400">
                    {{ section.sectionType }}
                  </span>
                </div>

                <div class="mt-4">
                  <template v-if="section.sectionType === 'summary'">
                    <textarea
                      v-model="section.content.text"
                      rows="3"
                      class="w-full rounded-2xl border border-gray-200 bg-white px-4 py-3 text-sm text-[#18181B] placeholder:text-gray-400 focus:border-[#18181B] focus:outline-none focus:ring-2 focus:ring-[#18181B]/10"
                      data-testid="section-summary-input"
                      @input="updateSectionField(index, (s) => ({ ...s, content: { ...s.content, text: section.content.text } }))"
                    />
                  </template>
                  <template v-else-if="section.sectionType === 'skills'">
                    <input
                      :value="(section.content.items || []).join(', ')"
                      class="h-11 w-full rounded-2xl border border-gray-200 bg-white px-4 text-sm text-[#18181B] placeholder:text-gray-400 focus:border-[#18181B] focus:outline-none focus:ring-2 focus:ring-[#18181B]/10"
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
                        'w-full rounded-2xl border bg-white px-4 py-3 font-mono text-sm text-[#18181B] placeholder:text-gray-400 focus:outline-none focus:ring-2',
                        sectionErrors[index]
                          ? 'border-red-300 focus:border-red-500 focus:ring-red-100'
                          : 'border-gray-200 focus:border-[#18181B] focus:ring-[#18181B]/10',
                      ]"
                      data-testid="section-generic-input"
                      @input="(event) => updateSectionContentFromJson(index, event.target.value)"
                    />
                    <p v-if="sectionErrors[index]" class="mt-2 text-xs font-medium text-red-600" :data-testid="`section-error-${index}`">
                      {{ sectionErrors[index] }}
                    </p>
                  </template>
                </div>
              </div>
            </div>

            <div class="mt-5 flex flex-col gap-3 border-t border-gray-100 pt-5 sm:flex-row sm:items-center sm:justify-between">
              <button
                type="button"
                :disabled="cvStore.saving"
                class="inline-flex h-11 items-center justify-center rounded-full bg-[#EC4899] px-6 text-sm font-semibold text-white transition-colors hover:bg-[#BE185D] disabled:cursor-not-allowed disabled:opacity-50"
                data-testid="editor-save-sections"
                @click="saveSections"
              >
                {{ cvStore.saving ? t("editor.saving") : t("editor.saveSections") }}
              </button>
              <p v-if="sectionsMessage" class="text-sm font-medium text-emerald-600" data-testid="editor-sections-message">
                {{ sectionsMessage }}
              </p>
            </div>
          </section>

          <section class="rounded-[2rem] border border-gray-200 bg-white p-5 sm:p-6">
            <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <p class="text-xs font-semibold uppercase tracking-[0.24em] text-gray-400">
                  Export
                </p>
                <h2 class="mt-2 text-xl font-semibold tracking-tight text-[#18181B]">
                  {{ t("export.title") }}
                </h2>
              </div>
              <button
                type="button"
                class="inline-flex h-11 items-center justify-center rounded-full border border-gray-200 bg-white px-6 text-sm font-semibold text-[#18181B] transition-colors hover:border-gray-300 hover:bg-gray-50"
                data-testid="editor-export-button"
                @click="exportPdf"
              >
                {{ t("export.pdf") }}
              </button>
            </div>
            <p v-if="exportMessage" class="mt-4 text-sm font-medium text-emerald-600" data-testid="editor-export-message">
              {{ exportMessage }}
            </p>
            <p v-if="exportError" class="mt-4 text-sm font-medium text-red-600" data-testid="editor-export-error">
              {{ exportError }}
            </p>
          </section>
        </div>

        <aside class="space-y-6 xl:sticky xl:top-6 self-start">
          <section class="rounded-[2rem] border border-gray-200 bg-white p-5 sm:p-6">
            <div class="flex items-start justify-between gap-4">
              <div>
                <p class="text-xs font-semibold uppercase tracking-[0.24em] text-gray-400">
                  Live link
                </p>
                <h2 class="mt-2 text-xl font-semibold tracking-tight text-[#18181B]">
                  {{ t("editor.publicLink", { url: publicPath || "—" }) }}
                </h2>
              </div>
              <span
                :class="[
                  'rounded-full px-3 py-1 text-xs font-semibold',
                  activeCv?.isPublic ? 'bg-emerald-50 text-emerald-700' : 'bg-gray-100 text-gray-500',
                ]"
              >
                {{ activeCv?.isPublic ? t("editor.isPublic") : "Private" }}
              </span>
            </div>

            <p v-if="publicPath" class="mt-4 break-all text-sm leading-6 text-gray-600" data-testid="editor-public-path">
              {{ t("editor.publicLink", { url: publicPath }) }}
            </p>
            <p v-else class="mt-4 text-sm text-gray-500">
              {{ t("editor.slugHint") }}
            </p>
          </section>

          <section class="overflow-hidden rounded-[2rem] border border-gray-200 bg-white">
            <div class="border-b border-gray-100 px-5 py-4 sm:px-6">
              <p class="text-xs font-semibold uppercase tracking-[0.24em] text-gray-400">
                Preview
              </p>
              <h2 class="mt-2 text-xl font-semibold tracking-tight text-[#18181B]">
                {{ t("editor.preview") }}
              </h2>
            </div>
            <div class="p-4 sm:p-6">
              <div class="overflow-auto rounded-[1.5rem] border border-gray-100 bg-[#FAFAFA] p-3 sm:p-4">
                <CvTemplateRenderer
                  :template-key="activeCv?.templateKey || form.templateKey || 'minimal'"
                  :cv="activeCv"
                  :sections="cvStore.sections"
                  mode="editor-preview"
                />
              </div>
            </div>
          </section>
        </aside>
      </div>
    </div>
  </section>
</template>
