-- Restore JDBC-safe SQL Server session state after reference-data migration.
-- V2 intentionally used NOCOUNT ON for idempotent seed statements; leaving that
-- session option enabled can break Hibernate IDENTITY inserts when the pooled
-- Flyway connection is later reused by JPA.
SET NOCOUNT OFF;
