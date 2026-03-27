import { defineStore } from "pinia";

export const useCvStore = defineStore("cv", {
  state: () => ({
    currentCv: null,
    sections: [],
  }),
  actions: {
    setCv(cv) {
      this.currentCv = cv;
    },
    setSections(sections) {
      this.sections = sections || [];
    },
  },
});
