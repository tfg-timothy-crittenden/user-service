# Reset the dev database by dropping and recreating schema `public`.
# This removes the Flyway schema history and all tables.
#
# Requirements:
# - Docker running
# - docker compose up for the DB

$ErrorActionPreference = 'Stop'

$composeFile = Join-Path $PSScriptRoot '..\compose.yaml'

Write-Host "Finding Postgres container from compose file: $composeFile" -ForegroundColor Cyan

# Get container id for service named 'postgres' (common). Fallback to first container that exposes 5433.
$cid = ''
try {
  $cid = (docker compose -f $composeFile ps -q postgres 2>$null).Trim()
} catch {}

if (-not $cid) {
  $cid = (docker ps --filter "publish=5433" -q).Trim()
}

if (-not $cid) {
  throw "Couldn't find a running Postgres container. Start it with: docker compose -f `"$composeFile`" up -d"
}

Write-Host "Using container: $cid" -ForegroundColor Cyan

# These defaults match application(-dev).properties in this repo.
$dbUser = $env:DB_USER; if (-not $dbUser) { $dbUser = 'myuser' }
$dbName = $env:DB_NAME; if (-not $dbName) { $dbName = 'user_db' }

$sql = @"
DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO $dbUser;
GRANT ALL ON SCHEMA public TO public;
"@

Write-Host "Dropping and recreating schema public in database '$dbName'..." -ForegroundColor Yellow

docker exec -i $cid psql -U $dbUser -d $dbName -v ON_ERROR_STOP=1 -c $sql | cat

Write-Host "Done. Next app startup will rebuild via Flyway baseline." -ForegroundColor Green

