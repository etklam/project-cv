<script setup>
const props = defineProps({
  items: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(["update:items"]);

function onInput(event) {
  emit(
    "update:items",
    event.target.value
      .split(",")
      .map((value) => value.trim())
      .filter(Boolean),
  );
}
</script>

<template>
  <section class="space-y-3" data-testid="section-editor-skills">
    <div>
      <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-slate-500">Capabilities</p>
      <h3 class="mt-1 font-headline text-lg font-extrabold tracking-tight text-on-surface">Skills</h3>
    </div>
    <input
      :value="items.join(', ')"
      class="h-12 w-full rounded-[24px] border border-slate-200 bg-surface-container-low px-4 text-sm text-on-surface focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/10"
      data-testid="section-skills-input"
      @input="onInput"
    />
  </section>
</template>
