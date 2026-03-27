<script>
export default {
  name: "MissingTemplate",
  props: {
    templateKey: {
      type: String,
      default: "",
    },
  },
};
</script>

<template>
  <section data-testid="template-missing">
    <h2>Template not found</h2>
    <p>{{ templateKey }}</p>
  </section>
</template>
