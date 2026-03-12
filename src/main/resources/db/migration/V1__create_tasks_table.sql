CREATE TABLE tasks
(
    id UUID PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    status      VARCHAR(50)  NOT NULL DEFAULT 'NEW',
    created_at  TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP
);