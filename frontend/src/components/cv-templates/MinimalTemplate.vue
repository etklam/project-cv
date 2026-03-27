<script>
export default {
  name: "MinimalTemplate",
  props: {
    cv: { type: Object, default: () => ({}) },
    sections: { type: Array, default: () => [] },
    mode: { type: String, default: "public" },
  },
};
</script>

<template>
  <section data-testid="template-minimal">
    <h1>{{ cv.title || "Untitled CV" }}</h1>
    <p>mode: {{ mode }}</p>
    <ul>
      <li v-for="(section, index) in sections" :key="index">
        {{ section.sectionType }}
      </li>
    </ul>
  </section>
</template>
