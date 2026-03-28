<script setup>
import { ref, onMounted } from "vue";
import { adminApi } from "@/api/admin";

const users = ref([]);
const pagination = ref({ page: 1, size: 20, total: 0 });
const loading = ref(false);
const searchQuery = ref("");

const loadUsers = async () => {
  loading.value = true;
  try {
    const data = await adminApi.listUsers({
      page: pagination.value.page,
      size: pagination.value.size,
      search: searchQuery.value || undefined,
    });
    users.value = data.users;
    pagination.value.total = data.total;
  } catch (error) {
    console.error("Failed to load users:", error);
  } finally {
    loading.value = false;
  }
};

const updateUserRole = async (userId, newRole) => {
  try {
    await adminApi.updateUserRole(userId, newRole);
    await loadUsers();
  } catch (error) {
    console.error("Failed to update role:", error);
  }
};

const adjustCredits = async (userId, delta, reason) => {
  try {
    await adminApi.adjustCredits(userId, delta, reason);
    await loadUsers();
  } catch (error) {
    console.error("Failed to adjust credits:", error);
  }
};

onMounted(loadUsers);
</script>

<template>
  <div class="user-management">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-800">{{ $t("admin.users.title") }}</h1>
      <input
        v-model="searchQuery"
        @keyup.enter="loadUsers"
        type="text"
        :placeholder="$t('admin.users.search')"
        class="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
      />
    </div>

    <div v-if="loading" class="text-center py-12">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600"></div>
    </div>

    <div v-else class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ $t("admin.users.email") }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ $t("admin.users.displayName") }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ $t("admin.users.role") }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ $t("admin.users.credits") }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ $t("admin.users.actions") }}</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="user in users" :key="user.id">
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ user.email }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ user.displayName }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <select
                :value="user.role"
                @change="updateUserRole(user.id, $event.target.value)"
                class="text-sm border rounded px-2 py-1"
                :class="{
                  'bg-green-100 text-green-800': user.role === 'ADMIN',
                  'bg-gray-100 text-gray-800': user.role === 'USER',
                }"
              >
                <option value="USER">USER</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ user.creditBalance }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">
              <button
                @click="adjustCredits(user.id, 100, 'Admin bonus')"
                class="text-green-600 hover:text-green-800 mr-2"
              >
                +100
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
