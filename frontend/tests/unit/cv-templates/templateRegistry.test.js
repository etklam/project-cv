import { describe, expect, it } from "vitest";
import fs from "node:fs";
import path from "node:path";
import {
  TEMPLATE_KEYS,
  getTemplateMeta,
  resolveTemplateComponent,
} from "@/components/cv-templates/templateRegistry";

function loadSeededTemplateKeysFromBackendMigrations() {
  const migrationDirectory = path.resolve(
    process.cwd(),
    "../backend/src/main/resources/db/migration",
  );
  const migrationFiles = fs
    .readdirSync(migrationDirectory)
    .filter((fileName) => fileName.endsWith(".sql"))
    .sort();
  const backendKeys = new Set();

  for (const migrationFile of migrationFiles) {
    const sql = fs.readFileSync(path.join(migrationDirectory, migrationFile), "utf8");
    const insertBlocks =
      sql.match(/INSERT INTO templates[\s\S]*?ON CONFLICT \(component_key\) DO NOTHING;/g) || [];

    for (const insertBlock of insertBlocks) {
      const tupleStartPattern = /\(\s*'([^']+)'/g;
      let match = tupleStartPattern.exec(insertBlock);
      while (match) {
        backendKeys.add(match[1]);
        match = tupleStartPattern.exec(insertBlock);
      }
    }
  }

  if (backendKeys.size === 0) {
    throw new Error("templates seed insert block not found in backend migrations");
  }

  return [...backendKeys];
}

describe("templateRegistry", () => {
  it("registers core templates", () => {
    expect(TEMPLATE_KEYS).toEqual(["minimal", "sidebar", "modern"]);
  });

  it("resolves existing template component", () => {
    const component = resolveTemplateComponent("minimal");
    expect(component?.name).toBe("MinimalTemplate");
  });

  it("falls back to MissingTemplate for unknown key", () => {
    const component = resolveTemplateComponent("non-existent");
    expect(component?.name).toBe("MissingTemplate");
  });

  it.todo("enforces supportsPrint contract for print mode rendering");
  it("aligns registry keys with backend templates.component_key contract check", () => {
    const backendSeededKeys = loadSeededTemplateKeysFromBackendMigrations().sort();
    const frontendRegistryKeys = [...TEMPLATE_KEYS].sort();

    expect(frontendRegistryKeys).toEqual(backendSeededKeys);
  });

  it("returns template metadata for onboarding/dashboard cards", () => {
    const meta = getTemplateMeta("modern");
    expect(meta).toMatchObject({
      key: "modern",
      supportsPrint: true,
    });
  });
});
