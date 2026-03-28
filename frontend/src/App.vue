<script setup>
import { computed, ref } from "vue";
import { RouterLink, RouterView } from "vue-router";
import { useI18n } from "vue-i18n";
import { useAuthStore } from "@/stores/auth";

const auth = useAuthStore();
const { locale } = useI18n();
const mobileMenuOpen = ref(false);

const locales = [
  { value: "en", label: "English" },
  { value: "zh-CN", label: "简体中文" },
  { value: "zh-TW", label: "繁體中文" },
];
const isLoggedIn = computed(() => auth.isLoggedIn);

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
  <div class="app-shell min-h-screen bg-gray-50">
    <header class="app-header bg-white border-b border-gray-200 sticky top-0 z-50">
      <div class="max-w-6xl mx-auto px-4 sm:px-6">
        <div class="flex items-center justify-between h-16">
          <RouterLink class="brand text-xl font-bold text-blue-600 hover:text-blue-700" to="/dashboard">
            Project CV
          </RouterLink>

          <nav class="hidden sm:flex items-center gap-6">
            <RouterLink
              v-if="isLoggedIn"
              to="/dashboard"
              class="text-gray-700 hover:text-blue-600 font-medium transition-colors"
            >
              {{ $t("nav.dashboard") }}
            </RouterLink>
            <RouterLink
              to="/login"
              class="text-gray-700 hover:text-blue-600 font-medium transition-colors"
            >
              {{ isLoggedIn ? $t("nav.profile") : $t("nav.login") }}
            </RouterLink>
          </nav>

          <div class="flex items-center gap-3">
            <select
              :value="locale"
              @change="onLocaleChange"
              class="text-sm border border-gray-300 rounded-lg px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option v-for="item in locales" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>

            <!-- Mobile menu button -->
            <button
              v-if="isLoggedIn"
              class="sm:hidden p-2 rounded-lg text-gray-600 hover:bg-gray-100"
              @click="toggleMobileMenu"
              aria-label="Toggle menu"
            >
              <svg v-if="!mobileMenuOpen" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
              </svg>
              <svg v-else class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        <!-- Mobile nav -->
        <nav v-if="isLoggedIn && mobileMenuOpen" class="sm:hidden pb-4 flex gap-4">
          <RouterLink
            to="/dashboard"
            class="text-gray-700 hover:text-blue-600 font-medium transition-colors"
            @click="closeMobileMenu"
          >
            {{ $t("nav.dashboard") }}
          </RouterLink>
          <RouterLink
            to="/profile"
            class="text-gray-700 hover:text-blue-600 font-medium transition-colors"
            @click="closeMobileMenu"
          >
            {{ $t("nav.profile") }}
          </RouterLink>
        </nav>
      </div>
    </header>
    <main class="app-main">
      <RouterView />
    </main>
    <footer class="bg-white border-t border-gray-200 mt-auto">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 py-6 text-center text-sm text-gray-600">
        © 2024 Project CV. All rights reserved.
      </div>
    </footer>
  </div>
</template>

<style scoped>
.app-shell {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}
.app-main {
  flex: 1;
}
</style>
