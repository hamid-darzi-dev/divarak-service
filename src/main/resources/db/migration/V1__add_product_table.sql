CREATE TABLE IF NOT EXISTS PRODUCT (
    id          UUID        PRIMARY KEY,
    name        TEXT        NOT NULL,
    description TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL
);
