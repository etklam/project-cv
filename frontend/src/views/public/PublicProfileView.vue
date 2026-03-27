<script setup>
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { getPublicProfile } from "@/api/public";

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
      requestError?.response?.data?.message || requestError?.message || "Failed to load profile";
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <section data-testid="view-public-profile">
    <div v-if="loading" data-testid="public-profile-loading">Loading public profile...</div>
    <div v-else-if="error" data-testid="public-profile-error">{{ error }}</div>
    <div v-else>
      <h1 data-testid="public-profile-name">
        {{ user?.displayName || user?.username || username }}
      </h1>
      <p data-testid="public-profile-username">@{{ user?.username || username }}</p>

      <ul v-if="cvs.length" data-testid="public-cv-list">
        <li v-for="cv in cvs" :key="cv.slug || cv.id">
          <RouterLink
            :to="`/u/${user?.username || username}/${cv.slug}`"
            data-testid="public-cv-link"
          >
            {{ cv.title || cv.slug }}
          </RouterLink>
        </li>
      </ul>
      <p v-else data-testid="public-cv-empty">No public CVs yet.</p>
    </div>
  </section>
</template>
