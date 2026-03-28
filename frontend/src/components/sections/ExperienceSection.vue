<script setup>
import { useI18n } from "vue-i18n";

const { t } = useI18n();

defineProps({
  content: {
    type: Object,
    default: () => ({}),
  },
});
</script>

<template>
  <section class="section experience">
    <h2 class="section__title">{{ t("sections.experience.title") }}</h2>
    <ul class="timeline">
      <li v-for="item in content?.items || []" :key="item?.company + item?.role" class="timeline__item">
        <div class="timeline__header">
          <span class="timeline__role">{{ item?.role }}</span>
          <span class="timeline__dates">
            {{ item?.startDate || "—" }} - {{ item?.current ? t("sections.experience.currentLabel") : item?.endDate || "—" }}
          </span>
        </div>
        <p class="timeline__company">{{ item?.company }}</p>
        <p class="timeline__desc" v-if="item?.description">{{ item.description }}</p>
      </li>
      <li v-if="!content?.items?.length" class="timeline__item muted">{{ t("sections.experience.noItems") }}</li>
    </ul>
  </section>
</template>

<style scoped>
.section {
  padding: 0.75rem 0;
}
.section__title {
  margin: 0 0 0.5rem;
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--editor-theme-accent-strong, #0f172a);
}
.timeline {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 0.65rem;
}
.timeline__item {
  border-left: 3px solid var(--editor-theme-border, #cbd5e1);
  padding-left: 0.75rem;
}
.timeline__header {
  display: flex;
  justify-content: space-between;
  font-weight: 600;
  flex-wrap: wrap;
  gap: 0.5rem;
}
.timeline__company {
  margin: 0.1rem 0;
  color: var(--editor-theme-text-muted, #475569);
}
.timeline__desc {
  margin: 0;
  color: var(--editor-theme-accent-strong, #334155);
  line-height: 1.5;
}
.muted {
  color: var(--editor-theme-text-muted, #94a3b8);
}
</style>
