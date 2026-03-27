<script setup>
import { computed } from "vue";
import { RouterLink, RouterView } from "vue-router";
import { useI18n } from "vue-i18n";
import { useAuthStore } from "@/stores/auth";

const auth = useAuthStore();
const { locale } = useI18n();

const locales = ["en", "zh-CN", "zh-TW"];
const isLoggedIn = computed(() => auth.isLoggedIn);

function onLocaleChange(event) {
  const next = event.target.value;
  locale.value = next;
  auth.setLocale(next);
}
</script>

<template>
  <div class="app-shell">
    <header class="app-header">
      <RouterLink class="brand" to="/dashboard">Project CV</RouterLink>
      <nav class="nav">
        <RouterLink to="/dashboard">Dashboard</RouterLink>
        <RouterLink to="/onboarding/step1">Onboarding</RouterLink>
        <RouterLink to="/login">{{ isLoggedIn ? "Account" : "Login" }}</RouterLink>
      </nav>
      <select :value="locale" @change="onLocaleChange">
        <option v-for="item in locales" :key="item" :value="item">{{ item }}</option>
      </select>
    </header>
    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>
