$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "../..")
& docker compose version | Out-Null
if ($LASTEXITCODE -ne 0) { throw "Instale Docker Desktop com Docker Compose." }
& docker info | Out-Null
if ($LASTEXITCODE -ne 0) { throw "Abra o Docker Desktop e aguarde iniciar." }
New-Item -ItemType Directory -Force -Path .local | Out-Null
if (-not (Test-Path .local/dev.env)) {
    function New-LocalPassword {
        $bytes = New-Object byte[] 24
        $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
        try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
        return ([BitConverter]::ToString($bytes)).Replace("-", "").ToLowerInvariant()
    }
    $dbPassword = New-LocalPassword
    $adminPassword = New-LocalPassword
    $configuration = "DATABASE_PASSWORD=$dbPassword`nADMIN_USERNAME=local-admin`nADMIN_PASSWORD=$adminPassword`nSAFE_PRODUCTION=false`nCOOKIE_SECURE=false`n"
    [System.IO.File]::WriteAllText((Join-Path (Get-Location) ".local/dev.env"), $configuration)
    Remove-Variable dbPassword, adminPassword, configuration
}
& docker compose -p safe-system-local --env-file .local/dev.env -f compose.local.yml up --build -d --wait --wait-timeout 180
if ($LASTEXITCODE -ne 0) { throw "O sistema nao iniciou. Confira o erro acima." }
$port = if ($env:SAFE_LOCAL_PORT) { $env:SAFE_LOCAL_PORT } else { "5173" }
Write-Host "SAFE System: http://localhost:$port/pt-BR"
Write-Host "Painel: http://localhost:$port/admin/login"
Write-Host "Credenciais: arquivo .local/dev.env (nao compartilhe)."
