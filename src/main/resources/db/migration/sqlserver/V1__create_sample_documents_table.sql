CREATE TABLE sample_documents (
    id UNIQUEIDENTIFIER PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    storage_key VARCHAR(256) NOT NULL,
    created_at_epoch_millis BIGINT NOT NULL
);
