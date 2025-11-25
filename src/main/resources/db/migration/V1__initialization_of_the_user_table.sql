CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    surname VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    patronymic VARCHAR(255) NOT NULL ,
    position VARCHAR(255) NOT NULL,
    faculty VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    last_login TIMESTAMP DEFAULT NOW()
);