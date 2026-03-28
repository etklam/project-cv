<script setup>
import { ref, onMounted } from "vue";
import { adminApi } from "@/api/admin";

const promoCodes = ref([]);
const loading = ref(false);
const showCreateModal = ref(false);
const newCode = ref({ code: "", rewardValue: 50, maxRedemptions: null });

const loadPromoCodes = async () => {
  loading.value = true;
  try {
    const data = await adminApi.listPromoCodes({ page: 1, size: 100 });
    promoCodes.value = data.promoCodes;
  } catch (error) {
    console.error("Failed to load promo codes:", error);
  } finally {
    loading.value = false;
  }
};

const createPromoCode = async () => {
  try {
    await adminApi.createPromoCode({
      code: newCode.value.code,
      rewardValue: newCode.value.rewardValue,
      maxRedemptions: newCode.value.maxRedemptions,
    });
    showCreateModal.value = false;
    newCode.value = { code: "", rewardValue: 50, maxRedemptions: null };
    await loadPromoCodes();
  } catch (error) {
    console.error("Failed to create promo code:", error);
  }
};

const deletePromoCode = async (id) => {
  if (!confirm("Are you sure?")) return;
  try {
    await adminApi.deletePromoCode(id);
    await loadPromoCodes();
  } catch (error) {
    console.error("Failed to delete promo code:", error);
  }
};

onMounted(loadPromoCodes);
</script>

<template>
  <div class="promocode-management">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-800">{{ $t("admin.promocodes.title") }}</h1>
      <button
        @click="showCreateModal = true"
        class="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
      >
        {{ $t("admin.promocodes.create") }}
      </button>
    </div>

    <div v-if="loading" class="text-center py-12">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600"></div>
    </div>

    <div v-else class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Code</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ $t("admin.promocodes.reward") }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ $t("admin.promocodes.maxRedemptions") }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ $t("admin.promocodes.redemptions") }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ $t("admin.promocodes.isActive") }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ $t("admin.users.actions") }}</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="code in promoCodes" :key="code.id">
            <td class="px-6 py-4 whitespace-nowrap text-sm font-mono text-gray-900">{{ code.code }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ code.rewardValue }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ code.maxRedemptions || "∞" }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ code.currentRedemptions }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span :class="code.isActive ? 'text-green-600' : 'text-red-600'" class="text-sm">
                {{ code.isActive ? $t("common.active") : $t("common.inactive") }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">
              <button @click="deletePromoCode(code.id)" class="text-red-600 hover:text-red-800">
                {{ $t("common.delete") }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Create Modal -->
    <div v-if="showCreateModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
      <div class="bg-white rounded-xl p-6 w-full max-w-md">
        <h2 class="text-xl font-bold mb-4">{{ $t("admin.promocodes.create") }}</h2>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700">Code</label>
            <input v-model="newCode.code" type="text" class="mt-1 block w-full border rounded px-3 py-2" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">{{ $t("admin.promocodes.reward") }}</label>
            <input v-model.number="newCode.rewardValue" type="number" class="mt-1 block w-full border rounded px-3 py-2" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">{{ $t("admin.promocodes.maxRedemptions") }}</label>
            <input v-model.number="newCode.maxRedemptions" type="number" class="mt-1 block w-full border rounded px-3 py-2" placeholder="Unlimited" />
          </div>
          <div class="flex justify-end gap-2">
            <button @click="showCreateModal = false" class="px-4 py-2 border rounded">{{ $t("common.cancel") }}</button>
            <button @click="createPromoCode" class="px-4 py-2 bg-purple-600 text-white rounded">{{ $t("common.create") }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
