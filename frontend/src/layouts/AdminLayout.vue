<script setup>
import { computed, ref } from "vue";
import { RouterLink, RouterView, useRoute } from "vue-router";
import { useI18n } from "vue-i18n";
import { useAuthStore } from "@/stores/auth";

const auth = useAuthStore();
const route = useRoute();
const { t } = useI18n();
const mobileNavOpen = ref(false);

const adminNavItems = [
  {
    path: "/admin/dashboard",
    label: "dashboard",
    title: "admin.dashboard.title",
    icon: "M3 12l2-2m0 0l7-7 7 7m-14 0v8a1 1 0 001 1h3a10 10 0 0020 0v-8m-14 0l7 7 7-7",
  },
  {
    path: "/admin/users",
    label: "users",
    title: "admin.users.title",
    icon: "M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z",
  },
  {
    path: "/admin/promocodes",
    label: "promocodes",
    title: "admin.promocodes.title",
    icon: "M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z",
  },
  {
    path: "/admin/templates",
    label: "templates",
    title: "admin.templates.title",
    icon: "M4 5a1 1 0 011-1h14a1 1 0 011 1v2a1 1 0 01-1 1H5a1 1 0 01-1-1V5zM4 13a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H5a1 1 0 01-1-1v-6zM16 13a1 1 0 011-1h2a1 1 0 011 1v6a1 1 0 01-1 1h-2a1 1 0 01-1-1v-6z",
  },
  {
    path: "/admin/credits",
    label: "credits",
    title: "admin.credits.title",
    icon: "M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z",
  },
];

const currentSection = computed(() => {
  const match = adminNavItems.find((item) => route.path === item.path || route.path.startsWith(`${item.path}/`));
  return match || adminNavItems[0];
});

const currentTitle = computed(() => t(currentSection.value.title));
const currentUser = computed(() => auth.user?.displayName || auth.user?.email || "Admin");

const logout = () => {
  mobileNavOpen.value = false;
  auth.logout();
};

const closeMobileNav = () => {
  mobileNavOpen.value = false;
};
</script>

<template>
  <div class="admin-shell fixed inset-0 z-[60] overflow-hidden bg-[#f8fafc] text-slate-900">
    <div class="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,_rgba(59,130,246,0.12),transparent_24%),radial-gradient(circle_at_top_right,_rgba(245,158,11,0.12),transparent_22%)]" aria-hidden="true"></div>

    <header class="fixed left-0 right-0 top-0 z-[70] h-16 border-b border-slate-200 bg-white/85 backdrop-blur-xl">
      <div class="mx-auto flex h-full max-w-[1720px] items-center gap-4 px-4 sm:px-6 lg:px-8">
        <button
          type="button"
          class="inline-flex h-11 w-11 items-center justify-center rounded-2xl border border-slate-200 bg-white text-slate-700 transition hover:border-blue-200 hover:text-blue-700 lg:hidden"
          @click="mobileNavOpen = !mobileNavOpen"
        >
          <svg v-if="!mobileNavOpen" class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
          <svg v-else class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M6 6l12 12M18 6L6 18" />
          </svg>
        </button>

        <div class="min-w-0">
          <p class="text-[10px] font-bold uppercase tracking-[0.28em] text-blue-600">Admin workspace</p>
          <h1 class="admin-display mt-1 truncate text-xl font-semibold tracking-[-0.04em] text-slate-950 sm:text-2xl">
            {{ currentTitle }}
          </h1>
        </div>

        <div class="ml-auto flex items-center gap-3">
          <div class="hidden rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-600 sm:block">
            {{ currentUser }}
          </div>
          <button
            type="button"
            class="hidden h-11 items-center justify-center rounded-full border border-slate-200 bg-white px-4 text-sm font-semibold text-slate-700 transition hover:border-blue-200 hover:text-blue-700 lg:inline-flex"
            @click="logout"
          >
            {{ $t("nav.logout") }}
          </button>
        </div>
      </div>

      <div v-if="mobileNavOpen" class="border-t border-slate-200 bg-white/95 px-4 py-3 backdrop-blur-xl lg:hidden">
        <div class="mx-auto flex max-w-[1720px] flex-wrap gap-2">
          <RouterLink
            v-for="item in adminNavItems"
            :key="item.path"
            :to="item.path"
            active-class="border-blue-600 bg-blue-600 text-white"
            class="inline-flex h-10 items-center rounded-full border border-slate-200 bg-white px-4 text-sm font-semibold text-slate-600 transition hover:border-blue-200 hover:text-blue-700"
            @click="closeMobileNav"
          >
            {{ $t(`admin.navigation.${item.label}`) }}
          </RouterLink>
        </div>
      </div>
    </header>

    <aside class="fixed left-0 top-16 hidden h-[calc(100vh-4rem)] w-80 flex-col border-r border-slate-200 bg-white/90 backdrop-blur-xl lg:flex">
      <div class="border-b border-slate-200 px-6 py-6">
        <RouterLink to="/admin/dashboard" class="flex items-center gap-4">
          <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-blue-700 text-sm font-extrabold tracking-[0.2em] text-white shadow-sm">
            CV
          </div>
          <div class="min-w-0">
            <p class="text-[10px] font-bold uppercase tracking-[0.28em] text-blue-600">Editorial Architect</p>
            <p class="mt-1 truncate text-lg font-semibold tracking-[-0.04em] text-slate-950">Project CV</p>
          </div>
        </RouterLink>
      </div>

      <nav class="flex-1 px-4 py-6">
        <p class="px-3 text-[10px] font-bold uppercase tracking-[0.28em] text-slate-400">Workspace</p>
        <div class="mt-4 space-y-1">
          <RouterLink
            v-for="item in adminNavItems"
            :key="item.path"
            :to="item.path"
            active-class="border-blue-700 bg-blue-700 text-white shadow-sm"
            class="group flex items-center gap-3 rounded-2xl border border-transparent px-3 py-3 text-sm font-semibold text-slate-600 transition duration-150 hover:border-slate-200 hover:bg-slate-50 hover:text-slate-950"
          >
            <svg class="h-5 w-5 shrink-0 text-current" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.75" :d="item.icon" />
            </svg>
            <span>{{ $t(`admin.navigation.${item.label}`) }}</span>
          </RouterLink>
        </div>
      </nav>

      <div class="border-t border-slate-200 p-4">
        <RouterLink
          to="/dashboard"
          class="flex items-center gap-3 rounded-2xl border border-slate-200 px-3 py-3 text-sm font-semibold text-slate-600 transition hover:border-blue-200 hover:bg-blue-50 hover:text-blue-700"
        >
          <svg class="h-4 w-4 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.75" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          <span>{{ $t("admin.backToUser") }}</span>
        </RouterLink>
        <button
          type="button"
          class="mt-3 flex w-full items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-slate-50 px-3 py-3 text-sm font-semibold text-slate-700 transition hover:border-blue-200 hover:bg-blue-50 hover:text-blue-700"
          @click="logout"
        >
          {{ $t("nav.logout") }}
        </button>
      </div>
    </aside>

    <main class="relative h-full overflow-y-auto pt-16 lg:pl-80">
      <div class="px-4 py-6 sm:px-6 lg:px-8 lg:py-8">
        <div class="mx-auto max-w-7xl">
          <RouterView />
        </div>
      </div>
    </main>
  </div>
</template>

<style>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=Manrope:wght@600;700;800;900&display=swap");

.admin-shell {
  font-family: "Inter", sans-serif;
}

.admin-shell h1,
.admin-shell h2,
.admin-shell h3,
.admin-shell .admin-display {
  font-family: "Manrope", sans-serif;
}
</style>
