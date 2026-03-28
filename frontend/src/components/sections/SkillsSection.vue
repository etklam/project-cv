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
  <section class="section skills">
    <h2 class="section__title">{{ t("sections.skills.title") }}</h2>
    <div class="chips">
      <span v-for="skill in content?.items || []" :key="skill" class="chip">{{ skill }}</span>
      <span v-if="!content?.items?.length" class="chip chip--muted">{{ t("sections.skills.noSkills") }}</span>
    </div>
  </section>
</template>

<style scoped>
.section {
  padding: 0.75rem 0;
}
.section__title {
  margin: 0 0 0.35rem;
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--editor-theme-accent-strong, #0f172a);
}
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
}
.chip {
  padding: 0.25rem 0.5rem;
  border-radius: 999px;
  background: var(--editor-theme-accent-soft, #e2e8f0);
  color: var(--editor-theme-accent-strong, #0f172a);
  font-size: 0.9rem;
}
.chip--muted {
  background: color-mix(in srgb, var(--editor-theme-accent-soft, #f1f5f9) 60%, white);
  color: var(--editor-theme-text-muted, #94a3b8);
}
</style>
