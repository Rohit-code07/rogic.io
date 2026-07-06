param (
    [string]$TargetHost = "http://localhost:8080",
    [string]$CognitoToken = ""
)

# 1. Check if k6 is installed
$k6Check = Get-Command k6 -ErrorAction SilentlyContinue
if (-not $k6Check) {
    Write-Host "[WARNING] k6 is not installed on this system." -ForegroundColor Yellow
    Write-Host "[INFO] Attempting to install k6 via winget..." -ForegroundColor Cyan
    
    winget install grafana.k6
    
    # Re-evaluate
    $k6Check = Get-Command k6 -ErrorAction SilentlyContinue
    if (-not $k6Check) {
        Write-Error "Failed to locate k6 after installation. Please restart your shell or install k6 manually from https://k6.io"
        exit 1
    }
}

Write-Host "[INFO] Starting k6 Stress Test..." -ForegroundColor Cyan
Write-Host "Target Host   : $TargetHost" -ForegroundColor Gray
if ($CognitoToken) {
    Write-Host "Cognito Token : [PRESENT]" -ForegroundColor Gray
} else {
    Write-Host "Cognito Token : [NOT PRESENT] - Skipping Authenticated Endpoints" -ForegroundColor Gray
}

# Set environment variables for the k6 execution session
$env:TARGET_HOST = $TargetHost
$env:K6_COGNITO_TOKEN = $CognitoToken

# Resolve script path
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ScenarioPath = Join-Path $ScriptDir "load_scenario.js"

# Execute load test
k6 run "$ScenarioPath"

# Clean env variables
Remove-Item env:TARGET_HOST -ErrorAction SilentlyContinue
Remove-Item env:K6_COGNITO_TOKEN -ErrorAction SilentlyContinue

Write-Host "[INFO] Stress test execution complete." -ForegroundColor Green
