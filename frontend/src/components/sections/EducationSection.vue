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
  <section class="section education">
    <h2 class="section__title">{{ t("sections.education.title") }}</h2>
    <ul class="edu-list">
      <li v-for="item in content?.items || []" :key="item?.school + item?.degree" class="edu">
        <div class="edu__header">
          <span class="edu__school">{{ item?.school || t("sections.education.school") }}</span>
          <span class="edu__years">{{ item?.startDate || "" }} - {{ item?.endDate || "" }}</span>
        </div>
        <p class="edu__degree">{{ item?.degree || "" }}{{ item?.major ? ` ${t("sections.education.major")} ${item.major}` : "" }}</p>
      </li>
      <li v-if="!content?.items?.length" class="edu muted">{{ t("sections.education.noItems") }}</li>
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
}
.edu-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 0.5rem;
}
.edu__header {
  display: flex;
  justify-content: space-between;
  font-weight: 600;
  flex-wrap: wrap;
  gap: 0.5rem;
}
.edu__degree {
  margin: 0.05rem 0;
  color: #475569;
}
.muted {
  color: #94a3b8;
}
</style>
