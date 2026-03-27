import { mount } from "@vue/test-utils";
import { createPinia } from "pinia";
import { describe, expect, it } from "vitest";
import { createMemoryHistory, createRouter } from "vue-router";

import LoginView from "@/views/auth/LoginView.vue";
import RegisterView from "@/views/auth/RegisterView.vue";
import DashboardView from "@/views/dashboard/DashboardView.vue";
import CvEditorView from "@/views/editor/CvEditorView.vue";
import PrintCvView from "@/views/print/PrintCvView.vue";
import PublicProfileView from "@/views/public/PublicProfileView.vue";
import PublicCvView from "@/views/public/PublicCvView.vue";
import OnboardingLayout from "@/views/onboarding/OnboardingLayout.vue";
import Step1BasicInfo from "@/views/onboarding/Step1BasicInfo.vue";
import Step2ProfessionInfo from "@/views/onboarding/Step2ProfessionInfo.vue";
import Step3Template from "@/views/onboarding/Step3Template.vue";

const smokeCases = [
  ["login", LoginView, "/login", "/login"],
  ["register", RegisterView, "/register", "/register"],
  ["dashboard", DashboardView, "/dashboard", "/dashboard"],
  ["editor", CvEditorView, "/editor/:id", "/editor/1"],
  ["print", PrintCvView, "/print/:id", "/print/1"],
  ["public-profile", PublicProfileView, "/u/:username", "/u/alice"],
  ["public-cv", PublicCvView, "/u/:username/:slug", "/u/alice/product-resume"],
  ["onboarding-layout", OnboardingLayout, "/onboarding", "/onboarding"],
  ["onboarding-step1", Step1BasicInfo, "/onboarding/step1", "/onboarding/step1"],
  ["onboarding-step2", Step2ProfessionInfo, "/onboarding/step2", "/onboarding/step2"],
  ["onboarding-step3", Step3Template, "/onboarding/step3", "/onboarding/step3"],
];

describe("major view smoke tests", () => {
  it.each(smokeCases)("mounts %s view without crashing", async (_name, component, path, location) => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path, component }],
    });

    await router.push(location);
    await router.isReady();

    const wrapper = mount(component, {
      global: {
        plugins: [createPinia(), router],
        stubs: {
          RouterView: true,
        },
      },
    });
    expect(wrapper.exists()).toBe(true);
  });

  it.todo("dashboard reward widgets show state from backend summary");
  it.todo("editor export button handles insufficient credits flow");
});
