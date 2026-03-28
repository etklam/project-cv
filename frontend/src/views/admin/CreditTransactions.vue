<script setup>
import { ref, onMounted } from "vue";
import { adminApi } from "@/api/admin";

const transactions = ref([]);
const pagination = ref({ page: 1, size: 20, total: 0 });
const loading = ref(false);

const loadTransactions = async () => {
  loading.value = true;
  try {
    const data = await adminApi.listTransactions({
      page: pagination.value.page,
      size: pagination.value.size,
    });
    transactions.value = data.transactions;
    pagination.value.total = data.total;
  } catch (error) {
    console.error("Failed to load transactions:", error);
  } finally {
    loading.value = false;
  }
};

onMounted(loadTransactions);
</script>

<template>
  <div class="credit-transactions">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-800">{{ $t("admin.credits.title") }}</h1>
    </div>

    <div v-if="loading" class="text-center py-12">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600"></div>
    </div>

    <div v-else class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ $t("admin.credits.transactionId") }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ $t("admin.credits.user") }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ $t("admin.credits.amount") }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ $t("admin.credits.balanceAfter") }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ $t("admin.credits.type") }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ $t("admin.credits.createdAt") }}</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="tx in transactions" :key="tx.id">
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">#{{ tx.id }}</td>
            <td class="px-6 py-4 text-sm text-gray-900">
              <div>{{ tx.userName || tx.userEmail }}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm" :class="tx.amount > 0 ? 'text-green-600' : 'text-red-600'">
              {{ tx.amount > 0 ? '+' : '' }}{{ tx.amount }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ tx.balanceAfter }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ tx.type }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ tx.createdAt }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
