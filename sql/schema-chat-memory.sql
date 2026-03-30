-- 对话记忆表结构（仅作仓库记录；由你在库中自行执行建表，应用不会自动跑本文件）
-- Spring AI 对话记忆（JdbcTemplate / PostgreSQL）
CREATE TABLE IF NOT EXISTS chat_conversation (
    conversation_id VARCHAR(128) PRIMARY KEY,
    user_id         VARCHAR(64),
    next_seq        INT         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS chat_message (
    id               BIGSERIAL PRIMARY KEY,
    conversation_id  VARCHAR(128) NOT NULL
        REFERENCES chat_conversation (conversation_id) ON DELETE CASCADE,
    seq              INT          NOT NULL,
    message_type     VARCHAR(32)  NOT NULL,
    text_content     TEXT         NOT NULL DEFAULT '',
    metadata         JSONB,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (conversation_id, seq)
);

CREATE INDEX IF NOT EXISTS idx_chat_message_conv_seq
    ON chat_message (conversation_id, seq);
