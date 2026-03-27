import MinimalTemplate from "@/components/cv-templates/MinimalTemplate.vue";
import SidebarTemplate from "@/components/cv-templates/SidebarTemplate.vue";
import ModernTemplate from "@/components/cv-templates/ModernTemplate.vue";
import MissingTemplate from "@/components/cv-templates/MissingTemplate.vue";

const TEMPLATE_MANIFEST = {
  minimal: {
    key: "minimal",
    component: MinimalTemplate,
    supportsPrint: true,
  },
  sidebar: {
    key: "sidebar",
    component: SidebarTemplate,
    supportsPrint: true,
  },
  modern: {
    key: "modern",
    component: ModernTemplate,
    supportsPrint: true,
  },
};

export const TEMPLATE_KEYS = Object.keys(TEMPLATE_MANIFEST);

export function getTemplateMeta(templateKey) {
  return TEMPLATE_MANIFEST[templateKey] || null;
}

export function resolveTemplateComponent(templateKey) {
  const meta = getTemplateMeta(templateKey);
  return meta?.component || MissingTemplate;
}
