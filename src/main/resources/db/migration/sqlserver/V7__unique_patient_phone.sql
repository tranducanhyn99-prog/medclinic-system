-- Stop before changing data: operators must resolve duplicate/invalid legacy phones deliberately.
IF EXISTS (
    SELECT phone FROM dbo.users WHERE phone IS NOT NULL
    GROUP BY phone HAVING COUNT(*) > 1
)
    THROW 51007, 'Duplicate user phone numbers exist. Resolve conflicting accounts before applying V7; no profiles were merged or deleted.', 1;

IF EXISTS (
    SELECT 1 FROM dbo.users WHERE phone IS NOT NULL AND (
        DATALENGTH(phone) <> 20 OR phone NOT LIKE N'0[35789][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]' COLLATE Latin1_General_100_BIN2
    )
)
    THROW 51008, 'Invalid or blank user phone numbers exist. Normalize valid phones or explicitly clear optional blanks before applying V7.', 1;

CREATE UNIQUE INDEX ux_users_phone ON dbo.users(phone) WHERE phone IS NOT NULL;
ALTER TABLE dbo.users ADD CONSTRAINT ck_users_phone CHECK (
    phone IS NULL OR (DATALENGTH(phone) = 20 AND phone LIKE N'0[35789][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]' COLLATE Latin1_General_100_BIN2)
);
