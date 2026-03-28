<script setup>
import { ref, onMounted, computed } from "vue";
import { adminApi } from "@/api/admin";

const promoCodes = ref([]);
const loading = ref(false);
const showCreateModal = ref(false);
const newCode = ref({ code: "", rewardValue: 50, maxRedemptions: null });

const summary = computed(() => ({
  total: promoCodes.value.length,
  active: promoCodes.value.filter((code) => code.isActive).length,
}));

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
  <section class="space-y-6">
    <header class="grid gap-5 rounded-[32px] border border-slate-200 bg-white px-6 py-6 shadow-sm sm:px-8 sm:py-8 lg:grid-cols-[1.2fr_0.8fr]">
      <div class="space-y-4">
        <span class="inline-flex w-fit rounded-full bg-blue-50 px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.24em] text-blue-700">
          Promo codes
        </span>
        <div class="space-y-3">
          <h2 class="admin-display text-4xl font-semibold tracking-[-0.05em] text-slate-950">
            {{ $t("admin.promocodes.title") }}
          </h2>
          <p class="max-w-2xl text-sm leading-7 text-slate-600">
            Manage rewards, control redemptions, and keep promo inventory tidy.
          </p>
        </div>
      </div>

      <div class="flex flex-wrap gap-3 lg:justify-end">
        <div class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
          <p class="text-[11px] font-semibold uppercase tracking-[0.24em] text-slate-500">Active</p>
          <p class="mt-1 text-sm font-semibold text-slate-950">{{ summary.active }}</p>
        </div>
        <div class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
          <p class="text-[11px] font-semibold uppercase tracking-[0.24em] text-slate-500">Total</p>
          <p class="mt-1 text-sm font-semibold text-slate-950">{{ summary.total }}</p>
        </div>
        <button
          type="button"
          @click="showCreateModal = true"
          class="inline-flex h-11 items-center rounded-2xl border border-blue-700 bg-blue-700 px-4 text-sm font-semibold text-white transition hover:bg-blue-800"
        >
          {{ $t("admin.promocodes.create") }}
        </button>
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
              <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">Code</th>
              <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                {{ $t("admin.promocodes.reward") }}
              </th>
              <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                {{ $t("admin.promocodes.maxRedemptions") }}
              </th>
              <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                {{ $t("admin.promocodes.redemptions") }}
              </th>
              <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                {{ $t("admin.promocodes.isActive") }}
              </th>
              <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                {{ $t("admin.users.actions") }}
              </th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-200 bg-white">
            <tr v-for="code in promoCodes" :key="code.id" class="hover:bg-slate-50/80">
              <td class="px-6 py-4 text-sm font-mono font-semibold text-slate-950">{{ code.code }}</td>
              <td class="px-6 py-4 text-sm text-slate-600">{{ code.rewardValue }}</td>
              <td class="px-6 py-4 text-sm text-slate-600">{{ code.maxRedemptions || "∞" }}</td>
              <td class="px-6 py-4 text-sm text-slate-600">{{ code.currentRedemptions }}</td>
              <td class="px-6 py-4">
                <span
                  :class="code.isActive ? 'border-blue-200 bg-blue-50 text-blue-700' : 'border-slate-200 bg-slate-50 text-slate-500'"
                  class="inline-flex rounded-full border px-3 py-1 text-xs font-semibold uppercase tracking-[0.2em]"
                >
                  {{ code.isActive ? $t("common.active") : $t("common.inactive") }}
                </span>
              </td>
              <td class="px-6 py-4">
                <button
                  type="button"
                  @click="deletePromoCode(code.id)"
                  class="inline-flex h-10 items-center rounded-2xl border border-slate-200 px-3 text-sm font-semibold text-slate-700 transition hover:border-rose-200 hover:bg-rose-50 hover:text-rose-700"
                >
                  {{ $t("common.delete") }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="!promoCodes.length" class="border-t border-slate-200 px-6 py-16 text-center">
        <p class="text-sm font-medium text-slate-500">No promo codes available yet.</p>
      </div>
    </div>

    <div
      v-if="showCreateModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/40 px-4 py-6 backdrop-blur-sm"
      @click.self="showCreateModal = false"
    >
      <form
        class="w-full max-w-lg rounded-[32px] border border-slate-200 bg-white p-6 shadow-xl sm:p-8"
        @submit.prevent="createPromoCode"
      >
        <div class="flex items-start justify-between gap-4">
          <div>
            <p class="text-[11px] font-semibold uppercase tracking-[0.28em] text-blue-700">Create promo code</p>
            <h3 class="admin-display mt-3 text-2xl font-semibold tracking-[-0.04em] text-slate-950">
              {{ $t("admin.promocodes.create") }}
            </h3>
          </div>
          <button
            type="button"
            class="inline-flex h-10 items-center rounded-full border border-slate-200 bg-white px-3 text-sm font-semibold text-slate-600 transition hover:border-blue-200 hover:text-blue-700"
            @click="showCreateModal = false"
          >
            {{ $t("common.close") }}
          </button>
        </div>

        <div class="mt-6 space-y-4">
          <div>
            <label class="text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">Code</label>
            <input
              v-model="newCode.code"
              type="text"
              class="mt-2 h-11 w-full rounded-2xl border border-slate-200 px-4 text-sm text-slate-950 placeholder:text-slate-400 focus:border-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-600/10"
            />
          </div>
          <div>
            <label class="text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
              {{ $t("admin.promocodes.reward") }}
            </label>
            <input
              v-model.number="newCode.rewardValue"
              type="number"
              class="mt-2 h-11 w-full rounded-2xl border border-slate-200 px-4 text-sm text-slate-950 placeholder:text-slate-400 focus:border-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-600/10"
            />
          </div>
          <div>
            <label class="text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
              {{ $t("admin.promocodes.maxRedemptions") }}
            </label>
            <input
              v-model.number="newCode.maxRedemptions"
              type="number"
              placeholder="Unlimited"
              class="mt-2 h-11 w-full rounded-2xl border border-slate-200 px-4 text-sm text-slate-950 placeholder:text-slate-400 focus:border-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-600/10"
            />
          </div>
        </div>

        <div class="mt-6 flex justify-end gap-3">
          <button
            type="button"
            class="inline-flex h-11 items-center rounded-2xl border border-slate-200 bg-white px-4 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
            @click="showCreateModal = false"
          >
            {{ $t("common.cancel") }}
          </button>
          <button
            type="submit"
            class="inline-flex h-11 items-center rounded-2xl border border-blue-700 bg-blue-700 px-4 text-sm font-semibold text-white transition hover:bg-blue-800"
          >
            {{ $t("common.create") }}
          </button>
        </div>
      </form>
    </div>
  </section>
</template>
