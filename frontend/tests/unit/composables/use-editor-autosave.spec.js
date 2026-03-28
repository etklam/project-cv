import { defineComponent } from "vue";
import { mount } from "@vue/test-utils";
import { afterEach, describe, expect, it, vi } from "vitest";
import { useEditorAutosave } from "@/composables/useEditorAutosave";

function mountAutosave(saveHandler, options = {}) {
  let autosaveRef = null;

  const wrapper = mount(defineComponent({
    template: "<div />",
    setup() {
      autosaveRef = useEditorAutosave(saveHandler, options);
      return {};
    },
  }));

  return {
    wrapper,
    autosave: autosaveRef,
  };
}

describe("useEditorAutosave", () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  it("debounces queued saves and only runs the latest request once", async () => {
    vi.useFakeTimers();
    const saveHandler = vi.fn().mockResolvedValue(undefined);
    const { autosave } = mountAutosave(saveHandler, { delay: 800 });

    autosave.queueAutosave();
    autosave.queueAutosave();
    autosave.queueAutosave();

    await vi.advanceTimersByTimeAsync(799);
    expect(saveHandler).not.toHaveBeenCalled();

    await vi.advanceTimersByTimeAsync(1);
    expect(saveHandler).toHaveBeenCalledTimes(1);
    expect(autosave.error.value).toBe("");
    expect(autosave.lastSavedAt.value).not.toBe("");
  });

  it("captures autosave errors without leaving saving stuck", async () => {
    vi.useFakeTimers();
    const saveHandler = vi.fn().mockRejectedValue(new Error("network down"));
    const { autosave } = mountAutosave(saveHandler, { delay: 800 });

    autosave.queueAutosave();
    await vi.advanceTimersByTimeAsync(800);
    await vi.runAllTicks();

    expect(saveHandler).toHaveBeenCalledTimes(1);
    expect(autosave.saving.value).toBe(false);
    expect(autosave.error.value).toBe("network down");
  });

  it("clears pending timers on unmount", async () => {
    vi.useFakeTimers();
    const saveHandler = vi.fn().mockResolvedValue(undefined);
    const { wrapper, autosave } = mountAutosave(saveHandler, { delay: 800 });

    autosave.queueAutosave();
    wrapper.unmount();
    await vi.advanceTimersByTimeAsync(800);

    expect(saveHandler).not.toHaveBeenCalled();
  });
});
