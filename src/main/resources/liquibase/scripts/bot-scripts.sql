-- liquibase formatted sql

-- changeset sbakhareva:1
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    time_stamp TIMESTAMP NOT NULL,
    message_text TEXT NOT NULL
);