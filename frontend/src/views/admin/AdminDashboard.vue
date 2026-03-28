<script setup>
import { computed, onMounted, ref } from "vue";
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

const statCards = computed(() => [
  {
    label: t("admin.dashboard.totalUsers"),
    value: stats.value.totalUsers,
    tone: "bg-slate-950 text-white",
    ring: "ring-slate-950/10",
    icon: "M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z",
  },
  {
    label: t("admin.dashboard.totalPromoCodes"),
    value: stats.value.totalPromoCodes,
    tone: "bg-blue-600 text-white",
    ring: "ring-blue-600/10",
    icon: "M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z",
  },
  {
    label: t("admin.dashboard.activeTemplates"),
    value: stats.value.activeTemplates,
    tone: "bg-slate-100 text-slate-950",
    ring: "ring-slate-200",
    icon: "M4 5a1 1 0 011-1h14a1 1 0 011 1v2a1 1 0 01-1 1H5a1 1 0 01-1-1V5zM4 13a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H5a1 1 0 01-1-1v-6zM16 13a1 1 0 011-1h2a1 1 0 011 1v6a1 1 0 01-1 1h-2a1 1 0 01-1-1v-6z",
  },
  {
    label: t("admin.dashboard.totalTransactions"),
    value: stats.value.totalTransactions,
    tone: "bg-amber-400 text-slate-950",
    ring: "ring-amber-400/20",
    icon: "M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z",
  },
]);

