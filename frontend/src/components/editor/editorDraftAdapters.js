function clone(value) {
  return JSON.parse(JSON.stringify(value));
}

export function normalizeSlug(value) {
  return String(value || "")
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9-]+/g, "-")
    .replace(/-+/g, "-")
    .replace(/^-|-$/g, "");
}

function createSection(sectionType, title, content = {}, extras = {}) {
  return {
    id: null,
    sectionType,
    sortOrder: 0,
    title,
    content: clone(content),
    ...extras,
  };
}

export function createEmptyDraft() {
  return {
    metadata: {
      title: "",
      templateKey: "minimal",
      isPublic: false,
      slug: "",
    },
    contact: createSection("contact", "Contact", {
      displayName: "",
      headline: "",
      email: "",
      location: "",
      website: "",
    }),
    summary: createSection("summary", "Summary", { text: "" }),
    skills: createSection("skills", "Skills", { items: [] }),
    experience: createSection("experience", "Work Experience", { items: [] }),
    education: createSection("education", "Education", { items: [] }),
    customSections: [],
  };
}

export function normalizeCvToDraft(cv, sections = []) {
  const draft = createEmptyDraft();

  draft.metadata = {
    title: cv?.title || "",
    templateKey: cv?.templateKey || "minimal",
    isPublic: Boolean(cv?.isPublic),
    slug: cv?.slug || "",
  };

  sections.forEach((section, index) => {
    const nextSection = {
      ...createSection(section?.sectionType || "custom", section?.title || "", section?.content || {}),
      ...clone(section || {}),
      content: clone(section?.content || {}),
      sortOrder: section?.sortOrder ?? index,
    };

    switch (nextSection.sectionType) {
      case "contact":
        draft.contact = {
          ...draft.contact,
          ...nextSection,
          content: {
            displayName: nextSection.content?.displayName || "",
            headline: nextSection.content?.headline || "",
            email: nextSection.content?.email || "",
            location: nextSection.content?.location || "",
            website: nextSection.content?.website || "",
          },
        };
        break;
      case "summary":
        draft.summary = {
          ...draft.summary,
          ...nextSection,
          content: {
            text: nextSection.content?.text || "",
          },
        };
        break;
      case "skills":
        draft.skills = {
          ...draft.skills,
          ...nextSection,
          content: {
            items: Array.isArray(nextSection.content?.items) ? nextSection.content.items : [],
          },
        };
        break;
      case "experience":
        draft.experience = {
          ...draft.experience,
          ...nextSection,
          content: {
            items: Array.isArray(nextSection.content?.items) ? nextSection.content.items : [],
          },
        };
        break;
      case "education":
        draft.education = {
          ...draft.education,
          ...nextSection,
          content: {
            items: Array.isArray(nextSection.content?.items) ? nextSection.content.items : [],
          },
        };
        break;
      default:
        draft.customSections.push(nextSection);
    }
  });

  return draft;
}

function isSectionEmpty(section) {
  if (!section) {
    return true;
  }
  if (section.sectionType === "summary") {
    return !String(section.content?.text || "").trim();
  }
  if (section.sectionType === "contact") {
    return !["displayName", "headline", "email", "location", "website"].some((field) =>
      String(section.content?.[field] || "").trim(),
    );
  }
  if (section.sectionType === "skills") {
    return !(section.content?.items || []).length;
  }
  if (["experience", "education"].includes(section.sectionType)) {
    return !(section.content?.items || []).some((item) =>
      Object.values(item || {}).some((value) => String(value || "").trim()),
    );
  }
  return !String(section.title || "").trim() && !Object.values(section.content || {}).some((value) => String(value || "").trim());
}

function serializeSection(section, sortOrder) {
  return {
    id: section?.id || null,
    sectionType: section?.sectionType,
    sortOrder,
    title: section?.title || "",
    content: clone(section?.content || {}),
  };
}

export function buildSectionsPayload(draft) {
  const sections = [
    draft?.contact,
    draft?.summary,
    draft?.experience,
    draft?.education,
    draft?.skills,
    ...(draft?.customSections || []),
  ]
    .filter((section) => section?.sectionType)
    .filter((section) => section?.id || !isSectionEmpty(section))
    .map((section, index) => serializeSection(section, index));

  return { sections };
}

export function buildMetadataPayload(draft) {
  const metadata = draft?.metadata || {};
  const normalizedSlug = normalizeSlug(metadata.slug);

  return {
    title: String(metadata.title || "").trim(),
    templateKey: metadata.templateKey || "minimal",
    isPublic: Boolean(metadata.isPublic),
    slug: metadata.isPublic ? normalizedSlug || null : null,
  };
}

export function buildPreviewCv(draft, fallbackCv = {}) {
  return {
    ...fallbackCv,
    title: draft?.metadata?.title || fallbackCv?.title || "",
    templateKey: draft?.metadata?.templateKey || fallbackCv?.templateKey || "minimal",
    isPublic: Boolean(draft?.metadata?.isPublic),
    slug: draft?.metadata?.isPublic ? normalizeSlug(draft?.metadata?.slug) : null,
  };
}
