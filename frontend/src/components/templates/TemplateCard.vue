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

const previewAlt = (template) => `${template.displayName || template.key} preview`;
</script>

<template>
  <article
    :class="[
      'group flex h-full cursor-pointer flex-col overflow-hidden rounded-3xl border bg-white transition-colors duration-200',
      props.selected
        ? 'border-[#18181B] bg-[#18181B]/[0.03]'
        : 'border-gray-200 hover:border-gray-300',
    ]"
    :data-testid="`template-card-${props.template.key}`"
    role="button"
    tabindex="0"
    @keydown.enter.prevent="$event.currentTarget?.click()"
    @keydown.space.prevent="$event.currentTarget?.click()"
  >
    <div v-if="props.template.previewImagePath" class="border-b border-gray-100 bg-gray-50">
      <img
        :src="props.template.previewImagePath"
        :alt="previewAlt(props.template)"
        class="aspect-[16/10] w-full object-cover"
      />
    </div>

    <div class="flex flex-1 flex-col gap-4 p-4 sm:p-5">
      <div class="flex items-start justify-between gap-3">
        <div class="min-w-0">
          <h3 class="truncate text-base font-semibold tracking-tight text-[#09090B]">
            {{ props.template.displayName || props.template.key }}
          </h3>
          <p class="mt-1 text-sm text-gray-500">
            {{ props.template.description || "No description provided yet." }}
          </p>
        </div>

        <span
          class="shrink-0 rounded-full border border-[#18181B]/10 px-3 py-1 text-xs font-semibold text-[#18181B]"
        >
          {{ props.template.creditCost }} credits
        </span>
      </div>

      <div class="mt-auto flex items-center justify-between gap-3">
        <span class="text-xs font-medium uppercase tracking-[0.18em] text-gray-400">
          {{ props.template.key }}
        </span>
        <span
          v-if="props.selected"
          class="rounded-full bg-[#EC4899]/10 px-3 py-1 text-xs font-semibold text-[#BE185D]"
        >
          Selected
        </span>
      </div>
    </div>
  </article>
</template>
