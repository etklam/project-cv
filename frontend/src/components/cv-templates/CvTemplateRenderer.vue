<script setup>
import { computed } from "vue";
import { resolveTemplateComponent } from "@/components/cv-templates/templateRegistry";

const props = defineProps({
  templateKey: {
    type: String,
    required: true,
  },
  cv: {
    type: Object,
    default: () => ({}),
  },
  sections: {
    type: Array,
    default: () => [],
  },
  appearance: {
    type: Object,
    default: () => ({}),
  },
  mode: {
    type: String,
    default: "public",
  },
});

const resolvedComponent = computed(() => resolveTemplateComponent(props.templateKey));
</script>

<template>
  <component
    :is="resolvedComponent"
    :template-key="templateKey"
    :cv="cv"
    :sections="sections"
    :appearance="appearance"
    :mode="mode"
  />
</template>
