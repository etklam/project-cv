<script setup>
import { computed, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import TemplateCard from "@/components/templates/TemplateCard.vue";
import { createCv, deleteCv, listCvs } from "@/api/cv";
import { useRewardCenter } from "@/composables/useRewardCenter";
import { useTemplateCatalog } from "@/composables/useTemplateCatalog";

const { t } = useI18n();

const {
  summary,
  showBalance,
  inviteStats,
  inviteRedemption,
  promoRedemptionsCount,
  recentRedemptions,
  loading,
  error,
  redeemStatus,
  redeemMessage,
  loadSummary,
  redeem,
} = useRewardCenter();

const inputCode = ref("");
const myCvs = ref([]);
const cvsLoading = ref(false);
const cvsError = ref("");
const actionMessage = ref("");
const actionError = ref("");
const creating = ref(false);
const deletingId = ref(null);
const deleteConfirmId = ref(null);
const deleteConfirmTitle = ref("");

const recentCvCount = computed(() => myCvs.value.length);

const handleRedeem = async () => {
  const success = await redeem(inputCode.value);
  if (success) {
    inputCode.value = "";
    await loadMyCvs();
  }
};

const loadMyCvs = async () => {
  cvsLoading.value = true;
  cvsError.value = "";
  try {
    const payload = await listCvs();
    myCvs.value = payload?.cvs || [];
  } catch (requestError) {
    cvsError.value =
      requestError?.response?.data?.message || requestError?.message || t("errors.general");
  } finally {
    cvsLoading.value = false;
  }
};

const handleCreateCv = async () => {
  creating.value = true;
  actionMessage.value = "";
  actionError.value = "";
  try {
    const payload = await createCv({ title: "New CV", templateKey: "minimal" });
    myCvs.value = [...(myCvs.value || []), payload?.cv || payload];
    actionMessage.value = t("dashboard.cvCreated");
  } catch (requestError) {
    actionError.value =
      requestError?.response?.data?.message || requestError?.message || t("dashboard.cvCreateError");
  } finally {
    creating.value = false;
  }
};

const confirmDelete = (cv) => {
  deleteConfirmId.value = cv.id;
  deleteConfirmTitle.value = cv.title;
};

const cancelDelete = () => {
  deleteConfirmId.value = null;
  deleteConfirmTitle.value = "";
};

const handleDeleteCv = async () => {
  if (!deleteConfirmId.value) return;

  deletingId.value = deleteConfirmId.value;
  actionMessage.value = "";
  actionError.value = "";

  try {
    await deleteCv(deleteConfirmId.value);
    myCvs.value = myCvs.value.filter((cv) => cv.id !== deleteConfirmId.value);
    actionMessage.value = t("dashboard.cvDeleted");
  } catch (requestError) {
    actionError.value =
      requestError?.response?.data?.message || requestError?.message || t("dashboard.cvDeleteError");
  } finally {
    deletingId.value = null;
    deleteConfirmId.value = null;
    deleteConfirmTitle.value = "";
  }
};

const {
  visibleTemplates,
  loading: templateLoading,
  error: templateError,
  loadTemplates,
} = useTemplateCatalog({
  errorMessage: t("errors.general"),
  limit: 3,
});

onMounted(() => {
  loadSummary();
  loadTemplates();
  loadMyCvs();
});
</script>

<template>
  <section data-testid="view-dashboard" class="mx-auto flex w-full max-w-[1720px] flex-col gap-8">
    <header>
      <h1 class="font-display text-4xl font-extrabold tracking-tight text-slate-900">My Resumes</h1>
      <p class="mt-2 max-w-3xl text-sm leading-7 text-slate-600">
        Manage your editorial portfolios and track your application performance across multiple platforms.
      </p>
    </header>

    <div class="grid grid-cols-12 gap-8">
      <div class="col-span-12 flex flex-col gap-8 lg:col-span-8">
        <div class="group overflow-hidden rounded-2xl bg-white shadow-soft transition-all hover:-translate-y-1">
          <div class="flex flex-col md:flex-row">
            <div class="relative w-full overflow-hidden bg-[#efedf6] p-6 md:w-1/3">
              <div class="aspect-[3/4] rounded-lg bg-white p-4 shadow-xl">
                <div class="h-full w-full border-t-4 border-[#003aa0] p-3">
                  <div class="mb-4 h-4 w-1/2 rounded bg-slate-200"></div>
                  <div class="mb-1 h-2 w-full rounded bg-slate-100"></div>
                  <div class="mb-1 h-2 w-full rounded bg-slate-100"></div>
                  <div class="mb-6 h-2 w-2/3 rounded bg-slate-100"></div>
                  <div class="grid grid-cols-2 gap-2">
                    <div class="h-12 rounded-sm bg-slate-100"></div>
                    <div class="h-12 rounded-sm bg-slate-100"></div>
                  </div>
                </div>
              </div>
              <div class="absolute right-4 top-4 rounded-full bg-[#003aa0] px-3 py-1 text-[10px] font-bold uppercase tracking-[0.18em] text-white">
                Active
              </div>
            </div>

            <div class="flex flex-1 flex-col justify-between p-8">
              <div>
                <div class="mb-4 flex items-start justify-between">
                  <div>
                    <h2 class="font-display text-2xl font-bold text-slate-900">
                      {{ myCvs[0]?.title || "Senior Product Architect" }}
                    </h2>
                    <p class="mt-1 text-sm text-slate-500">
                      Last edited recently • <span class="font-medium text-[#003aa0]">{{ recentCvCount }} resumes</span>
                    </p>
                  </div>
                  <button type="button" class="rounded-full p-2 transition hover:bg-slate-100">
                    <svg class="h-5 w-5 text-slate-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M12 6h.01M12 12h.01M12 18h.01" />
                    </svg>
                  </button>
                </div>

                <div class="mb-8 flex gap-2">
                  <span class="rounded-full bg-[#d0e1fb] px-3 py-1 text-xs font-bold text-[#38485d]">Executive</span>
                  <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-bold text-slate-500">Editorial Theme</span>
                </div>
              </div>

              <div class="grid grid-cols-2 gap-3 sm:grid-cols-4">
                <button
                  type="button"
                  class="inline-flex items-center justify-center gap-2 rounded-xl bg-[#003aa0] px-4 py-3 text-xs font-bold text-white transition hover:bg-blue-700"
                  :disabled="creating"
                  @click="handleCreateCv"
                  data-testid="create-cv"
                >
                  {{ creating ? t("dashboard.creating") : t("dashboard.createNew") }}
                </button>
                <RouterLink
                  v-if="myCvs[0]"
                  :to="`/editor/${myCvs[0].id}`"
                  class="inline-flex items-center justify-center gap-2 rounded-xl bg-[#e9e7f0] px-4 py-3 text-xs font-bold text-slate-800 transition hover:bg-[#e3e1ea]"
                >
                  Edit
                </RouterLink>
                <button type="button" class="rounded-xl bg-[#e9e7f0] px-4 py-3 text-xs font-bold text-slate-800 transition hover:bg-[#e3e1ea]">
                  Publish
                </button>
                <button type="button" class="rounded-xl bg-[#e9e7f0] px-4 py-3 text-xs font-bold text-slate-800 transition hover:bg-[#e3e1ea]">
                  PDF
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="loading" data-testid="reward-loading" class="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-600">
          {{ t("common.loading") }}
        </div>
        <div v-else-if="error" data-testid="reward-error" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
          {{ error }}
        </div>

        <div v-else class="grid grid-cols-1 gap-8 md:grid-cols-2">
          <article
            v-for="cv in myCvs.slice(1, 3)"
            :key="cv.id"
            class="rounded-2xl border-b-4 border-transparent bg-white p-6 shadow-sm transition-all hover:border-[#b4c5ff]"
          >
            <div class="mb-6 flex justify-between">
              <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-[#f4f2fc] text-[#003aa0]">
                <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M7 8h10M7 12h10M7 16h6" />
                </svg>
              </div>
              <span class="text-xs font-bold text-slate-500">Modified recently</span>
            </div>
            <h3 class="font-display text-lg font-bold text-slate-900">{{ cv.title }}</h3>
            <p class="mt-2 text-sm text-slate-500">
              {{ t("editor.template") }}: {{ cv.templateKey || "minimal" }}
            </p>
            <div class="mt-6 flex items-center gap-4">
              <RouterLink :to="`/editor/${cv.id}`" class="text-xs font-bold text-[#003aa0] hover:underline" data-testid="cv-edit-link">
                Edit Workspace
              </RouterLink>
              <button
                type="button"
                class="text-xs font-bold text-slate-500 transition hover:text-[#003aa0]"
                :disabled="deletingId === cv.id"
                data-testid="cv-delete-button"
                @click="confirmDelete(cv)"
              >
                {{ deletingId === cv.id ? t("dashboard.deleting") : t("common.delete") }}
              </button>
            </div>
          </article>

          <div
            v-if="cvsLoading"
            class="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-600 md:col-span-2"
            data-testid="cvs-loading"
          >
            {{ t("common.loading") }}
          </div>
          <div
            v-else-if="cvsError"
            class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700 md:col-span-2"
            data-testid="cvs-error"
          >
            {{ cvsError }}
          </div>
          <div
            v-else-if="!myCvs.length"
            class="rounded-2xl border border-dashed border-slate-300 bg-white px-4 py-10 text-center text-sm text-slate-500 md:col-span-2"
            data-testid="cvs-empty"
          >
            {{ t("dashboard.noCvs") }}
          </div>
        </div>
      </div>

      <div class="col-span-12 flex flex-col gap-8 lg:col-span-4">
        <section class="rounded-3xl bg-[#f4f2fc] p-8">
          <div class="flex items-center justify-between">
            <h3 class="font-display text-xl font-extrabold tracking-tight text-slate-900">Architect Rewards</h3>
            <span class="rounded-full bg-[#ffddb8] px-3 py-1 text-xs font-bold text-[#603b00]">Rewards</span>
          </div>

          <div class="mt-6 flex flex-col gap-2">
            <div class="flex items-end justify-between">
              <span class="font-display text-4xl font-black text-[#603b00]" data-testid="reward-balance">{{ showBalance }}</span>
              <span class="text-xs font-bold text-[#653e00]">{{ t("common.credits") }}</span>
            </div>
            <div class="flex h-3 w-full gap-0.5 overflow-hidden rounded-full bg-slate-200">
              <div class="h-full w-1/4 bg-[#f59e0b]"></div>
              <div class="h-full w-1/4 bg-[#f59e0b]"></div>
              <div class="h-full w-1/4 bg-[#f59e0b]/60"></div>
              <div class="h-full w-1/4 bg-slate-300"></div>
            </div>
            <p class="mt-1 text-center text-[10px] font-bold uppercase tracking-[0.18em] text-slate-500">
              Workspace credits and reward progress
            </p>
          </div>

          <div class="mt-6 rounded-2xl bg-white/60 p-4">
            <p class="text-sm font-semibold text-slate-900">Referral status</p>
            <div class="mt-3 flex items-center gap-3">
              <div class="flex -space-x-2">
                <div class="h-8 w-8 rounded-full border-2 border-white bg-blue-100"></div>
                <div class="h-8 w-8 rounded-full border-2 border-white bg-slate-200"></div>
                <div class="flex h-8 w-8 items-center justify-center rounded-full border-2 border-white bg-slate-300 text-[10px] font-bold text-slate-700">
                  +{{ inviteStats.inviterRedemptions }}
                </div>
              </div>
              <p class="text-xs text-slate-500">{{ t("dashboard.invitedUsers", { count: inviteStats.inviterRedemptions }) }}</p>
            </div>
          </div>

          <div class="mt-6 grid gap-3">
            <button
              type="button"
              class="w-full rounded-xl bg-[#603b00] px-4 py-4 text-sm font-bold text-white transition hover:opacity-90"
              @click="handleRedeem"
              data-testid="redeem-button"
            >
              {{ t("dashboard.redeemButton") }}
            </button>
            <input
              v-model="inputCode"
              :placeholder="t('dashboard.redeemPlaceholder')"
              class="h-11 rounded-xl border-slate-200 bg-white text-sm placeholder:text-slate-400 focus:border-blue-400 focus:ring-blue-200"
              data-testid="redeem-input"
            />
            <p
              v-if="redeemMessage"
              :class="['text-sm font-medium', redeemStatus === 'error' ? 'text-rose-700' : 'text-emerald-700']"
              data-testid="redeem-message"
            >
              {{ redeemMessage }}
            </p>
            <p v-if="inviteRedemption" class="text-xs text-slate-500">
              {{ t("dashboard.lastInviteUsage", { code: inviteRedemption.inviteCode }) }}
            </p>
          </div>
        </section>

        <section class="rounded-3xl bg-white p-8 shadow-soft">
          <h3 class="font-display text-xl font-extrabold tracking-tight text-slate-900">Weekly Pulse</h3>
          <div class="mt-6 flex flex-col gap-6">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-50 text-[#003aa0]">
                  <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M5 12h14M12 5l7 7-7 7" />
                  </svg>
                </div>
                <div>
                  <p class="text-xs font-bold text-slate-500">Templates</p>
                  <p class="text-lg font-extrabold text-slate-900">{{ visibleTemplates.length }}</p>
                </div>
              </div>
              <span class="text-xs font-bold text-emerald-600">+15%</span>
            </div>
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-50 text-[#603b00]">
                  <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M8 7V3m8 4V3m-9 8h10m-11 9h12a2 2 0 002-2V7a2 2 0 00-2-2H6a2 2 0 00-2 2v11a2 2 0 002 2z" />
                  </svg>
                </div>
                <div>
                  <p class="text-xs font-bold text-slate-500">Recent Redemptions</p>
                  <p class="text-lg font-extrabold text-slate-900">{{ recentRedemptions.length }}</p>
                </div>
              </div>
              <span class="text-xs font-bold text-slate-500">Stable</span>
            </div>
            <div class="relative mt-2 h-32 w-full">
              <div class="absolute inset-0 flex items-end gap-1">
                <div class="h-1/2 flex-1 rounded-t-sm bg-slate-200"></div>
                <div class="h-3/4 flex-1 rounded-t-sm bg-slate-200"></div>
                <div class="h-full flex-1 rounded-t-sm bg-[#003aa0]"></div>
                <div class="h-2/3 flex-1 rounded-t-sm bg-slate-200"></div>
                <div class="h-1/3 flex-1 rounded-t-sm bg-slate-200"></div>
                <div class="h-1/2 flex-1 rounded-t-sm bg-slate-200"></div>
                <div class="h-3/4 flex-1 rounded-t-sm bg-slate-200"></div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>

    <div class="grid gap-3">
      <p
        v-if="actionMessage"
        class="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700"
        data-testid="dashboard-action-message"
      >
        {{ actionMessage }}
      </p>
      <p
        v-if="actionError"
        class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm font-medium text-rose-700"
        data-testid="dashboard-action-error"
      >
        {{ actionError }}
      </p>
    </div>

    <section class="rounded-3xl bg-white p-8 shadow-soft">
      <div class="mb-6 flex items-end justify-between gap-4">
        <div>
          <h2 class="font-display text-2xl font-extrabold tracking-tight text-slate-900">{{ t("dashboard.featuredTemplates") }}</h2>
          <p class="mt-2 max-w-2xl text-sm text-slate-500">{{ t("dashboard.featuredTemplatesDescription") }}</p>
        </div>
      </div>
      <div v-if="templateLoading" class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-600" data-testid="templates-loading">
        {{ t("dashboard.templatesLoading") }}
      </div>
      <div v-else-if="templateError" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700" data-testid="templates-error">
        {{ templateError }}
      </div>
      <div v-else class="grid gap-4 md:grid-cols-2 xl:grid-cols-3" data-testid="template-grid">
        <TemplateCard v-for="template in visibleTemplates" :key="template.key" :template="template" />
      </div>
    </section>

    <div
      v-if="deleteConfirmId"
      class="fixed inset-0 z-30 flex items-center justify-center bg-slate-950/50 p-4 backdrop-blur-sm"
      @click.self="cancelDelete"
    >
      <div class="w-full max-w-md rounded-[28px] border border-slate-200 bg-white p-6">
        <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-slate-400">Confirm action</p>
        <h3 class="mt-3 font-display text-2xl font-bold tracking-tight text-slate-950">
          {{ t("dashboard.confirmDelete") }}
        </h3>
        <p class="mt-3 text-sm leading-7 text-slate-600">
          {{ t("dashboard.confirmDeleteMessage", { title: deleteConfirmTitle }) }}
        </p>
        <div class="mt-6 flex flex-col gap-3 sm:flex-row sm:justify-end">
          <button
            type="button"
            class="inline-flex min-h-11 items-center justify-center rounded-xl border border-slate-200 bg-white px-5 text-sm font-semibold text-slate-700 transition hover:bg-slate-100"
            @click="cancelDelete"
          >
            {{ t("common.cancel") }}
          </button>
          <button
            type="button"
            class="inline-flex min-h-11 items-center justify-center rounded-xl bg-rose-600 px-5 text-sm font-semibold text-white transition hover:bg-rose-700 disabled:cursor-not-allowed disabled:opacity-50"
            :disabled="deletingId !== null"
            @click="handleDeleteCv"
          >
            {{ deletingId !== null ? t("dashboard.deleting") : t("common.delete") }}
          </button>
        </div>
      </div>
    </div>
  </section>
</template>
