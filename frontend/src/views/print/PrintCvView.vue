<script setup>
import CvTemplateRenderer from "@/components/cv-templates/CvTemplateRenderer.vue";

const cv = { title: "Print CV" };
</script>

<template>
  <section data-testid="view-print">
    <CvTemplateRenderer
      template-key="minimal"
      :cv="cv"
      :sections="[]"
      mode="print"
    />
  </section>
</template>
