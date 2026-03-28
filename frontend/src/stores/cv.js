import { defineStore } from "pinia";
import { getCv, updateCv, updateSections } from "@/api/cv";

export const useCvStore = defineStore("cv", {
  state: () => ({
    currentCv: null,
    sections: [],
    loading: false,
    error: "",
    saving: false,
  }),
  getters: {
    isLoaded: (state) => state.currentCv !== null,
  },
  actions: {
    reset() {
      this.currentCv = null;
      this.sections = [];
      this.loading = false;
      this.error = "";
      this.saving = false;
    },
    setCv(cv) {
      this.currentCv = cv;
    },
    setSections(sections) {
      this.sections = sections || [];
    },
    async loadCv(cvId) {
      this.reset(); // Clear previous state before loading new CV
      this.loading = true;
      this.error = "";
      try {
        const payload = await getCv(cvId);
        this.currentCv = payload?.cv || null;
        this.sections = payload?.sections || [];
      } catch (requestError) {
        this.error =
          requestError?.response?.data?.message || requestError?.message || "Failed to load CV";
        throw requestError;
      } finally {
        this.loading = false;
      }
    },
    async updateMetadata(cvId, metadata) {
      this.saving = true;
      this.error = "";
      try {
        const payload = await updateCv(cvId, metadata);
        this.currentCv = payload?.cv || null;
        this.sections = payload?.sections || [];
        return payload;
      } catch (requestError) {
        this.error =
          requestError?.response?.data?.message || requestError?.message || "Failed to update CV";
        throw requestError;
      } finally {
        this.saving = false;
      }
    },
    async updateSections(cvId, sectionsPayload) {
      this.saving = true;
      this.error = "";
      try {
        const payload = await updateSections(cvId, sectionsPayload);
        this.currentCv = payload?.cv || this.currentCv;
        this.sections = payload?.sections || [];
        return payload;
      } catch (requestError) {
        this.error =
          requestError?.response?.data?.message || requestError?.message || "Failed to update sections";
        throw requestError;
      } finally {
        this.saving = false;
      }
    },
  },
});
