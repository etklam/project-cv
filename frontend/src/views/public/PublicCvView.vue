<script setup>
import CvTemplateRenderer from "@/components/cv-templates/CvTemplateRenderer.vue";

const cv = { title: "Public CV" };
</script>

<template>
  <section data-testid="view-public-cv">
    <CvTemplateRenderer
      template-key="minimal"
      :cv="cv"
      :sections="[]"
      mode="public"
    />
  </section>
</template>
