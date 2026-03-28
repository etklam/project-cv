<script setup>
import { computed } from "vue";
import CvTemplateRenderer from "@/components/cv-templates/CvTemplateRenderer.vue";

const props = defineProps({
  activeTab: {
    type: String,
    default: "preview",
  },
  isPublic: {
    type: Boolean,
    default: false,
  },
  publicPath: {
    type: String,
    default: "",
  },
  zoom: {
    type: Number,
    default: 85,
  },
  cv: {
    type: Object,
    default: () => ({}),
  },
  sections: {
    type: Array,
    default: () => [],
  },
  appearance: {
    type: Object,
    default: () => ({}),
  },
  exportMessage: {
    type: String,
    default: "",
  },
  exportError: {
    type: String,
    default: "",
  },
});

const emit = defineEmits(["zoom-in", "zoom-out", "export"]);

const surfaceClass = computed(() => {
  switch (props.appearance?.surface) {
    case "daylight":
      return "bg-[linear-gradient(180deg,rgba(244,248,255,0.95),rgba(233,240,251,0.95))]";
    case "midnight":
      return "bg-[linear-gradient(180deg,rgba(15,23,42,0.96),rgba(30,41,59,0.94))]";
    default:
      return "bg-[linear-gradient(180deg,rgba(239,237,246,0.6),rgba(227,225,234,0.92))]";
  }
});

const paperClass = computed(() => {
  switch (props.appearance?.surface) {
    case "midnight":
      return "bg-slate-50";
    case "daylight":
      return "bg-white";
    default:
      return "bg-white/95";
  }
});

const paperPaddingClass = computed(() =>
  props.appearance?.density === "compact" ? "p-6" : "p-8",
);

const previewThemeStyle = computed(() => {
  const accentThemes = {
    ink: {
      accent: "#334155",
      accentSoft: "#e2e8f0",
      accentStrong: "#0f172a",
      textMuted: "#475569",
      border: "#cbd5e1",
      aside: "#f8fafc",
    },
    ocean: {
      accent: "#0284c7",
      accentSoft: "#e0f2fe",
      accentStrong: "#0f172a",
      textMuted: "#0f4c81",
      border: "#bae6fd",
      aside: "#f0f9ff",
    },
    forest: {
      accent: "#15803d",
      accentSoft: "#dcfce7",
      accentStrong: "#14532d",
      textMuted: "#166534",
      border: "#bbf7d0",
      aside: "#f0fdf4",
    },
    ember: {
      accent: "#ea580c",
      accentSoft: "#ffedd5",
      accentStrong: "#7c2d12",
      textMuted: "#9a3412",
      border: "#fed7aa",
      aside: "#fff7ed",
    },
  };

  const theme = accentThemes[props.appearance?.accent] || accentThemes.ink;
  return {
    "--editor-theme-accent": theme.accent,
    "--editor-theme-accent-soft": theme.accentSoft,
    "--editor-theme-accent-strong": theme.accentStrong,
    "--editor-theme-text-muted": theme.textMuted,
    "--editor-theme-border": theme.border,
    "--editor-theme-aside": theme.aside,
  };
});
</script>

<template>
  <aside
    data-testid="editor-preview-pane"
    class="min-h-0 rounded-[32px] border border-slate-200 bg-surface-container-highest p-4 shadow-[0_24px_48px_rgba(26,27,34,0.05)] sm:p-5"
  >
    <div class="flex h-full flex-col gap-4">
      <div class="flex flex-wrap items-center justify-between gap-3 rounded-[24px] bg-white/70 px-4 py-3 backdrop-blur">
        <div class="inline-flex items-center gap-2 rounded-full bg-surface-container-low px-3 py-2">
          <span
            class="h-2.5 w-2.5 rounded-full"
            :class="isPublic ? 'bg-emerald-500' : 'bg-slate-400'"
          />
          <span class="text-xs font-bold uppercase tracking-[0.18em] text-slate-600">
            {{ isPublic ? "Public" : "Private" }}
          </span>
        </div>

        <div class="flex items-center gap-2">
          <button
            type="button"
            class="flex h-10 w-10 items-center justify-center rounded-full border border-slate-200 bg-white text-primary transition hover:scale-105"
            data-testid="editor-preview-zoom-out"
            @click="emit('zoom-out')"
          >
            -
          </button>
          <span data-testid="editor-preview-zoom-label" class="min-w-14 text-center text-xs font-bold uppercase tracking-[0.18em] text-slate-500">
            {{ zoom }}%
          </span>
          <button
            type="button"
            class="flex h-10 w-10 items-center justify-center rounded-full border border-slate-200 bg-white text-primary transition hover:scale-105"
            data-testid="editor-preview-zoom-in"
            @click="emit('zoom-in')"
          >
            +
          </button>
        </div>
      </div>

      <p
        v-if="publicPath"
        data-testid="editor-public-path"
        class="rounded-2xl bg-white/70 px-4 py-3 text-sm text-slate-600"
      >
        {{ publicPath }}
      </p>

      <div
        v-if="activeTab === 'style'"
        data-testid="editor-style-preview-status"
        class="grid gap-3 rounded-[28px] border border-slate-200 bg-white/80 px-4 py-4 text-sm text-slate-600 sm:grid-cols-3"
      >
        <div class="rounded-2xl bg-slate-50 px-4 py-3">
          <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-slate-500">Template</p>
          <p class="mt-2 font-semibold text-slate-900">{{ cv?.templateKey || "minimal" }}</p>
        </div>
        <div class="rounded-2xl bg-slate-50 px-4 py-3">
          <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-slate-500">Accent</p>
          <p class="mt-2 font-semibold capitalize text-slate-900">{{ appearance?.accent || "ink" }}</p>
        </div>
        <div class="rounded-2xl bg-slate-50 px-4 py-3">
          <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-slate-500">Density</p>
          <p class="mt-2 font-semibold capitalize text-slate-900">{{ appearance?.density || "comfortable" }}</p>
        </div>
      </div>

      <div
        class="min-h-0 flex-1 overflow-auto rounded-[28px] p-4 transition-colors duration-200"
        :class="surfaceClass"
      >
        <div class="flex min-h-full items-start justify-center">
          <div
            data-testid="editor-preview-canvas"
            class="origin-top"
            :style="{ width: '595px', minHeight: '842px', transform: `scale(${zoom / 100})`, transformOrigin: 'top center' }"
          >
            <div
              class="min-h-[842px] rounded-[20px] shadow-[0_24px_48px_rgba(26,27,34,0.08)] transition-all duration-200"
              :class="[paperClass, paperPaddingClass]"
              :style="previewThemeStyle"
            >
              <CvTemplateRenderer
                :template-key="cv?.templateKey || 'minimal'"
                :cv="cv"
                :sections="sections"
                :appearance="appearance"
                mode="editor-preview"
              />
            </div>
          </div>
        </div>
      </div>

      <p v-if="exportMessage" data-testid="editor-export-message" class="text-sm font-medium text-emerald-600">
        {{ exportMessage }}
      </p>
      <p v-if="exportError" data-testid="editor-export-error" class="text-sm font-medium text-red-600">
        {{ exportError }}
      </p>
    </div>
  </aside>
</template>
