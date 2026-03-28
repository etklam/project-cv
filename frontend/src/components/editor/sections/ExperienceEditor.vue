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
    company: "",
    role: "",
    startDate: "",
    endDate: "",
    description: "",
    current: false,
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
  <section class="space-y-4" data-testid="section-editor-experience">
    <div class="flex items-center justify-between gap-3">
      <div>
        <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-slate-500">Career history</p>
        <h3 class="mt-1 font-headline text-lg font-extrabold tracking-tight text-on-surface">Experience</h3>
      </div>
      <button
        type="button"
        class="text-sm font-bold text-primary transition hover:opacity-80"
        data-testid="section-experience-add"
        @click="addItem"
      >
        + Add Position
      </button>
    </div>

    <div class="space-y-4">
      <article
        v-for="(item, index) in items"
        :key="`${item.company}-${item.role}-${index}`"
        class="space-y-4 rounded-[24px] border border-slate-200 bg-white p-4"
      >
        <div class="grid gap-4 md:grid-cols-2">
          <input
            :value="item.role"
            class="h-11 rounded-2xl border border-slate-200 bg-surface-container-low px-4 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/10"
            placeholder="Role"
            @input="updateItem(index, 'role', $event.target.value)"
          />
          <input
            :value="item.company"
            class="h-11 rounded-2xl border border-slate-200 bg-surface-container-low px-4 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/10"
            placeholder="Company"
            @input="updateItem(index, 'company', $event.target.value)"
          />
          <input
            :value="item.startDate"
            class="h-11 rounded-2xl border border-slate-200 bg-surface-container-low px-4 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/10"
            placeholder="Start date"
            @input="updateItem(index, 'startDate', $event.target.value)"
          />
          <input
            :value="item.endDate"
            class="h-11 rounded-2xl border border-slate-200 bg-surface-container-low px-4 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/10"
            placeholder="End date"
            @input="updateItem(index, 'endDate', $event.target.value)"
          />
        </div>
        <textarea
          :value="item.description"
          rows="3"
          class="w-full rounded-[20px] border border-slate-200 bg-surface-container-low px-4 py-3 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/10"
          placeholder="Describe impact, ownership, and outcomes"
          @input="updateItem(index, 'description', $event.target.value)"
        />
        <div class="flex items-center justify-between">
          <label class="inline-flex items-center gap-2 text-sm font-medium text-slate-600">
            <input
              :checked="item.current"
              type="checkbox"
              class="h-4 w-4 rounded border-slate-300 text-primary focus:ring-primary/20"
              @change="updateItem(index, 'current', $event.target.checked)"
            />
            Current role
          </label>
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
        No experience entries yet.
      </div>
    </div>
  </section>
</template>
