# ==============================================================================
# SLIOR Setup Script - Generación de Secretos
# ==============================================================================

$envFilePath = Join-Path $PSScriptRoot "contenedores/.env"

if (-not (Test-Path $envFilePath)) {
    Write-Host "Generando archivo .env con secreto JWT seguro..." -ForegroundColor Cyan
    
    # Generar un secreto de 256 bits (32 bytes) codificado en Base64
    $bytes = New-Object Byte[] 32
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    $rng.GetBytes($bytes)
    $secret = [Convert]::ToBase64String($bytes)
    
    $envContent = @"
# Secreto generado automáticamente para SLIOR
# No compartir este archivo ni subirlo al repositorio
JWT_SECRET=$secret
"@
    Set-Content -Path $envFilePath -Value $envContent
    Write-Host "Archivo .env creado en: $envFilePath" -ForegroundColor Green
} else {
    Write-Host "El archivo .env ya existe. Saltando generación." -ForegroundColor Yellow
}

Write-Host "`nListo. Ahora puedes ejecutar: docker compose up -d --build" -ForegroundColor Cyan
