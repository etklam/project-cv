<script setup>
const props = defineProps({
  items: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(["update:items"]);

function buildItem() {
  return {
    school: "",
    degree: "",
    major: "",
    startDate: "",
    endDate: "",
  };
}

function updateItem(index, key, value) {
  const nextItems = props.items.map((item, itemIndex) =>
    itemIndex === index ? { ...item, [key]: value } : item,
  );
  emit("update:items", nextItems);
}

function addItem() {
  emit("update:items", [...props.items, buildItem()]);
}

function removeItem(index) {
  emit(
    "update:items",
    props.items.filter((_, itemIndex) => itemIndex !== index),
  );
}
</script>

<template>
  <section class="space-y-4" data-testid="section-editor-education">
    <div class="flex items-center justify-between gap-3">
      <div>
        <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-slate-500">Foundation</p>
        <h3 class="mt-1 font-headline text-lg font-extrabold tracking-tight text-on-surface">Education</h3>
      </div>
      <button
        type="button"
        class="text-sm font-bold text-primary transition hover:opacity-80"
        data-testid="section-education-add"
        @click="addItem"
      >
        + Add Education
      </button>
    </div>

    <div class="space-y-4">
      <article
        v-for="(item, index) in items"
        :key="`${item.school}-${item.degree}-${index}`"
        class="grid gap-4 rounded-[24px] border border-slate-200 bg-white p-4 md:grid-cols-2"
      >
        <input
          :value="item.school"
          class="h-11 rounded-2xl border border-slate-200 bg-surface-container-low px-4 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/10"
          placeholder="School"
          @input="updateItem(index, 'school', $event.target.value)"
        />
        <input
          :value="item.degree"
          class="h-11 rounded-2xl border border-slate-200 bg-surface-container-low px-4 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/10"
          placeholder="Degree"
          @input="updateItem(index, 'degree', $event.target.value)"
        />
        <input
          :value="item.major"
          class="h-11 rounded-2xl border border-slate-200 bg-surface-container-low px-4 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/10"
          placeholder="Major"
          @input="updateItem(index, 'major', $event.target.value)"
        />
        <div class="grid grid-cols-2 gap-4">
          <input
            :value="item.startDate"
            class="h-11 rounded-2xl border border-slate-200 bg-surface-container-low px-4 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/10"
            placeholder="Start"
            @input="updateItem(index, 'startDate', $event.target.value)"
          />
          <input
            :value="item.endDate"
            class="h-11 rounded-2xl border border-slate-200 bg-surface-container-low px-4 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/10"
            placeholder="End"
            @input="updateItem(index, 'endDate', $event.target.value)"
          />
        </div>
        <div class="md:col-span-2 flex justify-end">
          <button
            type="button"
            class="text-sm font-semibold text-red-600 transition hover:opacity-80"
            @click="removeItem(index)"
          >
            Remove
          </button>
        </div>
      </article>

      <div
        v-if="!items.length"
        class="rounded-[24px] border border-dashed border-slate-300 bg-surface-container-low px-4 py-5 text-sm text-slate-500"
      >
        No education entries yet.
      </div>
    </div>
  </section>
</template>
