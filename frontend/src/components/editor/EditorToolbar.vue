<script setup>
const props = defineProps({
  documentTitle: {
    type: String,
    default: "",
  },
  autosaveLabel: {
    type: String,
    default: "",
  },
  activeTab: {
    type: String,
    default: "preview",
  },
  isPublic: {
    type: Boolean,
    default: false,
  },
  exportRunning: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["update:activeTab", "export", "publish"]);
</script>

<template>
  <header
    data-testid="editor-toolbar"
    class="sticky top-16 z-20 rounded-[28px] border border-slate-200 bg-[rgba(251,248,255,0.9)] px-4 py-4 shadow-[0_24px_48px_rgba(26,27,34,0.06)] backdrop-blur sm:px-6"
  >
    <div class="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
      <div class="space-y-1">
        <p class="text-[11px] font-bold uppercase tracking-[0.28em] text-slate-500">
          Architect workspace
        </p>
        <div class="flex flex-wrap items-center gap-3">
          <h1
            data-testid="editor-toolbar-title"
            class="font-headline text-2xl font-extrabold tracking-tight text-on-surface"
          >
            {{ documentTitle || "Untitled resume" }}
          </h1>
          <span
            class="rounded-full px-3 py-1 text-[11px] font-bold uppercase tracking-[0.2em]"
            :class="isPublic ? 'bg-blue-100 text-primary' : 'bg-slate-200 text-slate-600'"
          >
            {{ isPublic ? "Public" : "Private" }}
          </span>
        </div>
        <p data-testid="editor-autosave-status" class="text-sm font-medium text-slate-500">
          {{ autosaveLabel }}
        </p>
      </div>

      <div class="flex flex-col gap-3 md:flex-row md:items-center">
        <div class="inline-flex rounded-full border border-slate-200 bg-white p-1">
          <button
            type="button"
            class="rounded-full px-4 py-2 text-sm font-semibold transition"
            :class="activeTab === 'preview' ? 'bg-primary text-white shadow-sm' : 'text-slate-600 hover:text-primary'"
            data-testid="editor-tab-preview"
            @click="emit('update:activeTab', 'preview')"
          >
            Preview
          </button>
          <button
            type="button"
            class="rounded-full px-4 py-2 text-sm font-semibold transition"
            :class="activeTab === 'style' ? 'bg-primary text-white shadow-sm' : 'text-slate-600 hover:text-primary'"
            data-testid="editor-tab-style"
            @click="emit('update:activeTab', 'style')"
          >
            Style
          </button>
        </div>

        <div class="flex flex-wrap items-center gap-3">
          <button
            type="button"
            class="inline-flex h-11 items-center justify-center rounded-full border border-slate-200 bg-white px-5 text-sm font-semibold text-on-surface transition hover:border-slate-300 hover:bg-slate-50"
            :disabled="exportRunning"
            data-testid="editor-export-button"
            @click="emit('export')"
          >
            {{ exportRunning ? "Exporting..." : "Export PDF" }}
          </button>
          <button
            type="button"
            class="inline-flex h-11 items-center justify-center rounded-xl bg-gradient-to-br from-primary to-primary-container px-5 text-sm font-bold text-on-primary shadow-sm transition hover:opacity-90"
            data-testid="editor-publish-button"
            @click="emit('publish')"
          >
            Publish Resume
          </button>
        </div>
      </div>
    </div>
  </header>
</template>
