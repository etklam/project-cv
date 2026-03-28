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
  <section data-testid="view-public-cv" class="mx-auto flex w-full max-w-7xl flex-col gap-6">
    <div v-if="loading" class="rounded-[32px] border border-slate-200 bg-white/90 px-6 py-16 text-center shadow-[0_24px_48px_rgba(15,23,42,0.06)] backdrop-blur">
      <div class="mx-auto h-12 w-12 animate-spin rounded-full border-2 border-slate-200 border-t-blue-600"></div>
      <p class="mt-4 text-sm font-medium text-slate-600">{{ t("common.loading") }}</p>
    </div>

    <div
      v-else-if="error"
      class="rounded-[32px] border border-rose-200 bg-rose-50 px-6 py-5 text-center text-sm text-rose-700"
      data-testid="public-cv-error"
    >
      {{ error }}
    </div>

    <template v-else-if="cv">
      <header class="grid gap-5 rounded-[32px] border border-slate-200 bg-white/90 p-6 shadow-[0_24px_48px_rgba(15,23,42,0.06)] backdrop-blur lg:grid-cols-[1.2fr_0.8fr] lg:p-8">
        <div class="space-y-4">
          <span class="inline-flex w-fit items-center rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold uppercase tracking-[0.22em] text-blue-700">
            Public resume
          </span>
          <div class="space-y-2">
            <h1 class="text-4xl font-semibold tracking-tight text-slate-950 sm:text-5xl" data-testid="public-cv-title">
              {{ cv.title || slug }}
            </h1>
            <p class="text-sm leading-7 text-slate-600" data-testid="public-cv-owner">
              @{{ user?.username || username }}
            </p>
          </div>
        </div>

        <div class="grid gap-3 sm:grid-cols-3 lg:grid-cols-1">
          <div class="rounded-3xl border border-slate-200 bg-slate-50 px-5 py-4">
            <p class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-500">Template</p>
            <strong class="mt-2 block truncate text-sm font-semibold text-slate-950">{{ cv.templateKey || "minimal" }}</strong>
          </div>
          <div class="rounded-3xl border border-slate-200 bg-slate-50 px-5 py-4">
            <p class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-500">Route</p>
            <strong class="mt-2 block truncate text-sm font-semibold text-slate-900">/u/{{ user?.username || username }}/{{ slug }}</strong>
          </div>
        </div>
      </header>

      <div class="grid gap-6 lg:grid-cols-[minmax(0,1fr)_320px]">
        <div class="rounded-[32px] border border-slate-200 bg-white/95 p-4 shadow-[0_24px_48px_rgba(15,23,42,0.06)] backdrop-blur sm:p-6 lg:p-8">
          <CvTemplateRenderer
            :template-key="cv.templateKey || 'minimal'"
            :cv="cv"
            :sections="sections"
            mode="public"
          />
        </div>

        <aside class="space-y-4">
          <div class="rounded-[28px] border border-slate-200 bg-white/90 p-5 shadow-[0_24px_48px_rgba(15,23,42,0.06)] backdrop-blur">
            <p class="text-[11px] font-semibold uppercase tracking-[0.24em] text-slate-400">Public route</p>
            <p class="mt-3 text-sm leading-7 text-slate-600">
              This resume is available for public sharing and uses the selected template above.
            </p>
          </div>

          <div class="rounded-[28px] border border-slate-200 bg-slate-950 p-5 text-white shadow-[0_24px_48px_rgba(15,23,42,0.08)]">
            <p class="text-[11px] font-semibold uppercase tracking-[0.24em] text-blue-200">Owner</p>
            <p class="mt-3 text-sm leading-7 text-white/75">
              @{{ user?.username || username }}
            </p>
          </div>
        </aside>
      </div>
    </template>

    <div
      v-else
      class="rounded-[32px] border border-dashed border-slate-300 bg-slate-50 px-6 py-10 text-center text-sm text-slate-500"
    >
      <p data-testid="public-cv-empty">{{ t("public.cvTitle") }} {{ t("public.notFound") }}</p>
    </div>
  </section>
</template>
