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
  <section class="section summary">
    <h2 class="section__title">{{ t("sections.summary.title") }}</h2>
    <p class="section__body">{{ content?.text || "—" }}</p>
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
.section__body {
  margin: 0;
  color: var(--editor-theme-text-muted, #475569);
  line-height: 1.6;
}
</style>
