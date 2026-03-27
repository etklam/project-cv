import { describe, expect, it } from "vitest";
import { routeGuardDecision } from "@/router";

function createAuthMock(overrides = {}) {
  return {
    initialized: true,
    isLoggedIn: false,
    user: null,
    bootstrap: async () => {},
    ...overrides,
  };
}

describe("router bootstrap guard", () => {
  it("redirects unauthenticated user to /login for auth-required page", async () => {
    const auth = createAuthMock();
    const result = await routeGuardDecision({ path: "/dashboard", meta: { requiresAuth: true } }, auth);
    expect(result).toBe("/login");
  });

  it("redirects onboarding-incomplete user to onboarding step", async () => {
    const auth = createAuthMock({
      isLoggedIn: true,
      user: { onboarding_status: "STEP_2" },
    });
    const result = await routeGuardDecision({ path: "/dashboard", meta: { requiresAuth: true } }, auth);
    expect(result).toBe("/onboarding/step2");
  });

  it("allows authenticated user with DONE onboarding to access dashboard", async () => {
    const auth = createAuthMock({
      isLoggedIn: true,
      user: { onboarding_status: "DONE" },
    });
    const result = await routeGuardDecision({ path: "/dashboard", meta: { requiresAuth: true } }, auth);
    expect(result).toBe(true);
  });

  it.todo("blocks /onboarding/* when onboarding_status is DONE");
  it.todo("handles bootstrap failure with safe fallback route");
});
