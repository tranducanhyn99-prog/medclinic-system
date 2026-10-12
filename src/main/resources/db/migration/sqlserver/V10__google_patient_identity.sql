ALTER TABLE users ADD google_subject NVARCHAR(255) COLLATE Latin1_General_100_BIN2 NULL;
GO
CREATE UNIQUE INDEX ux_users_google_subject ON users(google_subject)
WHERE google_subject IS NOT NULL;
