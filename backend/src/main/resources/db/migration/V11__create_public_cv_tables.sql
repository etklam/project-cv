CREATE TABLE IF NOT EXISTS cvs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    template_key VARCHAR(50) NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT false,
    slug VARCHAR(100),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    is_deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS cvs_user_id_idx ON cvs (user_id, id DESC);
CREATE UNIQUE INDEX IF NOT EXISTS cvs_slug_idx ON cvs (slug) WHERE slug IS NOT NULL;

CREATE TABLE IF NOT EXISTS cv_sections (
    id BIGSERIAL PRIMARY KEY,
    cv_id BIGINT NOT NULL REFERENCES cvs(id),
    section_type VARCHAR(30) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    title VARCHAR(200),
    content JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    is_deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS cv_sections_cv_id_sort_idx ON cv_sections (cv_id, sort_order, id);

UPDATE users
SET
    username = CASE email
        WHEN 'alice@example.com' THEN 'alice'
        WHEN 'bob@example.com' THEN 'bob'
        ELSE username
    END,
    onboarding_status = 'DONE'
WHERE email IN ('alice@example.com', 'bob@example.com')
  AND username IS NULL;

INSERT INTO cvs (user_id, title, template_key, is_public, slug)
SELECT u.id, 'Alice Product Resume', 'minimal', true, 'product-resume'
FROM users u
WHERE u.email = 'alice@example.com'
  AND NOT EXISTS (
      SELECT 1
      FROM cvs c
      WHERE c.user_id = u.id
        AND c.slug = 'product-resume'
  );

INSERT INTO cv_sections (cv_id, section_type, sort_order, title, content)
SELECT c.id, 'summary', 0, 'Summary', '{"text":"Product-minded full-stack engineer with strong ownership and delivery focus."}'::jsonb
FROM cvs c
JOIN users u ON u.id = c.user_id
WHERE u.email = 'alice@example.com'
  AND c.slug = 'product-resume'
  AND NOT EXISTS (
      SELECT 1
      FROM cv_sections s
      WHERE s.cv_id = c.id
        AND s.section_type = 'summary'
  );

INSERT INTO cv_sections (cv_id, section_type, sort_order, title, content)
SELECT c.id, 'experience', 1, 'Experience', '{"items":[{"company":"Acme Labs","role":"Senior Engineer","startDate":"2022-01","endDate":"2026-03","current":true,"description":"Led resume workflow and platform improvements."}]}'::jsonb
FROM cvs c
JOIN users u ON u.id = c.user_id
WHERE u.email = 'alice@example.com'
  AND c.slug = 'product-resume'
  AND NOT EXISTS (
      SELECT 1
      FROM cv_sections s
      WHERE s.cv_id = c.id
        AND s.section_type = 'experience'
  );

INSERT INTO cv_sections (cv_id, section_type, sort_order, title, content)
SELECT c.id, 'skills', 2, 'Skills', '{"items":["Kotlin","Spring Boot","Vue 3","PostgreSQL"]}'::jsonb
FROM cvs c
JOIN users u ON u.id = c.user_id
WHERE u.email = 'alice@example.com'
  AND c.slug = 'product-resume'
  AND NOT EXISTS (
      SELECT 1
      FROM cv_sections s
      WHERE s.cv_id = c.id
        AND s.section_type = 'skills'
  );
