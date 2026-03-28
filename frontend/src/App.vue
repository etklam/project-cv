<script setup>
import { computed, ref } from "vue";
import { RouterLink, RouterView, useRoute } from "vue-router";
import { useI18n } from "vue-i18n";
import { useAuthStore } from "@/stores/auth";

const auth = useAuthStore();
const route = useRoute();
const { locale } = useI18n();
const mobileMenuOpen = ref(false);

const locales = [
  { value: "en", label: "English" },
  { value: "zh-CN", label: "简体中文" },
  { value: "zh-TW", label: "繁體中文" },
];

const isLoggedIn = computed(() => auth.isLoggedIn);
const userLabel = computed(() => auth.user?.displayName || auth.user?.email || "Workspace");
const isAdminRoute = computed(() => route.path.startsWith("/admin"));
const isAuthRoute = computed(() => ["/login", "/register"].includes(route.path));
const isOnboardingRoute = computed(() => route.path.startsWith("/onboarding"));
const isPublicRoute = computed(() => route.path.startsWith("/u/"));

const showWorkspaceSidebar = computed(
  () => isLoggedIn.value && !isAdminRoute.value && !isAuthRoute.value && !isOnboardingRoute.value && !isPublicRoute.value,
);

const workspaceNavItems = computed(() => {
  const items = [{ to: "/dashboard", label: "My Resumes", current: route.path === "/dashboard" }];

  if (route.path.startsWith("/editor/")) {
    items.push({ to: route.path, label: "Editor", current: true });
  }

  if (route.path.startsWith("/print/")) {
    items.push({ to: route.path, label: "Preview", current: true });
  }

  return items;
});

function toggleMobileMenu() {
  mobileMenuOpen.value = !mobileMenuOpen.value;
}

function closeMobileMenu() {
  mobileMenuOpen.value = false;
}

function onLocaleChange(event) {
  const next = event.target.value;
  locale.value = next;
  auth.setLocale(next);
}
</script>

