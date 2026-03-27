import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";

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
  ["login", LoginView],
  ["register", RegisterView],
  ["dashboard", DashboardView],
  ["editor", CvEditorView],
  ["print", PrintCvView],
  ["public-profile", PublicProfileView],
  ["public-cv", PublicCvView],
  ["onboarding-layout", OnboardingLayout],
  ["onboarding-step1", Step1BasicInfo],
  ["onboarding-step2", Step2ProfessionInfo],
  ["onboarding-step3", Step3Template],
];

describe("major view smoke tests", () => {
  it.each(smokeCases)("mounts %s view without crashing", (_name, component) => {
    const wrapper = mount(component, {
      global: {
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
