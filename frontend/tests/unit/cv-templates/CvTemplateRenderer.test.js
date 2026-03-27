import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import CvTemplateRenderer from "@/components/cv-templates/CvTemplateRenderer.vue";

describe("CvTemplateRenderer", () => {
  it("renders known template through unified renderer", () => {
    const wrapper = mount(CvTemplateRenderer, {
      props: {
        templateKey: "minimal",
        cv: { title: "Frontend Engineer" },
        sections: [{ sectionType: "summary", content: { text: "hello" } }],
        mode: "public",
      },
    });

    expect(wrapper.find("[data-testid='template-minimal']").exists()).toBe(true);
  });

  it("renders MissingTemplate when template key is not registered", () => {
    const wrapper = mount(CvTemplateRenderer, {
      props: {
        templateKey: "ghost",
        cv: { title: "Unknown" },
        sections: [],
        mode: "editor-preview",
      },
    });

    expect(wrapper.text()).toContain("Template not found");
  });

  it.todo("logs template resolve errors for observability");
});
