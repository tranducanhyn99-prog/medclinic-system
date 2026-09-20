#!/usr/bin/env bash
set -euo pipefail

SQLCMD="/opt/mssql-tools18/bin/sqlcmd"
if [ ! -x "$SQLCMD" ]; then
  SQLCMD="/opt/mssql-tools/bin/sqlcmd"
fi

if [ ! -x "$SQLCMD" ]; then
  echo "sqlcmd không tồn tại trong image SQL Server."
  exit 1
fi

SERVER="sqlserver"
SA_USER="sa"
DB_NAME="medclinic_db"

until "$SQLCMD" -S "$SERVER" -U "$SA_USER" -P "$MSSQL_SA_PASSWORD" -C -b -Q "SELECT 1" >/dev/null 2>&1; do
  echo "Đang chờ SQL Server sẵn sàng..."
  sleep 2
done

"$SQLCMD" -S "$SERVER" -U "$SA_USER" -P "$MSSQL_SA_PASSWORD" -C -b \
  -Q "IF DB_ID(N'$DB_NAME') IS NULL CREATE DATABASE [$DB_NAME];"

APP_TABLE_COUNT=$("$SQLCMD" -S "$SERVER" -U "$SA_USER" -P "$MSSQL_SA_PASSWORD" -C -b \
  -d "$DB_NAME" -h -1 -W -Q "SET NOCOUNT ON; SELECT COUNT(*) FROM sys.tables WHERE name <> 'flyway_schema_history';" | tr -d '[:space:]')
HISTORY_TABLE_COUNT=$("$SQLCMD" -S "$SERVER" -U "$SA_USER" -P "$MSSQL_SA_PASSWORD" -C -b \
  -d "$DB_NAME" -h -1 -W -Q "SET NOCOUNT ON; SELECT COUNT(*) FROM sys.tables WHERE name = 'flyway_schema_history';" | tr -d '[:space:]')
if [ "$HISTORY_TABLE_COUNT" = "0" ]; then
  if [ "$APP_TABLE_COUNT" != "0" ] && [ "$APP_TABLE_COUNT" != "16" ]; then
    echo "Database chưa có Flyway history nhưng đang có $APP_TABLE_COUNT bảng ứng dụng; kỳ vọng 0 hoặc legacy 16 bảng. Dừng để tránh baseline nhầm schema."
    exit 1
  fi

  if [ "$APP_TABLE_COUNT" = "0" ]; then
    echo "Database rỗng đã sẵn sàng; Flyway sẽ áp dụng V1/V2 khi app khởi động."
  else
    echo "Schema legacy 16 bảng đã tồn tại; local/demo Compose cho phép Flyway baseline version 1 rồi áp dụng migration còn thiếu."
  fi
else
  echo "Flyway history đã tồn tại; bỏ qua guard số bảng cố định và để Flyway/Hibernate validate trạng thái schema."
fi

echo "SQL Server đã sẵn sàng cho Flyway."
