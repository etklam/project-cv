<script setup>
import { useI18n } from "vue-i18n";
import SummarySection from "@/components/sections/SummarySection.vue";

defineOptions({
  name: "ModernTemplate",
});
import ExperienceSection from "@/components/sections/ExperienceSection.vue";
import EducationSection from "@/components/sections/EducationSection.vue";
import SkillsSection from "@/components/sections/SkillsSection.vue";

const { t } = useI18n();

const props = defineProps({
  cv: { type: Object, default: () => ({}) },
  sections: { type: Array, default: () => [] },
  mode: { type: String, default: "public" },
});

const sectionComponentMap = {
  summary: SummarySection,
  experience: ExperienceSection,
  education: EducationSection,
  skills: SkillsSection,
};
</script>

<template>
  <section data-testid="template-modern" class="modern">
    <header class="modern__header">
      <div>
        <h1 class="text-2xl sm:text-3xl font-bold text-gray-900">{{ cv.title || t("templates.modern.name") }}</h1>
        <p class="text-sm text-gray-500 mt-1">{{ t("editor.template") }}</p>
      </div>
      <div class="modern__pill">{{ cv.templateKey || "modern" }}</div>
    </header>
    <div class="modern__grid space-y-6">
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
.modern {
  padding: 1.5rem;
  background: linear-gradient(145deg, #f8fafc, #f1f5f9);
  border-radius: 1.25rem;
  border: 1px solid #e2e8f0;
  display: grid;
  gap: 1rem;
}
@media (max-width: 640px) {
  .modern {
    padding: 1rem;
    border-radius: 0.75rem;
  }
}
.modern__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}
.modern__pill {
  padding: 0.35rem 0.75rem;
  border-radius: 999px;
  background: #0ea5e9;
  color: white;
  font-weight: 600;
  font-size: 0.9rem;
}
.modern__grid {
  display: grid;
  gap: 0.75rem;
}
</style>
