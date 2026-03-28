# Editor Workspace Implementation Plan

## Goal

Upgrade the current CV editor into a true workspace editor with:

- a fixed top toolbar
- a left editing pane
- a right live preview canvas
- autosave and editor status feedback
- section-oriented editing instead of one large mixed form

The target interaction model is the "editorial architect workspace" style:

- light material-like surfaces
- blue workspace accents
- higher information density
- persistent preview
- document-first editing flow

This plan focuses on frontend implementation first and preserves the current backend API contract where possible.

---

## Current Constraints

The current editor is still organized as a large page-level component:

- [CvEditorView.vue](/Users/klam/Desktop/project/project-cv/frontend/src/views/editor/CvEditorView.vue)

It currently mixes:

- metadata editing
- raw section editing
- preview rendering
- export handling
- validation state

Current rendering pipeline:

- [CvTemplateRenderer.vue](/Users/klam/Desktop/project/project-cv/frontend/src/components/cv-templates/CvTemplateRenderer.vue)
- [templateRegistry.js](/Users/klam/Desktop/project/project-cv/frontend/src/components/cv-templates/templateRegistry.js)

Current store dependency:

- [cv.js](/Users/klam/Desktop/project/project-cv/frontend/src/stores/cv.js)

This means the first priority is structural separation, not visual polish alone.

---

## Target UX

### Layout

Editor page should be split into 3 major zones:

1. Toolbar
2. Left editing workspace
3. Right preview workspace

### Toolbar

The toolbar should contain:

- document title
- autosave state
- preview/style mode toggle
- public/private state
- export PDF action
- publish action

### Left Pane

The left pane should contain section-oriented editors:

- Contact Details
- Professional Summary
- Experience
- Education
- Skills
- Custom Sections

### Right Pane

The right pane should contain:

- zoom controls
- preview/view mode controls
- public state badge
- export quick action
- A4 paper canvas

---

## Delivery Strategy

Implement in 5 phases to minimize breakage.

### Phase 1: Editor Shell Refactor

Goal:
Separate layout zones without changing the backend contract yet.

Files to modify:

- [CvEditorView.vue](/Users/klam/Desktop/project/project-cv/frontend/src/views/editor/CvEditorView.vue)

Files to add:

- [EditorToolbar.vue](/Users/klam/Desktop/project/project-cv/frontend/src/components/editor/EditorToolbar.vue)
- [EditorFormPane.vue](/Users/klam/Desktop/project/project-cv/frontend/src/components/editor/EditorFormPane.vue)
- [EditorPreviewPane.vue](/Users/klam/Desktop/project/project-cv/frontend/src/components/editor/EditorPreviewPane.vue)

Acceptance criteria:

- editor has fixed toolbar
- editor content is split into left/right panes
- preview pane remains visible at desktop widths
- existing `data-testid` values used by tests are preserved

### Phase 2: Draft Model Normalization

Goal:
Move from mixed page-local state to a normalized editor draft.

Files to add:

- [editorDraftAdapters.js](/Users/klam/Desktop/project/project-cv/frontend/src/components/editor/editorDraftAdapters.js)

Recommended draft model:

```js
const editorDraft = reactive({
  metadata: {
    title: "",
    templateKey: "minimal",
    isPublic: false,
    slug: "",
  },
  contact: {
    displayName: "",
    headline: "",
    email: "",
    location: "",
    website: "",
  },
  sections: [],
});

const uiState = reactive({
  activeTab: "preview",
  previewZoom: 85,
  previewMode: "document",
  saving: false,
  dirty: false,
  lastSavedAt: "",
  exportRunning: false,
});
```

Adapter functions:

- `normalizeCvToDraft(cv, sections)`
- `buildMetadataPayload(draft)`
- `buildSectionsPayload(draft)`

Acceptance criteria:

- editor renders from normalized state
- save/export continue to work with existing APIs
- no direct raw JSON editing remains in the main page component

### Phase 3: Section Editor Componentization

Goal:
Split editing responsibilities into focused section editors.

Files to add:

- [ContactEditor.vue](/Users/klam/Desktop/project/project-cv/frontend/src/components/editor/sections/ContactEditor.vue)
- [SummaryEditor.vue](/Users/klam/Desktop/project/project-cv/frontend/src/components/editor/sections/SummaryEditor.vue)
- [ExperienceEditor.vue](/Users/klam/Desktop/project/project-cv/frontend/src/components/editor/sections/ExperienceEditor.vue)
- [EducationEditor.vue](/Users/klam/Desktop/project/project-cv/frontend/src/components/editor/sections/EducationEditor.vue)
- [SkillsEditor.vue](/Users/klam/Desktop/project/project-cv/frontend/src/components/editor/sections/SkillsEditor.vue)
- [CustomSectionEditor.vue](/Users/klam/Desktop/project/project-cv/frontend/src/components/editor/sections/CustomSectionEditor.vue)

Responsibilities:

- each section editor only edits one slice of the draft
- parent page orchestrates persistence and preview

