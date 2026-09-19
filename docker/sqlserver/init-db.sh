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

TABLE_COUNT=$("$SQLCMD" -S "$SERVER" -U "$SA_USER" -P "$MSSQL_SA_PASSWORD" -C -b \
  -d "$DB_NAME" -h -1 -W -Q "SET NOCOUNT ON; SELECT COUNT(*) FROM sys.tables;" | tr -d '[:space:]')

if [ "$TABLE_COUNT" = "0" ]; then
  echo "Áp dụng baseline schema MedClinic..."
  "$SQLCMD" -S "$SERVER" -U "$SA_USER" -P "$MSSQL_SA_PASSWORD" -C -b \
    -d "$DB_NAME" -i /docker/sql/SQLSERVER_BASELINE_SCHEMA_2026-09-19.sql
elif [ "$TABLE_COUNT" != "16" ]; then
  echo "Database hiện có $TABLE_COUNT bảng; kỳ vọng 0 hoặc 16. Dừng để tránh ghi đè schema ngoài dự kiến."
  exit 1
else
  echo "Schema 16 bảng đã tồn tại; bỏ qua baseline."
fi

echo "Đồng bộ reference data hệ thống..."
"$SQLCMD" -S "$SERVER" -U "$SA_USER" -P "$MSSQL_SA_PASSWORD" -C -b \
  -d "$DB_NAME" -i /docker/sql/SQLSERVER_REFERENCE_DATA_2026-09-19.sql

echo "SQL Server demo đã sẵn sàng."
