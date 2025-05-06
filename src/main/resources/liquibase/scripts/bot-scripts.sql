-- liquibase formatted sql

-- changeset sbakhareva:1
CREATE TABLE notification_task (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    text TEXT NOT NULL
);

-- changeset sbakhareva:2
ALTER TABLE notification_task DROP id,
ADD id BIGSERIAL,
ADD chat_id INTEGER,
ADD PRIMARY KEY (id, chat_id)
