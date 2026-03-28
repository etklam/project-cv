<script setup>
import { onMounted, ref } from "vue";
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
  <section class="space-y-6">
    <header class="grid gap-5 rounded-[32px] border border-slate-200 bg-white px-6 py-6 shadow-sm sm:px-8 sm:py-8 lg:grid-cols-[1.2fr_0.8fr]">
      <div class="space-y-4">
        <span class="inline-flex w-fit rounded-full bg-blue-50 px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.24em] text-blue-700">
          Users
        </span>
        <div class="space-y-3">
          <h2 class="admin-display text-4xl font-semibold tracking-[-0.05em] text-slate-950">
            {{ $t("admin.users.title") }}
          </h2>
          <p class="max-w-2xl text-sm leading-7 text-slate-600">
            Review accounts, change roles, and adjust balances from a single workspace.
          </p>
        </div>
      </div>

      <aside class="grid gap-3 sm:grid-cols-2 xl:grid-cols-1">
        <div class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
          <p class="text-[11px] font-semibold uppercase tracking-[0.24em] text-slate-500">Results</p>
          <p class="mt-1 text-sm font-semibold text-slate-950">{{ pagination.total }} users</p>
        </div>
        <div class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
          <p class="text-[11px] font-semibold uppercase tracking-[0.24em] text-slate-500">Scope</p>
          <p class="mt-1 text-sm font-semibold text-slate-950">Search + role control</p>
        </div>
      </aside>
    </header>

    <form class="rounded-[28px] border border-slate-200 bg-white p-4 shadow-sm sm:p-6" @submit.prevent="loadUsers">
      <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div class="min-w-0">
          <label class="text-[11px] font-semibold uppercase tracking-[0.24em] text-slate-500">
            Search
          </label>
          <div class="mt-2 flex flex-col gap-3 sm:flex-row sm:items-center">
            <input
              v-model="searchQuery"
              type="text"
              :placeholder="$t('admin.users.search')"
              class="h-11 w-full min-w-0 rounded-2xl border border-slate-200 bg-white px-4 text-sm text-slate-950 placeholder:text-slate-400 focus:border-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-600/10 sm:w-96"
            />
            <button
              type="submit"
              class="inline-flex h-11 items-center justify-center rounded-2xl border border-blue-700 bg-blue-700 px-4 text-sm font-semibold text-white transition hover:bg-blue-800"
            >
              Search
            </button>
          </div>
        </div>

        <div class="flex items-center gap-2 text-sm text-slate-500">
          <span class="rounded-full border border-slate-200 bg-slate-50 px-3 py-2">Press Enter to search</span>
        </div>
      </div>
    </form>

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
              <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                {{ $t("admin.users.email") }}
              </th>
              <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                {{ $t("admin.users.displayName") }}
              </th>
              <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                {{ $t("admin.users.role") }}
              </th>
              <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                {{ $t("admin.users.credits") }}
              </th>
              <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                {{ $t("admin.users.actions") }}
              </th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-200 bg-white">
            <tr v-for="user in users" :key="user.id" class="hover:bg-slate-50/80">
              <td class="px-6 py-4 text-sm font-medium text-slate-950">{{ user.email }}</td>
              <td class="px-6 py-4 text-sm text-slate-600">{{ user.displayName }}</td>
              <td class="px-6 py-4">
                <select
                  :value="user.role"
                  @change="updateUserRole(user.id, $event.target.value)"
                  class="h-10 rounded-2xl border border-slate-200 bg-white px-3 text-sm font-semibold text-slate-700 focus:border-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-600/10"
                  :class="user.role === 'ADMIN' ? 'bg-blue-50 text-blue-700' : 'bg-slate-50 text-slate-700'"
                >
                  <option value="USER">USER</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </td>
              <td class="px-6 py-4 text-sm font-semibold text-slate-950">{{ user.creditBalance }}</td>
              <td class="px-6 py-4">
                <div class="flex flex-wrap gap-2">
                  <button
                    type="button"
                    @click="adjustCredits(user.id, 100, 'Admin bonus')"
                    class="inline-flex h-10 items-center rounded-2xl border border-slate-200 px-3 text-sm font-semibold text-slate-700 transition hover:border-blue-200 hover:bg-blue-50 hover:text-blue-700"
                  >
                    +100
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="!users.length" class="border-t border-slate-200 px-6 py-16 text-center">
        <p class="text-sm font-medium text-slate-500">No users found for the current search.</p>
      </div>
    </div>
  </section>
</template>
