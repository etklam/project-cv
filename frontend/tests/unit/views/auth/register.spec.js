import { mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import RegisterView from "@/views/auth/RegisterView.vue";

const push = vi.fn();
const register = vi.fn();

vi.mock("vue-router", () => ({
  useRouter: () => ({ push }),
}));

vi.mock("@/stores/auth", () => ({
  useAuthStore: () => ({ register }),
}));

describe("RegisterView", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("submits registration data and redirects to onboarding", async () => {
    register.mockResolvedValue({ user: { id: 1 } });

    const wrapper = mount(RegisterView);

    await wrapper.get("[data-testid=register-display-name]").setValue("Ada");
    await wrapper.get("[data-testid=register-email]").setValue("ada@example.com");
    await wrapper.get("[data-testid=register-password]").setValue("secret");
    await wrapper.get("form").trigger("submit");

    expect(register).toHaveBeenCalledWith({
      displayName: "Ada",
      email: "ada@example.com",
      password: "secret",
    });
    expect(push).toHaveBeenCalledWith("/onboarding/step1");
  });

  it("shows register errors", async () => {
    register.mockRejectedValue(new Error("email already used"));

    const wrapper = mount(RegisterView);

    await wrapper.get("[data-testid=register-display-name]").setValue("Ada");
    await wrapper.get("[data-testid=register-email]").setValue("ada@example.com");
    await wrapper.get("[data-testid=register-password]").setValue("secret");
    await wrapper.get("form").trigger("submit");

    expect(wrapper.get("[data-testid=register-error]").text()).toContain("email already used");
  });
});
