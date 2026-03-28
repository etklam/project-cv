<script>
import { useI18n } from "vue-i18n";

export default {
  name: "MissingTemplate",
  props: {
    templateKey: {
      type: String,
      default: "",
    },
  },
  setup() {
    const { t } = useI18n();
    return { t };
  },
};
</script>

<template>
  <section data-testid="template-missing" class="bg-yellow-50 border border-yellow-200 rounded-lg p-6 text-center">
    <h2 class="text-lg font-semibold text-yellow-800">{{ t("errors.general") }}</h2>
    <p class="text-yellow-700 mt-2">{{ t("editor.title") }}: {{ templateKey }}</p>
    <p class="text-yellow-600 text-sm mt-1">Template not found</p>
  </section>
</template>
