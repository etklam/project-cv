<script setup>
import { onMounted, ref } from "vue";
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

const handleRedeem = async () => {
  const success = await redeem(inputCode.value);
  if (success) {
    inputCode.value = "";
    // Refresh CV list after successful redemption
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
  } catch (error) {
    actionError.value =
      error?.response?.data?.message || error?.message || t("dashboard.cvCreateError");
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
  } catch (error) {
    actionError.value =
      error?.response?.data?.message || error?.message || t("dashboard.cvDeleteError");
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
  <section data-testid="view-dashboard" class="dashboard max-w-6xl mx-auto px-4 sm:px-6 py-6">
    <header class="dashboard__header flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
      <div>
        <h1 class="text-2xl sm:text-3xl font-bold text-gray-900">{{ t("dashboard.title") }}</h1>
        <p class="text-sm sm:text-base text-gray-600 mt-1">{{ t("dashboard.description") }}</p>
      </div>
      <button
        type="button"
        class="cta bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white font-semibold py-2 px-4 sm:px-6 rounded-lg transition-colors"
        :disabled="creating"
        @click="handleCreateCv"
        data-testid="create-cv"
      >
        {{ creating ? t("dashboard.creating") : t("dashboard.createNew") }}
      </button>
    </header>
    <p v-if="actionMessage" class="badge bg-green-50 text-green-800 px-4 py-2 rounded-lg" data-testid="dashboard-action-message">{{ actionMessage }}</p>
    <p v-if="actionError" class="badge badge--error bg-red-50 text-red-800 px-4 py-2 rounded-lg" data-testid="dashboard-action-error">{{ actionError }}</p>

    <div v-if="loading" data-testid="reward-loading" class="badge bg-gray-50 text-gray-700 px-4 py-2 rounded-lg">
      {{ t("common.loading") }}
    </div>
    <div v-else-if="error" data-testid="reward-error" class="badge badge--error bg-red-50 text-red-800 px-4 py-2 rounded-lg">
      {{ error }}
    </div>
    <div v-else class="dashboard__grid grid grid-cols-1 lg:grid-cols-3 gap-4">
      <article class="reward-card bg-white rounded-xl border border-gray-200 p-4 sm:p-5 lg:col-span-2">
        <div class="reward-card__header flex justify-between items-baseline">
          <p class="text-gray-700 font-medium">{{ t("dashboard.creditBalance") }}</p>
          <p class="text-2xl font-bold text-blue-600" data-testid="reward-balance">{{ showBalance }} {{ t("common.credits") }}</p>
        </div>
        <p class="reward-card__sub text-gray-600 text-sm mt-2" data-testid="reward-invite-code">
          {{ t("dashboard.inviteCode") }}: {{ summary?.inviteCode || "—" }}
        </p>
        <div class="reward-card__stats flex flex-wrap gap-3 sm:gap-4 mt-3 text-sm">
          <p class="text-gray-700">{{ t("dashboard.invitedUsers", { count: inviteStats.inviterRedemptions }) }}</p>
          <p class="text-gray-700">{{ t("dashboard.creditsEarned", { count: inviteStats.totalInviterCredits }) }}</p>
          <p class="text-gray-700" data-testid="promo-redemption-count">
            {{ t("dashboard.promoRedemptions") }}: {{ promoRedemptionsCount }}
          </p>
        </div>
        <div class="reward-card__section mt-4">
          <h3 class="font-semibold text-gray-900 mb-2">{{ t("dashboard.recentRedemptions") }}</h3>
          <div v-if="!recentRedemptions.length" class="empty-state text-gray-500 text-sm">
            {{ t("dashboard.noRedemptions") }}
          </div>
          <ul v-else class="space-y-2">
            <li
              v-for="record in recentRedemptions"
              :key="record.code + record.creditedAmount"
              data-testid="promo-record"
              class="flex justify-between items-center py-2 border-b border-dashed border-gray-200 last:border-0"
            >
              <span class="text-gray-700">{{ record.code }}</span>
              <span class="text-green-600 font-medium">+{{ record.creditedAmount }} {{ t("common.credits") }}</span>
            </li>
          </ul>
        </div>
      </article>

      <article class="redeem-panel bg-white rounded-xl border border-gray-200 p-4 sm:p-5">
        <h3 class="font-semibold text-gray-900">{{ t("dashboard.redeemCode") }}</h3>
        <p class="text-sm text-gray-600 mt-1">{{ t("dashboard.redeemDescription") }}</p>
        <div class="redeem-panel__form flex gap-2 mt-3">
          <input
            v-model="inputCode"
            :placeholder="t('dashboard.redeemPlaceholder')"
            class="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            data-testid="redeem-input"
          />
          <button
            type="button"
            @click="handleRedeem"
            class="bg-blue-600 hover:bg-blue-700 text-white font-semibold px-4 py-2 rounded-lg transition-colors text-sm"
            data-testid="redeem-button"
          >
            {{ t("dashboard.redeemButton") }}
          </button>
        </div>
        <p
          v-if="redeemMessage"
          :class="['redeem-panel__message mt-3 text-sm', redeemStatus === 'error' ? 'text-red-600' : 'text-green-600']"
          data-testid="redeem-message"
        >
          {{ redeemMessage }}
        </p>
        <div class="redeem-panel__details mt-3 text-sm text-gray-600" v-if="inviteRedemption">
          <p>{{ t("dashboard.lastInviteUsage", { code: inviteRedemption.inviteCode }) }}</p>
        </div>
      </article>
    </div>

    <section class="templates-section mt-8">
      <div class="section-header">
        <h2 class="text-xl font-semibold text-gray-900">{{ t("dashboard.featuredTemplates") }}</h2>
        <p class="text-sm text-gray-600 mt-1">{{ t("dashboard.featuredTemplatesDescription") }}</p>
      </div>
      <div v-if="templateLoading" class="placeholder bg-gray-50 text-gray-700 px-4 py-3 rounded-lg text-center" data-testid="templates-loading">
        {{ t("dashboard.templatesLoading") }}
      </div>
      <div v-else-if="templateError" class="placeholder bg-red-50 text-red-700 px-4 py-3 rounded-lg text-center" data-testid="templates-error">
        {{ templateError }}
      </div>
      <div v-else class="template-grid grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4" data-testid="template-grid">
        <TemplateCard
          v-for="template in visibleTemplates"
          :key="template.key"
          :template="template"
        />
      </div>
    </section>

    <section class="cvs-section mt-8">
      <div class="section-header">
        <h2 class="text-xl font-semibold text-gray-900">{{ t("dashboard.myCvs") }}</h2>
        <p class="text-sm text-gray-600 mt-1">{{ t("dashboard.myCvsDescription") }}</p>
      </div>
      <div v-if="cvsLoading" class="placeholder bg-gray-50 text-gray-700 px-4 py-3 rounded-lg text-center" data-testid="cvs-loading">{{ t("common.loading") }}</div>
      <div v-else-if="cvsError" class="placeholder bg-red-50 text-red-700 px-4 py-3 rounded-lg text-center" data-testid="cvs-error">{{ cvsError }}</div>
      <ul v-else-if="myCvs.length" class="cv-list space-y-3" data-testid="cvs-list">
        <li v-for="cv in myCvs" :key="cv.id" class="cv-list__item bg-white border border-gray-200 rounded-lg p-4">
          <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
            <div class="cv-list__meta flex-1">
              <p class="cv-list__title font-semibold text-gray-900" data-testid="cv-title">{{ cv.title || t("editor.title") }}</p>
              <p class="cv-list__sub text-sm text-gray-600 mt-1">
                {{ t("editor.template") }}: {{ cv.templateKey || "minimal" }} |
                {{ cv.isPublic ? t("editor.isPublic") : t("common.private", "Private") }}
                <span v-if="cv.slug"> | slug: {{ cv.slug }}</span>
              </p>
            </div>
            <div class="cv-list__actions flex flex-wrap items-center gap-3 w-full sm:w-auto">
              <RouterLink
                :to="`/editor/${cv.id}`"
                class="text-blue-600 hover:text-blue-700 font-medium text-sm"
                data-testid="cv-edit-link"
              >
                {{ t("common.edit") }}
              </RouterLink>
              <button
                type="button"
                class="text-red-600 hover:text-red-700 font-medium text-sm disabled:text-gray-400 disabled:cursor-not-allowed"
                :disabled="deletingId === cv.id"
                data-testid="cv-delete-button"
                @click="confirmDelete(cv)"
              >
                {{ deletingId === cv.id ? t("dashboard.deleting") : t("common.delete") }}
              </button>
              <span
                v-if="cv.isPublic && cv.slug"
                class="text-green-600 text-sm font-medium"
                data-testid="cv-public-badge"
              >
                /{{ cv.slug }}
              </span>
            </div>
          </div>
        </li>
      </ul>
      <div v-else class="placeholder bg-gray-50 text-gray-700 px-4 py-8 rounded-lg text-center" data-testid="cvs-empty">{{ t("dashboard.noCvs") }}</div>
    </section>

    <!-- Delete Confirmation Dialog -->
    <div
      v-if="deleteConfirmId"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      @click.self="cancelDelete"
    >
      <div class="bg-white rounded-lg shadow-xl p-6 max-w-sm mx-4">
        <h3 class="text-lg font-semibold text-gray-900 mb-2">{{ t("dashboard.confirmDelete") }}</h3>
        <p class="text-gray-600 mb-4">
          {{ t("dashboard.confirmDeleteMessage", { title: deleteConfirmTitle }) }}
        </p>
        <div class="flex justify-end gap-3">
          <button
            type="button"
            class="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
            @click="cancelDelete"
          >
            {{ t("common.cancel") }}
          </button>
          <button
            type="button"
            class="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors disabled:opacity-50"
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

<style scoped>
.badge {
  display: inline-block;
}
</style>
