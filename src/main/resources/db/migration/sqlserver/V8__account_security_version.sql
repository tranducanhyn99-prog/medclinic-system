ALTER TABLE users ADD security_version BIGINT NOT NULL
    CONSTRAINT df_users_security_version DEFAULT 0;
