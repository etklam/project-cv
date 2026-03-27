<script setup>
import { onMounted, ref } from "vue";
import TemplateCard from "@/components/templates/TemplateCard.vue";
import { listCvs } from "@/api/cv";
import { useRewardCenter } from "@/composables/useRewardCenter";
import { useTemplateCatalog } from "@/composables/useTemplateCatalog";

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

const handleRedeem = async () => {
  await redeem(inputCode.value);
  if (redeemStatus.value === "success") {
    inputCode.value = "";
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
      requestError?.response?.data?.message || requestError?.message || "Unable to load CV list";
  } finally {
    cvsLoading.value = false;
  }
};

const {
  visibleTemplates,
  loading: templateLoading,
  error: templateError,
  loadTemplates,
} = useTemplateCatalog({
  errorMessage: "Unable to load templates",
  limit: 3,
});

onMounted(() => {
  loadSummary();
  loadTemplates();
  loadMyCvs();
});
</script>

<template>
  <section data-testid="view-dashboard" class="dashboard">
    <header class="dashboard__header">
      <div>
        <h1>Dashboard</h1>
        <p>Track your credits, rewards, and template picks.</p>
      </div>
      <button type="button" class="cta">Create New</button>
    </header>

    <div v-if="loading" data-testid="reward-loading" class="badge">
      Loading reward summary...
    </div>
    <div v-else-if="error" data-testid="reward-error" class="badge badge--error">
      {{ error }}
    </div>
    <div v-else class="dashboard__grid">
      <article class="reward-card">
        <div class="reward-card__header">
          <p>Credit Balance</p>
          <p data-testid="reward-balance">{{ showBalance }} credits</p>
        </div>
        <p class="reward-card__sub" data-testid="reward-invite-code">
          Invite code: {{ summary?.inviteCode || "—" }}
        </p>
        <div class="reward-card__stats">
          <p>Used by {{ inviteStats.inviterRedemptions }} people</p>
          <p>Earned {{ inviteStats.totalInviterCredits }} credits</p>
          <p data-testid="promo-redemption-count">
            Promo redemptions: {{ promoRedemptionsCount }}
          </p>
        </div>
        <div class="reward-card__section">
          <h3>Recent redemptions</h3>
          <div v-if="!recentRedemptions.length" class="empty-state">
            No promo redemptions yet.
          </div>
          <ul v-else>
            <li
              v-for="record in recentRedemptions"
              :key="record.code + record.creditedAmount"
              data-testid="promo-record"
            >
              <span>{{ record.code }}</span>
              <span>+{{ record.creditedAmount }} credits</span>
            </li>
          </ul>
        </div>
      </article>

      <article class="redeem-panel">
        <h3>Redeem a code</h3>
        <div class="redeem-panel__form">
          <input
            v-model="inputCode"
            placeholder="Enter promo or invite code"
            data-testid="redeem-input"
          />
          <button type="button" @click="handleRedeem" data-testid="redeem-button">
            Apply
          </button>
        </div>
        <p
          v-if="redeemMessage"
          :class="['redeem-panel__message', redeemStatus === 'error' ? 'error' : 'success']"
          data-testid="redeem-message"
        >
          {{ redeemMessage }}
        </p>
        <div class="redeem-panel__details" v-if="inviteRedemption">
          <p>Last invite usage: {{ inviteRedemption.inviteCode }}</p>
        </div>
      </article>
    </div>

    <section class="templates-section">
      <div class="section-header">
        <h2>Featured templates</h2>
        <p>Each template costs credits to enable on your profile.</p>
      </div>
      <div v-if="templateLoading" class="placeholder" data-testid="templates-loading">
        Loading templates...
      </div>
      <div v-else-if="templateError" class="placeholder" data-testid="templates-error">
        {{ templateError }}
      </div>
      <div v-else class="template-grid" data-testid="template-grid">
        <TemplateCard
          v-for="template in visibleTemplates"
          :key="template.key"
          :template="template"
        />
      </div>
    </section>

    <section class="cvs-section">
      <div class="section-header">
        <h2>My CVs</h2>
        <p>Manage metadata and public visibility for each resume.</p>
      </div>
      <div v-if="cvsLoading" class="placeholder" data-testid="cvs-loading">Loading CVs...</div>
      <div v-else-if="cvsError" class="placeholder" data-testid="cvs-error">{{ cvsError }}</div>
      <ul v-else-if="myCvs.length" class="cv-list" data-testid="cvs-list">
        <li v-for="cv in myCvs" :key="cv.id" class="cv-list__item">
          <div class="cv-list__meta">
            <p class="cv-list__title" data-testid="cv-title">{{ cv.title || "Untitled CV" }}</p>
            <p class="cv-list__sub">
              template: {{ cv.templateKey || "minimal" }} |
              {{ cv.isPublic ? "public" : "private" }}
              <span v-if="cv.slug"> | slug: {{ cv.slug }}</span>
            </p>
          </div>
          <div class="cv-list__actions">
            <RouterLink :to="`/editor/${cv.id}`" data-testid="cv-edit-link">Edit</RouterLink>
            <span
              v-if="cv.isPublic && cv.slug"
              data-testid="cv-public-badge"
            >
              Public slug: /{{ cv.slug }}
            </span>
          </div>
        </li>
      </ul>
      <div v-else class="placeholder" data-testid="cvs-empty">No CVs yet.</div>
    </section>
  </section>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}
