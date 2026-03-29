<script setup>
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { getPublicProfile } from "@/api/public";

const { t } = useI18n();
const route = useRoute();
const email = route?.params?.email || "";

const loading = ref(true);
const error = ref("");
const user = ref(null);
const cvs = ref([]);

onMounted(async () => {
  try {
    const result = await getPublicProfile(email);
    user.value = result.user || null;
    cvs.value = result.cvs || [];
  } catch (requestError) {
    error.value =
      requestError?.response?.data?.message || requestError?.message || t("errors.general");
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <section data-testid="view-public-profile" class="mx-auto flex w-full max-w-7xl flex-col gap-6">
    <div v-if="loading" class="rounded-[32px] border border-slate-200 bg-white/90 px-6 py-16 text-center shadow-[0_24px_48px_rgba(15,23,42,0.06)] backdrop-blur">
      <div class="mx-auto h-12 w-12 animate-spin rounded-full border-2 border-slate-200 border-t-blue-600"></div>
      <p class="mt-4 text-sm font-medium text-slate-600">{{ t("common.loading") }}</p>
    </div>

    <div
      v-else-if="error"
      class="rounded-[32px] border border-rose-200 bg-rose-50 px-6 py-5 text-center text-sm text-rose-700"
      data-testid="public-profile-error"
    >
      {{ error }}
    </div>

    <template v-else>
      <header class="grid gap-5 rounded-[32px] border border-slate-200 bg-white/90 p-6 shadow-[0_24px_48px_rgba(15,23,42,0.06)] backdrop-blur lg:grid-cols-[1.2fr_0.8fr] lg:p-8">
        <div class="space-y-4">
          <span class="inline-flex w-fit items-center rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold uppercase tracking-[0.22em] text-blue-700">
            Public profile
          </span>
          <div class="space-y-2">
            <h1 class="text-4xl font-semibold tracking-tight text-slate-950 sm:text-5xl" data-testid="public-profile-name">
              {{ user?.displayName || email }}
            </h1>
            <p class="text-sm leading-7 text-slate-600" data-testid="public-profile-email">
              {{ user?.email || email }}
            </p>
          </div>
        </div>

        <div class="grid gap-3 sm:grid-cols-3 lg:grid-cols-1">
          <div class="rounded-3xl border border-slate-200 bg-slate-50 px-5 py-4">
            <p class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-500">Published resumes</p>
            <strong class="mt-2 block text-3xl font-bold tracking-[-0.04em] text-slate-950">{{ cvs.length }}</strong>
          </div>
          <div class="rounded-3xl border border-slate-200 bg-slate-50 px-5 py-4">
            <p class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-500">Profile route</p>
            <strong class="mt-2 block truncate text-sm font-semibold text-slate-900">/u/{{ user?.email || email }}</strong>
          </div>
        </div>
      </header>

      <div class="grid gap-6 lg:grid-cols-[minmax(0,1fr)_320px]">
        <section class="rounded-[32px] border border-slate-200 bg-white/90 p-6 shadow-[0_24px_48px_rgba(15,23,42,0.06)] backdrop-blur lg:p-8">
          <div class="mb-6 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <span class="inline-flex w-fit items-center rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold uppercase tracking-[0.22em] text-blue-700">
                Resume library
              </span>
              <h2 class="mt-3 text-3xl font-semibold tracking-tight text-slate-950">
                {{ t("dashboard.myCvs") }}
              </h2>
            </div>
            <p class="max-w-xl text-sm leading-7 text-slate-600">
              Publicly accessible resume variants for this profile.
            </p>
          </div>

          <ul v-if="cvs.length" class="grid gap-3" data-testid="public-cv-list">
            <li
              v-for="cv in cvs"
              :key="cv.slug || cv.id"
              class="rounded-3xl border border-slate-200 bg-slate-50 transition hover:border-blue-200 hover:bg-blue-50/40"
            >
              <RouterLink
                :to="`/u/${user?.email || email}/${cv.slug}`"
                class="flex items-center justify-between gap-4 px-5 py-5"
                data-testid="public-cv-link"
              >
                <div class="min-w-0">
                  <p class="truncate text-lg font-semibold text-slate-950">{{ cv.title || cv.slug }}</p>
                  <p class="mt-1 text-sm text-slate-500">
                    {{ t("templates." + cv.templateKey + ".name", cv.templateKey) }}
                  </p>
                </div>
                <span class="inline-flex h-11 w-11 flex-none items-center justify-center rounded-full border border-slate-200 bg-white text-slate-500">
                  <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                  </svg>
                </span>
              </RouterLink>
            </li>
          </ul>

          <p
            v-else
            class="rounded-3xl border border-dashed border-slate-300 bg-slate-50 px-4 py-10 text-center text-sm text-slate-500"
            data-testid="public-cv-empty"
          >
            {{ t("public.noCvs") }}
          </p>
        </section>

        <aside class="space-y-4">
          <div class="rounded-[28px] border border-slate-200 bg-white/90 p-5 shadow-[0_24px_48px_rgba(15,23,42,0.06)] backdrop-blur">
            <p class="text-[11px] font-semibold uppercase tracking-[0.24em] text-slate-400">Profile summary</p>
            <p class="mt-3 text-2xl font-semibold tracking-tight text-slate-950">
              {{ cvs.length }} resumes
            </p>
            <p class="mt-3 text-sm leading-7 text-slate-600">
              This profile uses a clean public showcase with direct links to each resume variant.
            </p>
          </div>

          <div class="rounded-[28px] border border-slate-200 bg-slate-950 p-5 text-white shadow-[0_24px_48px_rgba(15,23,42,0.08)]">
            <p class="text-[11px] font-semibold uppercase tracking-[0.24em] text-blue-200">Public route</p>
            <p class="mt-3 text-sm leading-7 text-white/75">
              /u/{{ user?.email || email }}
            </p>
          </div>
        </aside>
      </div>
    </template>
  </section>
</template>
