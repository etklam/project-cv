import { createI18n } from "vue-i18n";
import en from "./locales/en.json";
import zhCN from "./locales/zh-CN.json";
import zhTW from "./locales/zh-TW.json";

function resolveInitialLocale() {
  if (typeof globalThis.localStorage?.getItem === "function") {
    return globalThis.localStorage.getItem("locale") || "zh-TW";
  }

  return "zh-TW";
}

const i18n = createI18n({
  legacy: false,
  locale: resolveInitialLocale(),
  fallbackLocale: "en",
  messages: {
    en,
    "zh-CN": zhCN,
    "zh-TW": zhTW,
  },
});

export default i18n;
