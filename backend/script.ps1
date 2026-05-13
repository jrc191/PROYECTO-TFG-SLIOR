$ErrorActionPreference = "Stop"

$base = "http://localhost:8080"
$email = "rgpd_test_$(Get-Date -Format yyyyMMddHHmmss)@slior.com"
$pass  = "Secreto123"

Write-Host "Usuario de prueba: $email"

$regBody = @{
  nombre = "Prueba RGPD"
  email = $email
  password = $pass
  rol = "REPARTIDOR"
  vehicleType = "VAN"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "$base/auth/v1/register" -ContentType "application/json" -Body $regBody | Out-Null
Write-Host "Registro OK"

$loginBody = @{
  email = $email
  password = $pass
} | ConvertTo-Json

$login = Invoke-RestMethod -Method Post -Uri "$base/auth/v1/login" -ContentType "application/json" -Body $loginBody
$token = $login.token

if ([string]::IsNullOrWhiteSpace($token)) {
  throw "Login sin token JWT"
}

Write-Host "Login OK"

$export = Invoke-RestMethod -Method Get -Uri "$base/api/v1/users/me/data-export" -Headers @{ Authorization = "Bearer $token" }
$export | ConvertTo-Json -Depth 10

Write-Host ""
Write-Host "Comprueba auditoria con:"
Write-Host "docker exec slior-db psql -U slior -d slior -c ""SELECT id, action, entity, timestamp FROM audit_log ORDER BY id DESC LIMIT 20;"""

Read-Host "Pulsa Enter para salir"
