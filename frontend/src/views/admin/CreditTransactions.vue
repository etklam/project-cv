<script setup>
import { computed, onMounted, ref } from "vue";
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

const summary = computed(() => ({
  total: pagination.value.total,
  visible: transactions.value.length,
}));

onMounted(loadTransactions);
</script>

<template>
  <section class="space-y-6">
    <header class="grid gap-5 rounded-[32px] border border-slate-200 bg-white px-6 py-6 shadow-sm sm:px-8 sm:py-8 lg:grid-cols-[1.2fr_0.8fr]">
      <div class="space-y-4">
        <span class="inline-flex w-fit rounded-full bg-blue-50 px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.24em] text-blue-700">
          Credits
        </span>
        <div class="space-y-3">
          <h2 class="admin-display text-4xl font-semibold tracking-[-0.05em] text-slate-950">
            {{ $t("admin.credits.title") }}
          </h2>
          <p class="max-w-2xl text-sm leading-7 text-slate-600">
            Review balance movements and scan credit history without leaving the admin workspace.
          </p>
        </div>
      </div>

      <div class="grid gap-3 sm:grid-cols-2 xl:grid-cols-1">
        <div class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
          <p class="text-[11px] font-semibold uppercase tracking-[0.24em] text-slate-500">Total</p>
          <p class="mt-1 text-sm font-semibold text-slate-950">{{ summary.total }}</p>
        </div>
        <div class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
          <p class="text-[11px] font-semibold uppercase tracking-[0.24em] text-slate-500">Visible</p>
          <p class="mt-1 text-sm font-semibold text-slate-950">{{ summary.visible }}</p>
        </div>
      </div>
    </header>

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
                {{ $t("admin.credits.transactionId") }}
              </th>
              <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                {{ $t("admin.credits.user") }}
              </th>
              <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                {{ $t("admin.credits.amount") }}
              </th>
              <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                {{ $t("admin.credits.balanceAfter") }}
              </th>
              <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                {{ $t("admin.credits.type") }}
              </th>
              <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                {{ $t("admin.credits.createdAt") }}
              </th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-200 bg-white">
            <tr v-for="tx in transactions" :key="tx.id" class="hover:bg-slate-50/80">
              <td class="px-6 py-4 text-sm font-semibold text-slate-950">#{{ tx.id }}</td>
              <td class="px-6 py-4 text-sm text-slate-600">
                <div class="font-medium text-slate-950">{{ tx.userName || tx.userEmail }}</div>
              </td>
              <td class="px-6 py-4">
                <span
                  :class="tx.amount > 0 ? 'border-blue-200 bg-blue-50 text-blue-700' : 'border-rose-200 bg-rose-50 text-rose-700'"
                  class="inline-flex rounded-full border px-3 py-1 text-xs font-semibold uppercase tracking-[0.2em]"
                >
                  {{ tx.amount > 0 ? '+' : '' }}{{ tx.amount }}
                </span>
              </td>
              <td class="px-6 py-4 text-sm font-semibold text-slate-950">{{ tx.balanceAfter }}</td>
              <td class="px-6 py-4 text-sm text-slate-600">{{ tx.type }}</td>
              <td class="px-6 py-4 text-sm text-slate-500">{{ tx.createdAt }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="!transactions.length" class="border-t border-slate-200 px-6 py-16 text-center">
        <p class="text-sm font-medium text-slate-500">No credit transactions found.</p>
      </div>
    </div>
  </section>
</template>
