import { flushPromises, mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import PublicProfileView from "@/views/public/PublicProfileView.vue";
import { getPublicProfile } from "@/api/public";

const routeParams = { username: "alice" };

vi.mock("vue-router", () => ({
  useRoute: () => ({ params: routeParams }),
}));

vi.mock("@/api/public", () => ({
  getPublicProfile: vi.fn(),
}));

describe("PublicProfileView", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    routeParams.username = "alice";
  });

  it("loads public profile and renders public CV links", async () => {
    vi.mocked(getPublicProfile).mockResolvedValue({
      user: { username: "alice", displayName: "Alice" },
      cvs: [
        { id: 1, title: "Backend CV", slug: "backend-cv" },
        { id: 2, title: "Product CV", slug: "product-cv" },
      ],
    });

    const wrapper = mount(PublicProfileView);
    await flushPromises();

    expect(getPublicProfile).toHaveBeenCalledWith("alice");
    expect(wrapper.get("[data-testid=public-profile-name]").text()).toContain("Alice");
    expect(wrapper.findAll("[data-testid=public-cv-link]")).toHaveLength(2);
  });

  it("shows error state when profile loading fails", async () => {
    vi.mocked(getPublicProfile).mockRejectedValue(new Error("network down"));

    const wrapper = mount(PublicProfileView);
    await flushPromises();

    expect(wrapper.get("[data-testid=public-profile-error]").text()).toContain("network down");
  });
});
