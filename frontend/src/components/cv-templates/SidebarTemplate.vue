<script>
export default {
  name: "SidebarTemplate",
  props: {
    cv: { type: Object, default: () => ({}) },
    sections: { type: Array, default: () => [] },
    mode: { type: String, default: "public" },
  },
};
</script>

<template>
  <section data-testid="template-sidebar">
    <h1>{{ cv.title || "Sidebar CV" }}</h1>
    <p>{{ mode }}</p>
  </section>
</template>
