<script setup>
import { useI18n } from "vue-i18n";
import ContactSection from "@/components/sections/ContactSection.vue";
import SummarySection from "@/components/sections/SummarySection.vue";
import ExperienceSection from "@/components/sections/ExperienceSection.vue";
import EducationSection from "@/components/sections/EducationSection.vue";
import SkillsSection from "@/components/sections/SkillsSection.vue";

const { t } = useI18n();

defineOptions({
  name: "MinimalTemplate",
});

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
</script>

<template>
  <section data-testid="template-minimal" class="minimal">
    <header class="minimal__header">
      <h1 class="text-2xl sm:text-3xl font-bold text-gray-900">{{ cv.title || t("editor.title") }}</h1>
    </header>
    <div class="minimal__sections space-y-6">
      <component
        v-for="(section, index) in sections"
        :key="`${section.sectionType}-${index}`"
        :is="sectionComponentMap[section.sectionType] || null"
        v-bind="section"
      />
    </div>
  </section>
</template>

<style scoped>
.minimal {
  display: grid;
  gap: 1.5rem;
  padding: 1.5rem;
  background: #ffffff;
  border-radius: 1rem;
  border: 1px solid var(--editor-theme-border, #e2e8f0);
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--editor-theme-accent, #334155) 8%, transparent);
}
.minimal__header {
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--editor-theme-border, #e2e8f0);
}
.minimal__header h1 {
  color: var(--editor-theme-accent-strong, #0f172a);
}
@media (max-width: 640px) {
  .minimal {
    padding: 1rem;
    border-radius: 0.75rem;
  }
}
</style>
