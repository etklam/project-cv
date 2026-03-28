import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import MinimalTemplate from "@/components/cv-templates/MinimalTemplate.vue";

describe("MinimalTemplate", () => {
  it("renders supported section components", () => {
    const wrapper = mount(MinimalTemplate, {
      props: {
        cv: { title: "Resume" },
        sections: [
          { sectionType: "summary", content: { text: "Product engineer" } },
          {
            sectionType: "experience",
            content: { items: [{ company: "Acme", role: "Engineer", startDate: "2024-01", current: true }] },
          },
          {
            sectionType: "education",
            content: { items: [{ school: "NTU", degree: "CS", years: "2018-2022" }] },
          },
          {
            sectionType: "skills",
            content: { items: ["Kotlin", "Vue"] },
          },
        ],
      },
    });

    expect(wrapper.text()).toContain("Product engineer");
    expect(wrapper.text()).toContain("Acme");
    expect(wrapper.text()).toContain("NTU");
    expect(wrapper.text()).toContain("Kotlin");
  });
});
