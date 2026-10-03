#!/usr/bin/env bash
set -euo pipefail

TARGET="${1:-}"
if [[ ! "$TARGET" =~ ^/var/opt/mssql/backup/medclinic_[0-9]{8}_[0-9]{6}\.bak$ ]]; then
  echo "Invalid backup target" >&2
  exit 2
fi

SQLCMD=/opt/mssql-tools18/bin/sqlcmd
[[ -x "$SQLCMD" ]] || SQLCMD=/opt/mssql-tools/bin/sqlcmd
mkdir -p /var/opt/mssql/backup

"$SQLCMD" -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -b -Q \
  "BACKUP DATABASE [medclinic_db] TO DISK = N'$TARGET' WITH COPY_ONLY, INIT, CHECKSUM, STATS = 10; RESTORE VERIFYONLY FROM DISK = N'$TARGET' WITH CHECKSUM;"
