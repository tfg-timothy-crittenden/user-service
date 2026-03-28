param(
  [string]$DbHost = "localhost",
  [int]$Port = 5433,
  [string]$Database = "user_db",
  [string]$Username = "myuser",
  [string]$Password = "secret"
)

$ErrorActionPreference = "Stop"

Write-Host "Resetting user-service dev DB schema (drops public schema) on ${DbHost}:$Port/$Database ..."

# Prefer dockerized psql (works out of the box if Docker is installed)
$psqlCmd = @(
  "docker", "run", "--rm",
  "-e", "PGPASSWORD=$Password",
  "postgres:16",
  "psql",
  "-h", $DbHost,
  "-p", $Port,
  "-U", $Username,
  "-d", $Database,
  "-v", "ON_ERROR_STOP=1",
  "-c", "DROP SCHEMA IF EXISTS public CASCADE; CREATE SCHEMA public;"
)

& $psqlCmd[0] $psqlCmd[1..($psqlCmd.Length-1)]

Write-Host "Done. Next app start will re-apply Flyway migrations (baseline + repeatables)."
