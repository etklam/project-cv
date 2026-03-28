import { onBeforeUnmount, ref } from "vue";

export function useEditorAutosave(saveHandler, options = {}) {
  const delay = options.delay ?? 800;
  const saving = ref(false);
  const error = ref("");
  const lastSavedAt = ref("");
  let timer = null;

  const clearQueue = () => {
    if (timer) {
      clearTimeout(timer);
      timer = null;
    }
  };

  const runSave = async () => {
    clearQueue();
    saving.value = true;
    error.value = "";
    try {
      await saveHandler();
      lastSavedAt.value = new Date().toISOString();
    } catch (requestError) {
      error.value =
        requestError?.response?.data?.message ||
        requestError?.message ||
        "Autosave failed";
      throw requestError;
    } finally {
      saving.value = false;
    }
  };

  const queueAutosave = () => {
    clearQueue();
    timer = setTimeout(() => {
      runSave().catch(() => {});
    }, delay);
  };

  onBeforeUnmount(clearQueue);

  return {
    saving,
    error,
    lastSavedAt,
    queueAutosave,
    runSave,
    clearQueue,
  };
}
