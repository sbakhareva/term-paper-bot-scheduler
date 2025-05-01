-- liquibase formatted sql

-- changeset sbakhareva:1
CREATE TABLE notification_task (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    text TEXT NOT NULL
);