.dashboard__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.dashboard__grid {
  display: grid;
  grid-template-columns: minmax(280px, 2fr) minmax(260px, 1fr);
  gap: 1rem;
}
.reward-card,
.redeem-panel {
  padding: 1.25rem;
  border-radius: 1rem;
  border: 1px solid #e2e8f0;
  background: #ffffff;
}
.reward-card__header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}
.reward-card__sub {
  color: #475569;
}
.reward-card__stats {
  display: flex;
  gap: 1rem;
  margin: 0.5rem 0;
  font-size: 0.95rem;
}
.reward-card__section h3 {
  margin-bottom: 0.5rem;
}
.reward-card__section ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.reward-card__section li {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 0;
  border-bottom: 1px dashed #e2e8f0;
}
.empty-state {
  font-size: 0.9rem;
  color: #94a3b8;
}
.redeem-panel__form {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
}
.redeem-panel__form input {
  flex: 1;
  border: 1px solid #cbd5f5;
  border-radius: 0.75rem;
  padding: 0.65rem 0.75rem;
}
.redeem-panel__form button {
  border: none;
  background: #2563eb;
  color: white;
  font-weight: 600;
  padding: 0 1rem;
  border-radius: 0.75rem;
  cursor: pointer;
}
.redeem-panel__message {
  margin-top: 0.75rem;
  font-size: 0.95rem;
}
.redeem-panel__message.error {
  color: #b91c1c;
}
.redeem-panel__message.success {
  color: #16a34a;
}
.redeem-panel__details {
  margin-top: 0.5rem;
}
.templates-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.section-header h2 {
  margin: 0;
  font-size: 1.2rem;
}
.placeholder {
  padding: 1rem;
  border-radius: 0.75rem;
  border: 1px dashed #94a3b8;
  color: #475569;
}
.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
}
.cvs-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.cv-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.cv-list__item {
  border: 1px solid #e2e8f0;
  border-radius: 0.75rem;
  padding: 0.9rem 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}
.cv-list__title {
  margin: 0;
  font-weight: 600;
}
.cv-list__sub {
  margin: 0.2rem 0 0;
  color: #64748b;
  font-size: 0.92rem;
}
.cv-list__actions {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}
.cv-list__actions a,
.cv-list__actions span {
  color: #1d4ed8;
  font-weight: 600;
}
.cv-list__actions a {
  text-decoration: none;
}
</style>
