import { computed, ref } from "vue";
import { getTemplateMeta } from "@/components/cv-templates/templateRegistry";
import { listTemplates } from "@/api/template";

export function useTemplateCatalog(options = {}) {
  const {
    errorMessage = "Failed to load templates",
    limit = null,
  } = options;

  const templates = ref([]);
  const loading = ref(false);
  const error = ref("");

  const supportedTemplates = computed(() =>
    templates.value.filter((template) => getTemplateMeta(template.key)),
  );

  const visibleTemplates = computed(() => {
    if (limit == null) {
      return supportedTemplates.value;
    }
    return supportedTemplates.value.slice(0, limit);
  });

  const loadTemplates = async () => {
    loading.value = true;
    error.value = "";
    try {
      const data = await listTemplates();
      templates.value = data.templates || [];
    } catch (_error) {
      error.value = errorMessage;
    } finally {
      loading.value = false;
    }
  };

  return {
    templates,
    supportedTemplates,
    visibleTemplates,
    loading,
    error,
    loadTemplates,
  };
}
