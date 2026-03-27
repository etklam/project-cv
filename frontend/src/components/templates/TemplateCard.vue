<script setup>
const props = defineProps({
  template: {
    type: Object,
    required: true,
  },
  selected: {
    type: Boolean,
    default: false,
  },
});
</script>

<template>
  <article
    class="template-card"
    :class="{ selected: props.selected }"
    :data-testid="`template-card-${props.template.key}`"
  >
    <div class="template-card__preview" v-if="props.template.previewImagePath">
      <img :src="props.template.previewImagePath" :alt="`${props.template.displayName} preview`" />
    </div>
    <div class="template-card__content">
      <div class="template-card__header">
        <h3>{{ props.template.displayName || props.template.key }}</h3>
        <span class="template-card__cost">{{ props.template.creditCost }} credits</span>
      </div>
      <p class="template-card__description">
        {{ props.template.description || "No description provided yet." }}
      </p>
      <p v-if="props.selected" class="template-card__selected-text">Selected</p>
    </div>
  </article>
</template>

<style scoped>
.template-card {
  border: 1px solid var(--surface-divider, #cbd5f5);
  border-radius: 12px;
  padding: 1rem;
  background: var(--surface, #ffffff);
  transition: border 0.2s ease, box-shadow 0.2s ease;
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
}
.template-card.selected {
  border-color: var(--primary, #3b82f6);
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.12);
}
.template-card__preview img {
  width: 100%;
  border-radius: 10px;
  object-fit: cover;
  height: 160px;
}
.template-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.5rem;
}
.template-card__cost {
  font-weight: 600;
  font-size: 0.85rem;
  color: var(--primary, #3b82f6);
}
.template-card__description {
  line-height: 1.4;
  color: #475569;
  font-size: 0.95rem;
  min-height: 2.65rem;
}
.template-card__selected-text {
  color: #0f766e;
  font-weight: 600;
}
</style>
