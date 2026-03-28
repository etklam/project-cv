<script setup>
import { RouterLink, RouterView } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import { useI18n } from "vue-i18n";

const auth = useAuthStore();
const { t } = useI18n();

const adminNavItems = [
  { path: "/admin/dashboard", label: "dashboard", icon: "M3 12l2-2m0 0l7-7 7 7 7M5 10v10a1 1 0 001 1h3a10 10 0 0020 0v-5a1 1 0 00-1-1h-3a10 10 0 00-3.7 3.7M5 21H7a2 2 0 01-2-2V5a2 2 0 012-2h5.5" },
  { path: "/admin/users", label: "users", icon: "M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" },
  { path: "/admin/promocodes", label: "promocodes", icon: "M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" },
  { path: "/admin/templates", label: "templates", icon: "M4 5a1 1 0 011-1h14a1 1 0 011 1v2a1 1 0 01-1 1H5a1 1 0 01-1-1V5zM4 13a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H5a1 1 0 01-1-1v-6zM16 13a1 1 0 011-1h2a1 1 0 011 1v6a1 1 0 01-1 1h-2a1 1 0 01-1-1v-6z" },
  { path: "/admin/credits", label: "credits", icon: "M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" },
];

const logout = () => {
  auth.logout();
};
</script>

<template>
  <div class="min-h-screen bg-gray-100">
    <!-- Header -->
    <header class="bg-white shadow-sm border-b border-gray-200">
      <div class="flex items-center justify-between px-6 py-4">
        <div class="flex items-center gap-4">
          <h1 class="text-xl font-bold text-gray-800">Project CV</h1>
          <span class="px-2 py-1 text-xs font-semibold text-purple-600 bg-purple-100 rounded-full">
            Admin
          </span>
        </div>
        <div class="flex items-center gap-4">
          <span class="text-sm text-gray-600">{{ auth.user?.displayName }}</span>
          <button
            @click="logout"
            class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded-lg transition-colors"
          >
            {{ $t("nav.logout") }}
          </button>
        </div>
      </div>
    </header>

    <div class="flex">
      <!-- Sidebar -->
      <aside class="w-64 bg-white border-r border-gray-200 min-h-screen">
        <nav class="p-4 space-y-1">
          <RouterLink
            v-for="item in adminNavItems"
            :key="item.path"
            :to="item.path"
            class="flex items-center gap-3 px-4 py-3 text-gray-700 hover:bg-gray-100 hover:text-purple-600 rounded-lg transition-colors"
            active-class="bg-purple-50 text-purple-600"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="item.icon" />
            </svg>
            <span class="font-medium">{{ $t(`admin.navigation.${item.label}`) }}</span>
          </RouterLink>
        </nav>

        <div class="p-4 border-t border-gray-200">
          <RouterLink
            to="/dashboard"
            class="flex items-center gap-2 px-4 py-2 text-sm text-gray-500 hover:text-gray-700 transition-colors"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            <span>{{ $t("admin.backToUser") }}</span>
          </RouterLink>
        </div>
      </aside>

      <!-- Main content -->
      <main class="flex-1 p-6">
        <RouterView />
      </main>
    </div>
  </div>
</template>
