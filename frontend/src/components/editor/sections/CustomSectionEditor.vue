<script setup>
const props = defineProps({
  sections: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(["update:sections"]);

function buildSection() {
  return {
    id: null,
    sectionType: "custom",
    sortOrder: props.sections.length,
    title: "",
    content: {
      text: "",
    },
  };
}

function addSection() {
  emit("update:sections", [...props.sections, buildSection()]);
}

function updateSection(index, key, value) {
  const nextSections = props.sections.map((section, sectionIndex) =>
    sectionIndex === index
      ? key === "text"
        ? { ...section, content: { ...section.content, text: value } }
        : { ...section, [key]: value }
      : section,
  );
  emit("update:sections", nextSections);
}

function removeSection(index) {
  emit(
    "update:sections",
    props.sections.filter((_, sectionIndex) => sectionIndex !== index),
  );
}
</script>

<template>
  <section class="space-y-4" data-testid="section-editor-custom">
    <div class="flex items-center justify-between gap-3">
      <div>
        <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-slate-500">Extensions</p>
        <h3 class="mt-1 font-headline text-lg font-extrabold tracking-tight text-on-surface">Custom sections</h3>
      </div>
      <button
        type="button"
        class="text-sm font-bold text-primary transition hover:opacity-80"
        data-testid="section-custom-add"
        @click="addSection"
      >
        + Add Section
      </button>
    </div>

    <div class="space-y-4">
      <article
        v-for="(section, index) in sections"
        :key="`${section.id || 'custom'}-${index}`"
        class="space-y-3 rounded-[24px] border border-slate-200 bg-white p-4"
      >
        <input
          :value="section.title"
          class="h-11 w-full rounded-2xl border border-slate-200 bg-surface-container-low px-4 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/10"
          placeholder="Section title"
          @input="updateSection(index, 'title', $event.target.value)"
        />
        <textarea
          :value="section.content?.text || ''"
          rows="3"
          class="w-full rounded-[20px] border border-slate-200 bg-surface-container-low px-4 py-3 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/10"
          placeholder="Section content"
          @input="updateSection(index, 'text', $event.target.value)"
        />
        <div class="flex justify-end">
          <button
            type="button"
            class="text-sm font-semibold text-red-600 transition hover:opacity-80"
            @click="removeSection(index)"
          >
            Remove
          </button>
        </div>
      </article>
    </div>
  </section>
</template>