Acceptance criteria:

- section editors are individually testable
- adding/removing experience or education items is localized
- section validation does not rely on one giant component

### Phase 4: Preview Workspace Upgrade

Goal:
Turn preview into a proper document workspace instead of a plain render box.

Files to modify:

- [CvTemplateRenderer.vue](/Users/klam/Desktop/project/project-cv/frontend/src/components/cv-templates/CvTemplateRenderer.vue)
- [templateRegistry.js](/Users/klam/Desktop/project/project-cv/frontend/src/components/cv-templates/templateRegistry.js)
- [EditorPreviewPane.vue](/Users/klam/Desktop/project/project-cv/frontend/src/components/editor/EditorPreviewPane.vue)

Features:

- A4 paper shell
- zoom in / zoom out
- fit-to-canvas sizing
- live template switching
- status badge for public/private

Preview rules:

- canvas width target: `595px`
- canvas height target: `842px`
- preview zoom range: `50` to `125`
- zoom step: `5`

Acceptance criteria:

- preview responds instantly to draft changes
- zoom controls work without breaking layout
- export action still uses the current document state

### Phase 5: Autosave and Workflow Polish

Goal:
Make the editor feel like a true workspace, not a long form.

Files to add:

- [useEditorAutosave.js](/Users/klam/Desktop/project/project-cv/frontend/src/composables/useEditorAutosave.js)

Features:

- debounced autosave
- "saving" and "autosaved X ago" states
- non-blocking save errors
- dirty state detection

Suggested autosave policy:

- debounce: `800ms`
- metadata and sections saved independently
- autosave pauses during export

Acceptance criteria:

- toolbar shows autosave state
- draft changes trigger autosave
- save failures do not hard-block editing

---

## Component Contract Draft

### `EditorToolbar.vue`

Props:

- `title`
- `isPublic`
- `saving`
- `lastSavedAt`
- `previewZoom`
- `activeTab`
- `exportRunning`

Emits:

- `update:activeTab`
- `zoom-in`
- `zoom-out`
- `export`
- `publish`

### `EditorFormPane.vue`

Props:

- `draft`
- `templates`
- `templatesLoading`
- `templatesError`
- `sectionErrors`

Emits:

- `update:draft`
- `save-metadata`
- `save-sections`
- `add-section`
- `remove-section`

### `EditorPreviewPane.vue`

Props:

- `templateKey`
- `cv`
- `sections`
- `zoom`
- `mode`

Emits:

- `zoom-in`
- `zoom-out`
- `export`

---

## Visual System Guidance

Match the current workspace direction used in the app:

- primary blue accent
- light paper background
- slightly denser controls
- `Inter` for body
- `Manrope` for headings

Editor-specific UI rules:

- toolbar uses shallow elevation
- inputs are mostly surface-low with focused bottom-edge or border emphasis
- preview pane background should differ from form pane
- action buttons should prioritize export/publish hierarchy clearly

---

## Testing Plan

### Unit Tests

Update:

- [editor.spec.js](/Users/klam/Desktop/project/project-cv/frontend/tests/unit/views/editor.spec.js)

Add coverage for:

- editor draft initialization
- metadata update flow
- section add/remove
- experience item add/remove
- public toggle and slug normalization
- preview zoom controls
- autosave behavior
- export loading/success/error states

### E2E Tests

Add or extend Playwright coverage for:

- open editor
- change title
- autosave state appears
- zoom in preview
- export action remains available
- template switch updates preview

---

## Risks

### Risk 1: Raw section JSON coupling

The current editor still maps closely to generic section JSON. If this is not isolated behind adapters, the new UI will become brittle.

Mitigation:

- keep adapters in a dedicated module
- never let section editor components mutate raw API payloads directly

### Risk 2: Preview render inconsistency

If preview reads directly from mixed page-local state, render drift will happen.

Mitigation:

- preview should only consume normalized draft state

### Risk 3: Scope creep

Trying to ship drag-and-drop, undo/redo, and style switching in the same pass will slow the main refactor.

Mitigation:

- ship shell + draft + preview + autosave first
- defer reorder/history/theme switching to follow-up work

---

## Recommended Commit Sequence

1. `feat(editor): split editor into toolbar form and preview workspace`
2. `feat(editor): normalize draft state and add section adapters`
3. `feat(editor): componentize section editors`
4. `feat(editor): add preview canvas and zoom controls`
5. `feat(editor): add autosave workflow and status feedback`
6. `test(editor): expand unit and e2e coverage`

---

## Ready-to-Start Task List

### Task A

Create the new editor shell and move current page logic behind `EditorToolbar`, `EditorFormPane`, and `EditorPreviewPane`.

### Task B

Introduce draft adapters and stop editing raw section JSON in the main page component.

### Task C

Extract typed section editors for contact, summary, experience, education, and skills.

### Task D

Implement preview zoom and A4 canvas shell.

### Task E

Implement autosave with visible save state in the toolbar.

If approved, execution should start with Task A and Task B together.
