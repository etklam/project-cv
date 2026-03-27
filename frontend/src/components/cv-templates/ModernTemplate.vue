<script>
export default {
  name: "ModernTemplate",
  props: {
    cv: { type: Object, default: () => ({}) },
    sections: { type: Array, default: () => [] },
    mode: { type: String, default: "public" },
  },
};
</script>

<template>
  <section data-testid="template-modern">
    <h1>{{ cv.title || "Modern CV" }}</h1>
    <p>{{ mode }}</p>
  </section>
</template>
