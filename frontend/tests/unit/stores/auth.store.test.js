import { setActivePinia, createPinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vitest";
import i18n from "@/i18n";
import { useAuthStore } from "@/stores/auth";

vi.mock("@/api/auth", () => {
  return {
    me: vi.fn(),
    changeLocale: vi.fn(),
    login: vi.fn(),
    logout: vi.fn(),
    register: vi.fn(),
  };
});

import { me, changeLocale } from "@/api/auth";

describe("auth store", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.resetAllMocks();
    i18n.global.locale.value = "zh-TW";
  });

  it("bootstrap sets user and auth flags from /auth/me", async () => {
    me.mockResolvedValue({
      user: {
        id: 1,
        email: "test@example.com",
        onboarding_status: "STEP_1",
        locale: "zh-TW",
        creditBalance: 50,
      },
    });

    const store = useAuthStore();
    await store.bootstrap();

    expect(store.initialized).toBe(true);
    expect(store.isLoggedIn).toBe(true);
    expect(store.user?.email).toBe("test@example.com");
    expect(i18n.global.locale.value).toBe("zh-TW");
  });

  it("setLocale updates local state and invokes backend API when logged in", async () => {
    changeLocale.mockResolvedValue({ user: { locale: "en" } });

    const store = useAuthStore();
    store.user = { id: 1, onboarding_status: "DONE", locale: "zh-TW" };
    store.isLoggedIn = true;

    await store.setLocale("en");

    expect(store.locale).toBe("en");
    expect(changeLocale).toHaveBeenCalledWith({ locale: "en" });
    expect(i18n.global.locale.value).toBe("en");
  });

  it.todo("bootstrap gracefully handles 401 and keeps app in guest mode");
});
