import { defineStore } from "pinia";
import { getCv, updateCv } from "@/api/cv";

export const useCvStore = defineStore("cv", {
  state: () => ({
    currentCv: null,
    sections: [],
    loading: false,
    error: "",
    saving: false,
  }),
  actions: {
    setCv(cv) {
      this.currentCv = cv;
    },
    setSections(sections) {
      this.sections = sections || [];
    },
    async loadCv(cvId) {
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
  },
});