<template>
  <div class="app-shell">
    <div class="app-shell__backdrop" aria-hidden="true">
      <span class="app-shell__orb app-shell__orb--rose"></span>
      <span class="app-shell__orb app-shell__orb--amber"></span>
      <span class="app-shell__grid"></span>
    </div>

    <header class="fixed inset-x-0 top-0 z-40 h-16 border-b border-slate-200/80 bg-[#fbf8ff]/80 backdrop-blur-xl">
      <div class="mx-auto flex h-full w-full max-w-[1720px] items-center gap-4 px-4 sm:px-6 lg:px-8">
        <div class="flex items-center gap-8">
          <RouterLink class="flex items-center gap-3" to="/dashboard" @click="closeMobileMenu">
            <span class="font-display text-2xl font-black text-[#003aa0]">Editorial Architect</span>
          </RouterLink>

          <nav class="hidden gap-6 md:flex">
            <RouterLink
              v-if="isLoggedIn"
              to="/dashboard"
              class="font-display border-b-2 pb-1 text-lg font-bold transition-all"
              :class="route.path === '/dashboard' ? 'border-[#003aa0] text-[#003aa0]' : 'border-transparent text-slate-600 hover:text-[#003aa0]'"
              @click="closeMobileMenu"
            >
              Editor
            </RouterLink>
            <RouterLink
              :to="isLoggedIn ? '/dashboard' : '/login'"
              class="font-display border-b-2 border-transparent pb-1 text-lg font-bold text-slate-600 transition-all hover:text-[#003aa0]"
              @click="closeMobileMenu"
            >
              {{ isLoggedIn ? $t("nav.profile") : $t("auth.login") }}
            </RouterLink>
          </nav>
        </div>

        <div class="ml-auto hidden items-center gap-4 md:flex">
          <button
            v-if="isLoggedIn && !isAdminRoute"
            type="button"
            class="rounded-xl bg-gradient-to-br from-[#003aa0] to-[#004fd2] px-6 py-2 text-sm font-semibold text-white transition-transform active:scale-95"
          >
            Publish Resume
          </button>

          <label class="relative">
            <span class="sr-only">Language</span>
            <select
              :value="locale"
              class="min-h-10 rounded-xl border border-slate-200 bg-white px-4 pr-10 text-sm font-medium text-slate-900 outline-none transition focus:border-blue-400 focus:ring-2 focus:ring-blue-200"
              @change="onLocaleChange"
            >
              <option v-for="item in locales" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>
          </label>

          <div
            v-if="isLoggedIn"
            class="rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-600"
          >
            {{ userLabel }}
          </div>
        </div>

        <div class="ml-auto flex items-center gap-3 md:hidden">
          <label class="relative">
            <span class="sr-only">Language</span>
            <select
              :value="locale"
              class="min-h-10 rounded-xl border border-slate-200 bg-white px-4 pr-9 text-sm font-medium text-slate-900 outline-none transition focus:border-blue-400 focus:ring-2 focus:ring-blue-200"
              @change="onLocaleChange"
            >
              <option v-for="item in locales" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>
          </label>

          <button
            type="button"
            class="inline-flex h-10 w-10 items-center justify-center rounded-xl border border-slate-200 bg-white text-slate-900"
            :aria-expanded="mobileMenuOpen ? 'true' : 'false'"
            aria-label="Toggle menu"
            @click="toggleMobileMenu"
          >
            <svg viewBox="0 0 24 24" class="h-5 w-5" aria-hidden="true">
              <path
                v-if="!mobileMenuOpen"
                d="M4 7h16M4 12h16M4 17h16"
                fill="none"
                stroke="currentColor"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="1.8"
              />
              <path
                v-else
                d="M6 6l12 12M18 6L6 18"
                fill="none"
                stroke="currentColor"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="1.8"
              />
            </svg>
          </button>
        </div>
      </div>
    </header>

    <div class="relative z-10 flex flex-1 pt-16">
      <aside
        v-if="showWorkspaceSidebar"
        class="hidden h-[calc(100vh-4rem)] w-64 shrink-0 border-r border-slate-200 bg-[#f4f2fc] lg:fixed lg:left-0 lg:top-16 lg:flex lg:flex-col lg:p-6"
      >
        <div class="mb-8 px-2">
          <h2 class="font-display text-xl font-extrabold tracking-tight text-[#003aa0]">Architect Workspace</h2>
          <p class="mt-1 text-xs font-semibold uppercase tracking-[0.22em] text-slate-500">Pro Member</p>
        </div>

        <nav class="flex flex-col gap-2">
          <RouterLink
            v-for="item in workspaceNavItems"
            :key="item.to"
            :to="item.to"
            class="rounded-xl p-3 text-sm font-semibold tracking-tight transition-all duration-200"
            :class="item.current ? 'bg-white text-[#003aa0] shadow-soft' : 'text-slate-600 hover:bg-[#e9e7f0] hover:text-[#003aa0]'"
          >
            {{ item.label }}
          </RouterLink>
        </nav>
      </aside>

      <main class="min-w-0 flex-1 px-4 pb-10 pt-6 sm:px-6 lg:px-8" :class="showWorkspaceSidebar ? 'lg:ml-64' : ''">
        <div
          v-if="mobileMenuOpen"
          class="mb-4 grid gap-2 rounded-3xl border border-slate-200 bg-white p-2 md:hidden"
          aria-label="Mobile navigation"
        >
          <div
            v-if="isLoggedIn"
            class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3"
          >
            <span class="block text-[10px] font-bold uppercase tracking-[0.22em] text-slate-400">Workspace</span>
            <strong class="mt-1 block truncate text-sm font-semibold text-slate-900">{{ userLabel }}</strong>
          </div>
          <RouterLink
            v-if="isLoggedIn"
            to="/dashboard"
            class="rounded-2xl px-4 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-100"
            @click="closeMobileMenu"
          >
            {{ $t("nav.dashboard") }}
          </RouterLink>
          <RouterLink
            :to="isLoggedIn ? '/dashboard' : '/login'"
            class="rounded-2xl px-4 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-100"
            @click="closeMobileMenu"
          >
            {{ isLoggedIn ? $t("nav.profile") : $t("auth.login") }}
          </RouterLink>
        </div>

        <RouterView />
      </main>
    </div>

    <footer class="relative z-10 px-4 pb-5 sm:px-5">
      <div class="mx-auto flex w-full max-w-[1720px] flex-col gap-2 border-t border-slate-200 px-1 pt-5 text-sm text-slate-500 sm:flex-row sm:items-center sm:justify-between">
        <p>Structured resumes, cleaner workflows, faster publishing.</p>
        <small>© 2026 Project CV</small>
      </div>
    </footer>
  </div>
</template>
