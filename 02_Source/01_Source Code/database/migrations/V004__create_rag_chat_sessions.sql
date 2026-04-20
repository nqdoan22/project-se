-- V004__create_rag_chat_sessions.sql
-- Chat conversation state: session + message history for the frontend chat UI.

CREATE TABLE IF NOT EXISTS rag_chat_session (
    session_id   VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id      VARCHAR(36) NOT NULL,
    title        VARCHAR(200) NULL,
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_session_user (user_id, updated_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS rag_chat_message (
    message_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id   VARCHAR(36) NOT NULL,
    role         ENUM('user','assistant') NOT NULL,
    content      TEXT NOT NULL,
    sources      JSON NULL,
    query_log_id BIGINT NULL,
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_msg_session (session_id, created_date),
    CONSTRAINT fk_msg_session FOREIGN KEY (session_id) REFERENCES rag_chat_session(session_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
