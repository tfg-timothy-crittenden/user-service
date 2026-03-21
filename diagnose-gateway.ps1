# Gateway Configuration Diagnosis Script
# This helps identify how the gateway is configured and what patterns work

Write-Host "`n=======================================" -ForegroundColor Cyan
Write-Host "Gateway Configuration Diagnosis" -ForegroundColor Cyan
Write-Host "=======================================`n" -ForegroundColor Cyan

# Step 1: Check if gateway is running
Write-Host "1. Checking Gateway Status..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -ErrorAction Stop
    Write-Host "   Gateway Status: $($health.status)" -ForegroundColor Green
} catch {
    Write-Host "   Gateway is NOT accessible on port 8080" -ForegroundColor Red
    exit
}

# Step 2: Check Eureka registration
Write-Host "`n2. Checking Eureka Service Registry..." -ForegroundColor Yellow
try {
    $eureka = Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps" -Headers @{Accept="application/json"} -ErrorAction Stop
    $services = $eureka.applications.application | ForEach-Object { $_.name }
    Write-Host "   Registered Services:" -ForegroundColor Green
    $services | ForEach-Object { Write-Host "     - $_" -ForegroundColor White }
} catch {
    Write-Host "   Cannot access Eureka at localhost:8761" -ForegroundColor Red
}

# Step 3: Test direct service access
Write-Host "`n3. Testing Direct Service Access (Port 8081)..." -ForegroundColor Yellow
try {
    $directTest = Invoke-RestMethod -Uri "http://localhost:8081/api/tests/1" -ErrorAction Stop
    Write-Host "   Direct Access: WORKING" -ForegroundColor Green
    Write-Host "   Response: $($directTest | ConvertTo-Json -Compress)" -ForegroundColor White
} catch {
    Write-Host "   Direct Access: FAILED" -ForegroundColor Red
}

# Step 4: Test various gateway patterns
Write-Host "`n4. Testing Gateway Route Patterns..." -ForegroundColor Yellow

$patterns = @(
    "http://localhost:8080/api/tests/1",
    "http://localhost:8080/toefl-speaking-service/api/tests/1",
    "http://localhost:8080/TOEFL-SPEAKING-SERVICE/api/tests/1",
    "http://localhost:8080/services/toefl-speaking-service/api/tests/1",
    "http://localhost:8080/toefl-speaking/api/tests/1",
    "http://localhost:8080/speaking/api/tests/1"
)

$workingPattern = $null
foreach ($pattern in $patterns) {
    $shortPattern = $pattern -replace "http://localhost:8080/", "/"
    try {
        $result = Invoke-RestMethod -Uri $pattern -Method Get -ErrorAction Stop
        Write-Host "   SUCCESS: $shortPattern" -ForegroundColor Green
        $workingPattern = $pattern
        break
    } catch {
        Write-Host "   Failed:  $shortPattern (404)" -ForegroundColor Red
    }
}

# Step 5: Summary and recommendations
Write-Host "`n=======================================" -ForegroundColor Cyan
Write-Host "Summary & Recommendations" -ForegroundColor Cyan
Write-Host "=======================================`n" -ForegroundColor Cyan

if ($workingPattern) {
    Write-Host "Gateway Routing: WORKING" -ForegroundColor Green
    Write-Host "Working URL Pattern: $workingPattern`n" -ForegroundColor White

    Write-Host "Use this pattern for your API calls:" -ForegroundColor Yellow
    Write-Host "  GET:    $($workingPattern -replace '/1$', '/{id}')" -ForegroundColor White
    Write-Host "  POST:   $($workingPattern -replace '/api/tests/1$', '/api/tests/{name}')" -ForegroundColor White
    Write-Host "  PATCH:  $($workingPattern -replace '/1$', '/{id}')" -ForegroundColor White
    Write-Host "  DELETE: $($workingPattern -replace '/1$', '/{id}')" -ForegroundColor White
} else {
    Write-Host "Gateway Routing: NOT WORKING" -ForegroundColor Red
    Write-Host "`nPossible Issues:" -ForegroundColor Yellow
    Write-Host "  1. Gateway routes are not configured" -ForegroundColor White
    Write-Host "  2. Gateway was not restarted after configuration" -ForegroundColor White
    Write-Host "  3. Route predicates don't match the service name" -ForegroundColor White

    Write-Host "`nWhat to check in your gateway configuration:" -ForegroundColor Yellow
    Write-Host "  - Look for spring.cloud.gateway.routes in application.yml" -ForegroundColor White
    Write-Host "  - Verify the service name matches: TOEFL-SPEAKING-SERVICE" -ForegroundColor White
    Write-Host "  - Check the Path predicates" -ForegroundColor White
    Write-Host "  - Make sure gateway was restarted after changes" -ForegroundColor White

    Write-Host "`nFor now, use direct access:" -ForegroundColor Yellow
    Write-Host "  http://localhost:8081/api/tests/1" -ForegroundColor White
}

Write-Host "`n=======================================" -ForegroundColor Cyan

