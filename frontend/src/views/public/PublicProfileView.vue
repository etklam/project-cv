<script setup>
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { getPublicProfile } from "@/api/public";

const { t } = useI18n();
const route = useRoute();
const username = route?.params?.username || "";

const loading = ref(true);
const error = ref("");
const user = ref(null);
const cvs = ref([]);

onMounted(async () => {
  try {
    const result = await getPublicProfile(username);
    user.value = result.user || null;
    cvs.value = result.cvs || [];
  } catch (requestError) {
    error.value =
      requestError?.response?.data?.message || requestError?.message || t("errors.general");
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <section data-testid="view-public-profile" class="min-h-screen bg-gray-50">
    <div v-if="loading" class="flex items-center justify-center min-h-screen">
      <div class="text-center">
        <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        <p class="mt-4 text-gray-600">{{ t("common.loading") }}</p>
      </div>
    </div>
    <div v-else-if="error" class="flex items-center justify-center min-h-screen">
      <div class="bg-red-50 text-red-800 px-6 py-4 rounded-lg max-w-md" data-testid="public-profile-error">
        {{ error }}
      </div>
    </div>
    <div v-else class="max-w-4xl mx-auto px-4 sm:px-6 py-8">
      <div class="bg-white rounded-xl shadow-lg p-6 sm:p-8 mb-8">
        <h1 class="text-2xl sm:text-3xl font-bold text-gray-900" data-testid="public-profile-name">
          {{ user?.displayName || user?.username || username }}
        </h1>
        <p class="text-gray-600 mt-1" data-testid="public-profile-username">@{{ user?.username || username }}</p>
      </div>

      <div class="bg-white rounded-xl shadow-lg p-6 sm:p-8">
        <h2 class="text-xl font-semibold text-gray-900 mb-4">{{ t("dashboard.myCvs") }}</h2>
        <ul v-if="cvs.length" class="space-y-3" data-testid="public-cv-list">
          <li v-for="cv in cvs" :key="cv.slug || cv.id" class="border border-gray-200 rounded-lg p-4 hover:border-blue-300 transition-colors">
            <RouterLink
              :to="`/u/${user?.username || username}/${cv.slug}`"
              class="flex items-center justify-between"
              data-testid="public-cv-link"
            >
              <div>
                <p class="font-semibold text-gray-900">{{ cv.title || cv.slug }}</p>
                <p class="text-sm text-gray-500 mt-1">{{ t("templates." + cv.templateKey + ".name", cv.templateKey) }}</p>
              </div>
              <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </RouterLink>
          </li>
        </ul>
        <p v-else class="text-gray-500 text-center py-8" data-testid="public-cv-empty">
          {{ t("public.noCvs") }}
        </p>
      </div>
    </div>
  </section>
</template>
