CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    username VARCHAR(50),
    display_name VARCHAR(100) NOT NULL,
    avatar_path VARCHAR(500),
    onboarding_draft JSONB,
    onboarding_status VARCHAR(20) NOT NULL DEFAULT 'STEP_1',
    locale VARCHAR(10) NOT NULL DEFAULT 'zh-TW',
    credit_balance INT NOT NULL DEFAULT 0,
    invite_code VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    is_deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE UNIQUE INDEX IF NOT EXISTS users_email_idx ON users (email);
CREATE UNIQUE INDEX IF NOT EXISTS users_username_idx ON users (username);
CREATE UNIQUE INDEX IF NOT EXISTS users_invite_code_idx ON users (invite_code);

CREATE TABLE IF NOT EXISTS templates (
    id BIGSERIAL PRIMARY KEY,
    component_key VARCHAR(50) NOT NULL UNIQUE,
    display_name_i18n JSONB NOT NULL,
    description_i18n JSONB,
    preview_image VARCHAR(500),
    credit_cost INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    is_deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS templates_sort_order_idx ON templates (sort_order, id);

CREATE TABLE IF NOT EXISTS credit_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    amount INT NOT NULL,
    balance_after INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    description VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS credit_transactions_user_id_idx ON credit_transactions (user_id, id DESC);

CREATE TABLE IF NOT EXISTS promo_codes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    campaign_key VARCHAR(50),
    reward_type VARCHAR(30) NOT NULL,
    reward_value INT NOT NULL,
    max_redemptions INT,
    starts_at TIMESTAMP WITHOUT TIME ZONE,
    expires_at TIMESTAMP WITHOUT TIME ZONE,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT promo_codes_code_namespace_chk CHECK (code NOT LIKE 'INV-%')
);

CREATE UNIQUE INDEX IF NOT EXISTS promo_codes_code_idx ON promo_codes (code);

CREATE TABLE IF NOT EXISTS promo_code_redemptions (
    id BIGSERIAL PRIMARY KEY,
    promo_code_id BIGINT NOT NULL REFERENCES promo_codes(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    reward_value INT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS promo_code_redemptions_promo_user_idx
    ON promo_code_redemptions (promo_code_id, user_id);
CREATE INDEX IF NOT EXISTS promo_code_redemptions_user_id_idx
    ON promo_code_redemptions (user_id, id DESC);

CREATE TABLE IF NOT EXISTS invite_code_redemptions (
    id BIGSERIAL PRIMARY KEY,
    inviter_user_id BIGINT NOT NULL REFERENCES users(id),
    invitee_user_id BIGINT NOT NULL REFERENCES users(id),
    invite_code VARCHAR(20) NOT NULL,
    inviter_reward INT NOT NULL,
    invitee_reward INT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    CONSTRAINT invite_code_redemptions_self_chk CHECK (inviter_user_id <> invitee_user_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS invite_code_redemptions_invitee_idx
    ON invite_code_redemptions (invitee_user_id);
CREATE INDEX IF NOT EXISTS invite_code_redemptions_inviter_idx
    ON invite_code_redemptions (inviter_user_id, id DESC);

INSERT INTO users (email, password_hash, display_name, locale, credit_balance, invite_code)
VALUES
    ('alice@example.com', 'dev-password-hash', 'Alice', 'zh-TW', 50, 'INV-ALICE1'),
    ('bob@example.com', 'dev-password-hash', 'Bob', 'en', 50, 'INV-BOB001')
ON CONFLICT (email) DO NOTHING;

INSERT INTO promo_codes (code, campaign_key, reward_type, reward_value, max_redemptions, is_active)
VALUES
    ('WELCOME2026', 'launch', 'CREDIT_FIXED', 30, NULL, true),
    ('SPRINGCV', 'seasonal', 'CREDIT_FIXED', 15, 100, true)
ON CONFLICT (code) DO NOTHING;

INSERT INTO templates (component_key, display_name_i18n, description_i18n, preview_image, credit_cost, is_active, sort_order)
VALUES
    (
        'minimal',
        '{"en":"Minimal","zh-CN":"简约","zh-TW":"簡約"}'::jsonb,
        '{"en":"Clean layout that highlights your text and achievements","zh-CN":"干净的版式，突出文字与成果","zh-TW":"乾淨版型，凸顯文字與成果"}'::jsonb,
        '/images/templates/minimal.png',
        0,
        true,
        0
    ),
    (
        'sidebar',
        '{"en":"Sidebar","zh-CN":"侧边栏","zh-TW":"側邊欄"}'::jsonb,
        '{"en":"Split layout with persistent navigation for experiences","zh-CN":"分栏式排版，鎖定经验导航","zh-TW":"分欄式排版，鎖定經驗導航"}'::jsonb,
        '/images/templates/sidebar.png',
        0,
        true,
        1
    ),
    (
        'modern',
        '{"en":"Modern","zh-CN":"现代","zh-TW":"現代"}'::jsonb,
        '{"en":"Visual-forward template with accent colors","zh-CN":"视觉导向样式，搭配强调色","zh-TW":"視覺主導樣式，搭配強調色"}'::jsonb,
        '/images/templates/modern.png',
        5,
        true,
        2
    )
ON CONFLICT (component_key) DO NOTHING;
