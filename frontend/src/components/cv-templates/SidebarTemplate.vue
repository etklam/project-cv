<script setup>
import { useI18n } from "vue-i18n";
import { computed } from "vue";
import ContactSection from "@/components/sections/ContactSection.vue";
import SummarySection from "@/components/sections/SummarySection.vue";

defineOptions({
  name: "SidebarTemplate",
});
import ExperienceSection from "@/components/sections/ExperienceSection.vue";
import EducationSection from "@/components/sections/EducationSection.vue";
import SkillsSection from "@/components/sections/SkillsSection.vue";

const { t } = useI18n();

const props = defineProps({
  cv: { type: Object, default: () => ({}) },
  sections: { type: Array, default: () => [] },
  appearance: { type: Object, default: () => ({}) },
  mode: { type: String, default: "public" },
});

const sectionComponentMap = {
  contact: ContactSection,
  summary: SummarySection,
  experience: ExperienceSection,
  education: EducationSection,
  skills: SkillsSection,
};

const sidebarSections = computed(() =>
  props.sections.filter((section) => ["skills", "education"].includes(section.sectionType)),
);
const mainSections = computed(() =>
  props.sections.filter((section) => !["skills", "education"].includes(section.sectionType)),
);
</script>

<template>
  <section data-testid="template-sidebar" class="sidebar">
    <aside class="sidebar__aside">
      <h2 class="sidebar__title text-xl font-bold text-gray-900">{{ cv.title || t("editor.title") }}</h2>
      <component
        v-for="(section, index) in sidebarSections"
        :key="`${section.sectionType}-${index}`"
        :is="sectionComponentMap[section.sectionType] || null"
        v-bind="section"
      />
    </aside>
    <main class="sidebar__main">
      <component
        v-for="(section, index) in mainSections"
        :key="`${section.sectionType}-${index}`"
        :is="sectionComponentMap[section.sectionType] || null"
        v-bind="section"
      />
    </main>
  </section>
</template>

<style scoped>
.sidebar {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 1rem;
}
@media (max-width: 768px) {
  .sidebar {
    grid-template-columns: 1fr;
  }
}
.sidebar__aside {
  padding: 1rem;
  background: var(--editor-theme-aside, #f8fafc);
  border: 1px solid var(--editor-theme-border, #e2e8f0);
  border-radius: 1rem;
}
.sidebar__title {
  margin: 0 0 0.75rem;
  color: var(--editor-theme-accent-strong, #0f172a);
}
.sidebar__main {
  padding: 1rem;
  border: 1px solid var(--editor-theme-border, #e2e8f0);
  border-radius: 1rem;
  background: #ffffff;
  display: grid;
  gap: 0.75rem;
}
</style>
