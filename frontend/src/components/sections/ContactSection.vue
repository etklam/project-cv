<script setup>
defineProps({
  content: {
    type: Object,
    default: () => ({}),
  },
});

const contactFields = [
  { key: "email", label: "Email" },
  { key: "location", label: "Location" },
  { key: "website", label: "Website" },
];
</script>

<template>
  <section class="contact" data-testid="contact-section">
    <div class="contact__header">
      <h2 class="contact__name">{{ content?.displayName || "Untitled profile" }}</h2>
      <p v-if="content?.headline" class="contact__headline">{{ content.headline }}</p>
    </div>
    <ul v-if="contactFields.some((field) => content?.[field.key])" class="contact__meta">
      <li v-for="field in contactFields" :key="field.key" v-show="content?.[field.key]" class="contact__meta-item">
        <span class="contact__meta-label">{{ field.label }}</span>
        <span class="contact__meta-value">{{ content?.[field.key] }}</span>
      </li>
    </ul>
  </section>
</template>

<style scoped>
.contact {
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--editor-theme-border, #e2e8f0);
  display: grid;
  gap: 0.75rem;
}
.contact__header {
  display: grid;
  gap: 0.25rem;
}
.contact__name {
  margin: 0;
  font-size: 1.6rem;
  font-weight: 800;
  color: var(--editor-theme-accent-strong, #0f172a);
  line-height: 1.1;
}
.contact__headline {
  margin: 0;
  color: var(--editor-theme-text-muted, #475569);
  font-size: 0.95rem;
}
.contact__meta {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 0.35rem;
}
.contact__meta-item {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  font-size: 0.92rem;
}
.contact__meta-label {
  font-weight: 700;
  color: var(--editor-theme-accent-strong, #0f172a);
}
.contact__meta-value {
  color: var(--editor-theme-text-muted, #475569);
}
</style>
