import { expect, test } from "@playwright/test";

function uniqueIdentity() {
  const stamp = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
  return {
    email: `e2e-${stamp}@example.com`,
    password: "Password123!",
    displayName: `E2E ${stamp.slice(-6)}`,
    username: `e2e${stamp.replace(/[^a-z0-9]/gi, "").toLowerCase()}`.slice(0, 20),
  };
}

async function registerFreshUser(page, identity) {
  await page.goto("/register");
  await expect(page.getByTestId("view-register")).toBeVisible();

  const registerForm = page.getByTestId("view-register");
  const inputs = registerForm.locator("input");
  await inputs.nth(0).fill(identity.displayName);
  await inputs.nth(1).fill(identity.email);
  await inputs.nth(2).fill(identity.password);
  await registerForm.locator("button[type='submit']").click();

  await expect(page).toHaveURL(/\/onboarding\/step1$/);
  await expect(page.getByTestId("view-onboarding-step1")).toBeVisible();
}

async function completeOnboarding(page, identity) {
  const step1 = page.getByTestId("view-onboarding-step1");
  const step1Inputs = step1.locator("input");
  await step1Inputs.nth(1).fill(identity.username);
  await step1.locator("button[type='submit']").click();

  await expect(page).toHaveURL(/\/onboarding\/step2$/);
  const step2 = page.getByTestId("view-onboarding-step2");
  await expect(step2).toBeVisible();
  await step2.locator("button[type='submit']").click();

  await expect(page).toHaveURL(/\/onboarding\/step3$/);
  const step3 = page.getByTestId("view-onboarding-step3");
  await expect(step3).toBeVisible();
  await expect(page.getByTestId("template-grid")).toBeVisible();
  await expect(page.locator("[data-testid^='template-card-']").first()).toBeVisible();
  await expect(page.getByTestId("selected-template")).not.toHaveText(/^Select$/);
}

test.describe("project-cv system smoke", () => {
  test("redirects anonymous visitors to the login page without rendering a blank shell", async ({ page }) => {
    await page.goto("/");

    await expect(page).toHaveURL(/\/login$/);
    await expect(page.getByTestId("view-login")).toBeVisible();
    await expect(page.getByTestId("view-login").locator("form")).toBeVisible();
    await expect(page.locator("body")).toContainText(/log in|登入/i);
  });

  test("serves active templates from the real backend", async ({ request }) => {
    const response = await request.get("/api/v1/templates");
    expect(response.ok()).toBeTruthy();

    const payload = await response.json();
    expect(Array.isArray(payload?.data?.templates)).toBeTruthy();
    expect(payload.data.templates.length).toBeGreaterThan(0);
  });

  test("registers a fresh user and lands on onboarding step 1", async ({ page }) => {
    const identity = uniqueIdentity();
    await registerFreshUser(page, identity);
  });

  test("reaches onboarding step 3 and loads live template data", async ({ page }) => {
    const identity = uniqueIdentity();

    await registerFreshUser(page, identity);
    await completeOnboarding(page, identity);

    await expect(page.getByTestId("template-grid")).toBeVisible();
    await expect(page.getByTestId("selected-template")).not.toHaveText(/^Select$/);
  });
});
