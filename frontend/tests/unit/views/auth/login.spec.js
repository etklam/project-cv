import { mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import LoginView from "@/views/auth/LoginView.vue";

const push = vi.fn();
const login = vi.fn();

vi.mock("vue-router", () => ({
  useRouter: () => ({ push }),
}));

vi.mock("@/stores/auth", () => ({
  useAuthStore: () => ({ login }),
}));

describe("LoginView", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("submits credentials and redirects to dashboard", async () => {
    login.mockResolvedValue({ user: { id: 1 } });

    const wrapper = mount(LoginView);

    await wrapper.get("[data-testid=login-email]").setValue("ada@example.com");
    await wrapper.get("[data-testid=login-password]").setValue("secret");
    await wrapper.get("form").trigger("submit");

    expect(login).toHaveBeenCalledWith({
      email: "ada@example.com",
      password: "secret",
    });
    expect(push).toHaveBeenCalledWith("/dashboard");
  });

  it("shows backend error messages", async () => {
    login.mockRejectedValue(new Error("bad credentials"));

    const wrapper = mount(LoginView);

    await wrapper.get("[data-testid=login-email]").setValue("ada@example.com");
    await wrapper.get("[data-testid=login-password]").setValue("wrong");
    await wrapper.get("form").trigger("submit");

    expect(wrapper.get("[data-testid=login-error]").text()).toContain("bad credentials");
  });
});
