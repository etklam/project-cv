import { defineStore } from "pinia";
import { changeLocale, login, logout, me, register } from "@/api/auth";

function safeGetLocale() {
  if (typeof globalThis.localStorage?.getItem === "function") {
    return globalThis.localStorage.getItem("locale");
  }

  return null;
}

function safeSetLocale(locale) {
  if (typeof globalThis.localStorage?.setItem === "function") {
    globalThis.localStorage.setItem("locale", locale);
  }
}

export const useAuthStore = defineStore("auth", {
  state: () => ({
    user: null,
    isLoggedIn: false,
    initialized: false,
    locale: safeGetLocale() || "zh-TW",
  }),
  actions: {
    async bootstrap() {
      try {
        const data = await me();
        this.user = data.user;
        this.isLoggedIn = true;
        this.locale = data.user?.locale || this.locale;
      } catch (_error) {
        this.user = null;
        this.isLoggedIn = false;
      } finally {
        this.initialized = true;
      }
    },
    async setLocale(locale) {
      this.locale = locale;
      safeSetLocale(locale);

      if (this.isLoggedIn) {
        const data = await changeLocale({ locale });
        this.user = {
          ...this.user,
          ...data.user,
        };
      }
    },
    async login(payload) {
      const data = await login(payload);
      this.user = data.user;
      this.isLoggedIn = true;
      return data;
    },
    async register(payload) {
      const data = await register(payload);
      this.user = data.user;
      this.isLoggedIn = true;
      return data;
    },
    async logout() {
      await logout();
      this.user = null;
      this.isLoggedIn = false;
    },
  },
});
