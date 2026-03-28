<script setup>
import { computed } from "vue";

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({}),
  },
  saving: {
    type: Boolean,
    default: false,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  errors: {
    type: Object,
    default: () => ({}),
  },
});

const emit = defineEmits(["update:modelValue", "submit", "reset"]);

const contact = computed(() => ({
  displayName: props.modelValue?.displayName || "",
  headline: props.modelValue?.headline || "",
  email: props.modelValue?.email || "",
  location: props.modelValue?.location || "",
  website: props.modelValue?.website || "",
}));

function updateField(field, value) {
  emit("update:modelValue", {
    ...contact.value,
    [field]: value,
  });
}
</script>

<template>
  <section data-testid="editor-contact-editor" class="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
    <div class="flex items-start justify-between gap-4">
      <div>
        <p class="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">Contact</p>
        <h3 class="mt-2 text-lg font-semibold tracking-tight text-slate-900">Contact Details</h3>
      </div>
      <div class="flex gap-2">
        <button
          type="button"
          class="rounded-full border border-slate-200 bg-white px-3 py-1.5 text-xs font-medium text-slate-700"
          :disabled="disabled"
          data-testid="editor-contact-reset"
          @click="emit('reset')"
        >
          Reset
        </button>
        <button
          type="button"
          class="rounded-full bg-slate-900 px-3 py-1.5 text-xs font-semibold text-white disabled:cursor-not-allowed disabled:opacity-60"
          :disabled="saving || disabled"
          data-testid="editor-contact-save"
          @click="emit('submit')"
        >
          {{ saving ? "Saving..." : "Save" }}
        </button>
      </div>
    </div>

    <div class="mt-5 grid gap-4 sm:grid-cols-2">
      <label class="grid gap-2">
        <span class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">Display Name</span>
        <input
          class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-slate-400"
          data-testid="editor-contact-display-name"
          :value="contact.displayName"
          :disabled="disabled"
          @input="updateField('displayName', $event.target.value)"
        />
      </label>

      <label class="grid gap-2">
        <span class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">Headline</span>
        <input
          class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-slate-400"
          data-testid="editor-contact-headline"
          :value="contact.headline"
          :disabled="disabled"
          @input="updateField('headline', $event.target.value)"
        />
      </label>

      <label class="grid gap-2">
        <span class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">Email</span>
        <input
          class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-slate-400"
          data-testid="editor-contact-email"
          :value="contact.email"
          :disabled="disabled"
          @input="updateField('email', $event.target.value)"
        />
      </label>

      <label class="grid gap-2">
        <span class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">Location</span>
        <input
          class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-slate-400"
          data-testid="editor-contact-location"
          :value="contact.location"
          :disabled="disabled"
          @input="updateField('location', $event.target.value)"
        />
      </label>

      <label class="grid gap-2 sm:col-span-2">
        <span class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">Website</span>
        <input
          class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-slate-400"
          data-testid="editor-contact-website"
          :value="contact.website"
          :disabled="disabled"
          @input="updateField('website', $event.target.value)"
        />
      </label>
    </div>

    <p v-if="Object.keys(errors || {}).length" class="mt-4 text-sm text-rose-600" data-testid="editor-contact-errors">
      Contact fields contain validation errors.
    </p>
  </section>
</template>
