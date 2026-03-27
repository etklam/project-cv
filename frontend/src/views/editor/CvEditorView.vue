<script setup>
import CvTemplateRenderer from "@/components/cv-templates/CvTemplateRenderer.vue";

const cv = { title: "Draft CV" };
const sections = [{ sectionType: "summary", content: { text: "Draft" } }];
</script>

<template>
  <section data-testid="view-editor">
    <h1>Editor</h1>
    <button type="button">Export PDF</button>
    <CvTemplateRenderer
      template-key="minimal"
      :cv="cv"
      :sections="sections"
      mode="editor-preview"
    />
  </section>
</template>
