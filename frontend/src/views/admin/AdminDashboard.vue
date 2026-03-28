<script setup>
import { ref, onMounted } from "vue";
import { adminApi } from "@/api/admin";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const stats = ref({
  totalUsers: 0,
  totalPromoCodes: 0,
  totalTransactions: 0,
  activeTemplates: 0,
});

const loading = ref(true);

onMounted(async () => {
  try {
    const usersData = await adminApi.listUsers({ page: 1, size: 1 });
    stats.value.totalUsers = usersData.total;

    const promosData = await adminApi.listPromoCodes({ page: 1, size: 1 });
    stats.value.totalPromoCodes = promosData.total;

    const templatesData = await adminApi.listAllTemplates();
    stats.value.activeTemplates = templatesData.filter((t) => t.isActive).length;

    const transactionsData = await adminApi.listTransactions({ page: 1, size: 1 });
    stats.value.totalTransactions = transactionsData.total;
  } catch (error) {
    console.error("Failed to load stats:", error);
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="admin-dashboard">
    <h1 class="text-2xl font-bold text-gray-800 mb-6">{{ $t("admin.dashboard.title") }}</h1>

    <div v-if="loading" class="text-center py-12">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600"></div>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <!-- Total Users -->
      <div class="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">{{ $t("admin.dashboard.totalUsers") }}</p>
            <p class="text-3xl font-bold text-gray-800">{{ stats.totalUsers }}</p>
          </div>
          <div class="p-3 bg-blue-100 rounded-full">
            <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
            </svg>
          </div>
        </div>
      </div>

      <!-- Active PromoCodes -->
      <div class="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">{{ $t("admin.dashboard.totalPromoCodes") }}</p>
            <p class="text-3xl font-bold text-gray-800">{{ stats.totalPromoCodes }}</p>
          </div>
          <div class="p-3 bg-green-100 rounded-full">
            <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
            </svg>
          </div>
        </div>
      </div>

      <!-- Active Templates -->
      <div class="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">{{ $t("admin.dashboard.activeTemplates") }}</p>
            <p class="text-3xl font-bold text-gray-800">{{ stats.activeTemplates }}</p>
          </div>
          <div class="p-3 bg-purple-100 rounded-full">
            <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 5a1 1 0 011-1h14a1 1 0 011 1v2a1 1 0 01-1 1H5a1 1 0 01-1-1V5zM4 13a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H5a1 1 0 01-1-1v-6zM16 13a1 1 0 011-1h2a1 1 0 011 1v6a1 1 0 01-1 1h-2a1 1 0 01-1-1v-6z" />
            </svg>
          </div>
        </div>
      </div>

      <!-- Total Transactions -->
      <div class="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">{{ $t("admin.dashboard.totalTransactions") }}</p>
            <p class="text-3xl font-bold text-gray-800">{{ stats.totalTransactions }}</p>
          </div>
          <div class="p-3 bg-yellow-100 rounded-full">
            <svg class="w-6 h-6 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
