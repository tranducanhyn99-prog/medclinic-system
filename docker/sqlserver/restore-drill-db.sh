#!/usr/bin/env bash
set -euo pipefail

SOURCE="${1:-}"
DRILL_DB="${2:-}"
if [[ ! "$SOURCE" =~ ^/var/opt/mssql/backup/medclinic_[0-9]{8}_[0-9]{6}\.bak$ ]]; then
  echo "Invalid backup source" >&2
  exit 2
fi
if [[ ! "$DRILL_DB" =~ ^medclinic_restore_drill_[0-9]{14}$ ]]; then
  echo "Invalid drill database name" >&2
  exit 2
fi

SQLCMD=/opt/mssql-tools18/bin/sqlcmd
[[ -x "$SQLCMD" ]] || SQLCMD=/opt/mssql-tools/bin/sqlcmd

"$SQLCMD" -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -b -Q "
RESTORE DATABASE [$DRILL_DB]
FROM DISK = N'$SOURCE'
WITH MOVE N'medclinic_db' TO N'/var/opt/mssql/data/${DRILL_DB}.mdf',
     MOVE N'medclinic_db_log' TO N'/var/opt/mssql/data/${DRILL_DB}_log.ldf',
     RECOVERY, REPLACE;
DBCC CHECKDB ([$DRILL_DB]) WITH NO_INFOMSGS;
DECLARE @tables int;
SELECT @tables = COUNT(*) FROM [$DRILL_DB].sys.tables WHERE is_ms_shipped = 0;
IF @tables < 18 THROW 51000, 'Restore drill found too few application tables', 1;
ALTER DATABASE [$DRILL_DB] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
DROP DATABASE [$DRILL_DB];
"
