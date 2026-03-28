<script setup>
import { ref, onMounted } from "vue";
import { adminApi } from "@/api/admin";

const templates = ref([]);
const loading = ref(false);

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
    const template = templates.value.find((t) => t.id === id);
    await adminApi.toggleTemplateStatus(id, !template.isActive);
    await loadTemplates();
  } catch (error) {
    console.error("Failed to toggle status:", error);
  }
};

onMounted(loadTemplates);
</script>

<template>
  <div class="template-management">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-800">{{ $t("admin.templates.title") }}</h1>
    </div>

    <div v-if="loading" class="text-center py-12">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600"></div>
    </div>

    <div v-else class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Key</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ $t("admin.templates.displayName") }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ $t("admin.templates.creditCost") }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ $t("admin.templates.sortOrder") }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ $t("admin.templates.isActive") }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ $t("admin.users.actions") }}</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="template in templates" :key="template.id">
            <td class="px-6 py-4 whitespace-nowrap text-sm font-mono text-gray-900">{{ template.componentKey }}</td>
            <td class="px-6 py-4 text-sm text-gray-900">{{ template.displayNameI18n }}</td>
            <td class="px-6 py-4 text-sm text-gray-900">{{ template.creditCost }}</td>
            <td class="px-6 py-4 text-sm text-gray-900">{{ template.sortOrder }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <button
                @click="toggleStatus(template.id)"
                :class="template.isActive ? 'text-green-600' : 'text-red-600'"
                class="text-sm"
              >
                {{ template.isActive ? $t("common.active") : $t("common.inactive") }}
              </button>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">-</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
