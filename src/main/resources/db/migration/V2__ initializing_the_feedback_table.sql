CREATE TABLE feedback_users (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    feedback VARCHAR(500) NOT NULL,
    mood_feedback VARCHAR(50) NOT NULL,
    critical INT CHECK (critical BETWEEN 1 AND 5),
    recommendation VARCHAR(500) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);