onMounted(async () => {
  try {
    const usersData = await adminApi.listUsers({ page: 1, size: 1 });
    stats.value.totalUsers = usersData.total;

    const promosData = await adminApi.listPromoCodes({ page: 1, size: 1 });
    stats.value.totalPromoCodes = promosData.total;

    const templatesData = await adminApi.listAllTemplates();
    stats.value.activeTemplates = templatesData.filter((template) => template.isActive).length;

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
  <section class="space-y-6">
    <header class="grid gap-5 rounded-[32px] border border-slate-200 bg-white px-6 py-6 shadow-sm sm:px-8 sm:py-8 lg:grid-cols-[1.15fr_0.85fr]">
      <div class="space-y-4">
        <span class="inline-flex w-fit rounded-full bg-blue-50 px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.24em] text-blue-700">
          Admin workspace
        </span>
        <div class="space-y-3">
          <h2 class="admin-display text-4xl font-semibold tracking-[-0.05em] text-slate-950 sm:text-5xl">
            {{ $t("admin.dashboard.title") }}
          </h2>
          <p class="max-w-2xl text-sm leading-7 text-slate-600 sm:text-base">
            Monitor the platform at a glance and keep users, promo codes, templates, and credits in sync.
          </p>
        </div>
        <div class="flex flex-wrap gap-3">
          <div class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
            <p class="text-[11px] font-semibold uppercase tracking-[0.24em] text-slate-500">Mode</p>
            <p class="mt-1 text-sm font-semibold text-slate-950">Control center</p>
          </div>
          <div class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
            <p class="text-[11px] font-semibold uppercase tracking-[0.24em] text-slate-500">Scope</p>
            <p class="mt-1 text-sm font-semibold text-slate-950">All services</p>
          </div>
          <div class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
            <p class="text-[11px] font-semibold uppercase tracking-[0.24em] text-slate-500">Status</p>
            <p class="mt-1 text-sm font-semibold text-blue-700">Live</p>
          </div>
        </div>
      </div>

      <aside class="rounded-[28px] bg-slate-950 p-5 text-white">
        <p class="text-[11px] font-semibold uppercase tracking-[0.24em] text-slate-400">Workspace snapshot</p>
        <ul class="mt-4 space-y-3">
          <li class="rounded-2xl border border-white/10 bg-white/5 px-4 py-3">
            <span class="block text-xs text-slate-400">Users</span>
            <strong class="mt-1 block text-2xl font-bold tracking-[-0.04em] text-white">{{ stats.totalUsers }}</strong>
          </li>
          <li class="rounded-2xl border border-white/10 bg-white/5 px-4 py-3">
            <span class="block text-xs text-slate-400">Templates active</span>
            <strong class="mt-1 block text-2xl font-bold tracking-[-0.04em] text-white">{{ stats.activeTemplates }}</strong>
          </li>
          <li class="rounded-2xl border border-white/10 bg-white/5 px-4 py-3">
            <span class="block text-xs text-slate-400">Transactions</span>
            <strong class="mt-1 block text-2xl font-bold tracking-[-0.04em] text-white">{{ stats.totalTransactions }}</strong>
          </li>
        </ul>
      </aside>
    </header>

    <div v-if="loading" class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4" aria-busy="true">
      <div v-for="index in 4" :key="index" class="rounded-[28px] border border-slate-200 bg-white p-6">
        <div class="h-3 w-20 animate-pulse rounded-full bg-slate-200"></div>
        <div class="mt-5 h-8 w-24 animate-pulse rounded-full bg-slate-200"></div>
        <div class="mt-6 h-10 w-10 animate-pulse rounded-2xl bg-slate-200"></div>
      </div>
    </div>

    <div v-else class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <article
        v-for="card in statCards"
        :key="card.label"
        class="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm"
      >
        <div class="flex items-start justify-between gap-4">
          <div>
            <p class="text-sm font-medium text-slate-500">{{ card.label }}</p>
            <p class="mt-3 text-4xl font-semibold tracking-[-0.05em] text-slate-950">{{ card.value }}</p>
          </div>
          <div :class="['flex h-11 w-11 items-center justify-center rounded-2xl ring-1', card.tone, card.ring]">
            <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.75" :d="card.icon" />
            </svg>
          </div>
        </div>
      </article>
    </div>

    <div class="grid gap-6 lg:grid-cols-[1.3fr_0.7fr]">
      <article class="rounded-[28px] border border-slate-200 bg-white p-6 sm:p-8">
        <div class="flex items-center justify-between gap-3">
          <div>
            <p class="text-[11px] font-semibold uppercase tracking-[0.24em] text-blue-700">Overview</p>
            <h3 class="admin-display mt-2 text-2xl font-semibold tracking-[-0.04em] text-slate-950">
              Live platform snapshot
            </h3>
          </div>
          <span class="rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-xs font-semibold text-slate-500">
            Updated on demand
          </span>
        </div>

        <div class="mt-6 grid gap-3 sm:grid-cols-2">
          <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4">
            <p class="text-sm font-medium text-slate-500">Users</p>
            <p class="mt-2 text-2xl font-semibold text-slate-950">{{ stats.totalUsers }}</p>
          </div>
          <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4">
            <p class="text-sm font-medium text-slate-500">Promo codes</p>
            <p class="mt-2 text-2xl font-semibold text-slate-950">{{ stats.totalPromoCodes }}</p>
          </div>
          <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4">
            <p class="text-sm font-medium text-slate-500">Templates active</p>
            <p class="mt-2 text-2xl font-semibold text-slate-950">{{ stats.activeTemplates }}</p>
          </div>
          <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4">
            <p class="text-sm font-medium text-slate-500">Transactions</p>
            <p class="mt-2 text-2xl font-semibold text-slate-950">{{ stats.totalTransactions }}</p>
          </div>
        </div>
      </article>

      <article class="rounded-[28px] border border-slate-200 bg-[#1e40af] p-6 text-white sm:p-8">
        <p class="text-[11px] font-semibold uppercase tracking-[0.24em] text-blue-100">Control notes</p>
        <h3 class="admin-display mt-3 text-2xl font-semibold tracking-[-0.04em]">Keep the system flat, legible, and fast.</h3>
        <ul class="mt-6 space-y-3 text-sm leading-6 text-blue-50">
          <li class="rounded-2xl border border-white/10 bg-white/10 px-4 py-3">Use borders and spacing first, not heavy shadows.</li>
          <li class="rounded-2xl border border-white/10 bg-white/10 px-4 py-3">Keep table actions visible and easy to scan.</li>
          <li class="rounded-2xl border border-white/10 bg-white/10 px-4 py-3">Prefer strong contrast for states and toggles.</li>
        </ul>
      </article>
    </div>
  </section>
</template>
