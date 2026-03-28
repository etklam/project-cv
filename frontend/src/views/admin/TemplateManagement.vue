<script setup>
import { onMounted, ref, computed } from "vue";
import { adminApi } from "@/api/admin";

const templates = ref([]);
const loading = ref(false);

const summary = computed(() => ({
  total: templates.value.length,
  active: templates.value.filter((template) => template.isActive).length,
}));

const loadTemplates = async () => {
  loading.value = true;
  try {
    const data = await adminApi.listAllTemplates();
    templates.value = data;
  } catch (error) {
    console.error("Failed to load templates:", error);
  } finally {
    loading.value = false;
  }
};

const toggleStatus = async (id) => {
  try {
    const template = templates.value.find((item) => item.id === id);
    await adminApi.toggleTemplateStatus(id, !template.isActive);
    await loadTemplates();
  } catch (error) {
    console.error("Failed to toggle status:", error);
  }
};

onMounted(loadTemplates);
</script>

<template>
  <section class="space-y-6">
    <header class="grid gap-5 rounded-[32px] border border-slate-200 bg-white px-6 py-6 shadow-sm sm:px-8 sm:py-8 lg:grid-cols-[1.2fr_0.8fr]">
      <div class="space-y-4">
        <span class="inline-flex w-fit rounded-full bg-blue-50 px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.24em] text-blue-700">
          Templates
        </span>
        <div class="space-y-3">
          <h2 class="admin-display text-4xl font-semibold tracking-[-0.05em] text-slate-950">
            {{ $t("admin.templates.title") }}
          </h2>
          <p class="max-w-2xl text-sm leading-7 text-slate-600">
            Keep the template catalog consistent and make active states easy to scan.
          </p>
        </div>
      </div>

      <div class="flex flex-wrap gap-3 lg:justify-end">
        <div class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
          <p class="text-[11px] font-semibold uppercase tracking-[0.24em] text-slate-500">Active</p>
          <p class="mt-1 text-sm font-semibold text-slate-950">{{ summary.active }}</p>
        </div>
        <div class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
          <p class="text-[11px] font-semibold uppercase tracking-[0.24em] text-slate-500">Total</p>
          <p class="mt-1 text-sm font-semibold text-slate-950">{{ summary.total }}</p>
        </div>
      </div>
    </header>

    <div v-if="loading" class="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm" aria-busy="true">
      <div class="grid gap-3">
        <div v-for="index in 4" :key="index" class="h-12 animate-pulse rounded-2xl bg-slate-100"></div>
      </div>
    </div>

    <div v-else class="overflow-hidden rounded-[28px] border border-slate-200 bg-white shadow-sm">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-slate-200">
          <thead class="bg-slate-50">
            <tr>
              <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">Key</th>
              <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                {{ $t("admin.templates.displayName") }}
              </th>
              <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                {{ $t("admin.templates.creditCost") }}
              </th>
              <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                {{ $t("admin.templates.sortOrder") }}
              </th>
              <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                {{ $t("admin.templates.isActive") }}
              </th>
              <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                {{ $t("admin.users.actions") }}
              </th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-200 bg-white">
            <tr v-for="template in templates" :key="template.id" class="hover:bg-slate-50/80">
              <td class="px-6 py-4 text-sm font-mono font-semibold text-slate-950">{{ template.componentKey }}</td>
              <td class="px-6 py-4 text-sm text-slate-600">{{ template.displayNameI18n }}</td>
              <td class="px-6 py-4 text-sm font-semibold text-slate-950">{{ template.creditCost }}</td>
              <td class="px-6 py-4 text-sm text-slate-600">{{ template.sortOrder }}</td>
              <td class="px-6 py-4">
                <button
                  type="button"
                  @click="toggleStatus(template.id)"
                  :class="template.isActive ? 'border-blue-200 bg-blue-50 text-blue-700' : 'border-slate-200 bg-slate-50 text-slate-500'"
                  class="inline-flex rounded-full border px-3 py-1 text-xs font-semibold uppercase tracking-[0.2em] transition hover:border-blue-700 hover:bg-blue-700 hover:text-white"
                >
                  {{ template.isActive ? $t("common.active") : $t("common.inactive") }}
                </button>
              </td>
              <td class="px-6 py-4 text-sm text-slate-500">-</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="!templates.length" class="border-t border-slate-200 px-6 py-16 text-center">
        <p class="text-sm font-medium text-slate-500">No templates are available.</p>
      </div>
    </div>
  </section>
</template>
