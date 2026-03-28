<script setup>
const props = defineProps({
  modelValue: {
    type: String,
    default: "",
  },
});

const emit = defineEmits(["update:modelValue"]);
</script>

<template>
  <section class="space-y-3" data-testid="section-editor-summary">
    <div>
      <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-slate-500">Professional Summary</p>
      <h3 class="mt-1 font-headline text-lg font-extrabold tracking-tight text-on-surface">Editorial pitch</h3>
    </div>
    <textarea
      :value="modelValue"
      rows="4"
      class="w-full rounded-[24px] border border-slate-200 bg-surface-container-low px-4 py-3 text-sm text-on-surface focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/10"
      data-testid="section-summary-input"
      @input="emit('update:modelValue', $event.target.value)"
    />
  </section>
</template>
