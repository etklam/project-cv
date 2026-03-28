<script setup>
const props = defineProps({
  templates: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: "",
  },
  metadata: {
    type: Object,
    default: () => ({}),
  },
  appearance: {
    type: Object,
    default: () => ({}),
  },
});

const emit = defineEmits(["update:template-key", "update:appearance"]);

const surfaceOptions = [
  { value: "studio", label: "Studio", description: "Soft editorial board for drafting." },
  { value: "daylight", label: "Daylight", description: "Bright paper review with lighter contrast." },
  { value: "midnight", label: "Midnight", description: "Dark review surface for proofing." },
];

const accentOptions = [
  { value: "ink", label: "Ink", swatch: "linear-gradient(135deg,#0f172a,#475569)" },
  { value: "ocean", label: "Ocean", swatch: "linear-gradient(135deg,#0369a1,#38bdf8)" },
  { value: "forest", label: "Forest", swatch: "linear-gradient(135deg,#166534,#4ade80)" },
  { value: "ember", label: "Ember", swatch: "linear-gradient(135deg,#c2410c,#fb7185)" },
];

const densityOptions = [
  { value: "comfortable", label: "Comfortable", description: "More whitespace and slower reading rhythm." },
  { value: "compact", label: "Compact", description: "Tighter spacing closer to export density." },
];

function updateAppearance(field, value) {
  emit("update:appearance", {
    ...props.appearance,
    [field]: value,
  });
}
</script>

<template>
  <section
    data-testid="editor-style-pane"
    class="space-y-6 rounded-[28px] border border-slate-200 bg-surface-container-lowest p-5"
  >
    <header class="space-y-2 border-b border-slate-100 pb-5">
      <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-slate-500">Style direction</p>
      <h3 class="font-headline text-2xl font-extrabold tracking-tight text-on-surface">Preview system</h3>
      <p class="max-w-2xl text-sm text-slate-600">
        Switch the saved template and tune live preview tokens for the editor workspace.
      </p>
    </header>

    <section class="space-y-4">
      <div class="flex items-start justify-between gap-3">
        <div>
          <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-slate-500">Template</p>
          <h4 class="mt-1 text-lg font-semibold tracking-tight text-slate-900">Layout family</h4>
        </div>
        <span class="rounded-full bg-slate-100 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] text-slate-600">
          Saved
        </span>
      </div>

      <div v-if="loading" data-testid="editor-style-loading" class="text-sm text-slate-500">Loading templates...</div>
      <div v-else-if="error" data-testid="editor-style-error" class="text-sm text-red-600">{{ error }}</div>

      <div v-else class="grid gap-3 md:grid-cols-3">
        <button
          v-for="template in templates"
          :key="template.key"
          type="button"
          class="rounded-[24px] border p-4 text-left transition"
          :class="metadata.templateKey === template.key ? 'border-primary bg-primary/5 shadow-sm' : 'border-slate-200 bg-white hover:border-slate-300'"
          :data-testid="`editor-style-template-${template.key}`"
          @click="emit('update:template-key', template.key)"
        >
          <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-slate-500">{{ template.key }}</p>
          <h5 class="mt-2 text-base font-semibold tracking-tight text-slate-900">
            {{ template.displayName || template.key }}
          </h5>
          <p class="mt-2 text-sm text-slate-600">
            {{ metadata.templateKey === template.key ? "Current export layout." : "Switch to this layout." }}
          </p>
        </button>
      </div>
    </section>

    <section class="space-y-4">
      <div>
        <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-slate-500">Canvas surface</p>
        <h4 class="mt-1 text-lg font-semibold tracking-tight text-slate-900">Workspace mood</h4>
      </div>
      <div class="grid gap-3 md:grid-cols-3">
        <button
          v-for="option in surfaceOptions"
          :key="option.value"
          type="button"
          class="rounded-[22px] border p-4 text-left transition"
          :class="appearance.surface === option.value ? 'border-primary bg-primary/5' : 'border-slate-200 bg-white hover:border-slate-300'"
          :data-testid="`editor-style-surface-${option.value}`"
          @click="updateAppearance('surface', option.value)"
        >
          <p class="text-sm font-semibold text-slate-900">{{ option.label }}</p>
          <p class="mt-2 text-sm text-slate-600">{{ option.description }}</p>
        </button>
      </div>
    </section>

    <section class="space-y-4">
      <div>
        <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-slate-500">Accent system</p>
        <h4 class="mt-1 text-lg font-semibold tracking-tight text-slate-900">Theme color</h4>
      </div>
      <div class="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
        <button
          v-for="option in accentOptions"
          :key="option.value"
          type="button"
          class="rounded-[22px] border p-3 text-left transition"
          :class="appearance.accent === option.value ? 'border-primary bg-primary/5' : 'border-slate-200 bg-white hover:border-slate-300'"
          :data-testid="`editor-style-accent-${option.value}`"
          @click="updateAppearance('accent', option.value)"
        >
          <span class="block h-10 rounded-2xl" :style="{ background: option.swatch }" />
          <p class="mt-3 text-sm font-semibold text-slate-900">{{ option.label }}</p>
        </button>
      </div>
    </section>

    <section class="space-y-4">
      <div>
        <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-slate-500">Density</p>
        <h4 class="mt-1 text-lg font-semibold tracking-tight text-slate-900">Reading rhythm</h4>
      </div>
      <div class="grid gap-3 md:grid-cols-2">
        <button
          v-for="option in densityOptions"
          :key="option.value"
          type="button"
          class="rounded-[22px] border p-4 text-left transition"
          :class="appearance.density === option.value ? 'border-primary bg-primary/5' : 'border-slate-200 bg-white hover:border-slate-300'"
          :data-testid="`editor-style-density-${option.value}`"
          @click="updateAppearance('density', option.value)"
        >
          <p class="text-sm font-semibold text-slate-900">{{ option.label }}</p>
          <p class="mt-2 text-sm text-slate-600">{{ option.description }}</p>
        </button>
      </div>
    </section>
  </section>
</template>
