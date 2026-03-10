# ============================================================
# start_tunnel.ps1 - Arranca el túnel Cloudflare para SLIOR
#
# Uso:
#   .\scripts\start_tunnel.ps1
#
# Requisito previo:
#   winget install --id Cloudflare.cloudflared
#
# Resultado:
#   Genera una URL pública HTTPS tipo:
#   https://algo-random.trycloudflare.com
#
# Después de obtener la URL:
#   1. Cópiala
#   2. En mobile-app/app/build.gradle.kts, cambia el buildConfigField
#      de la variante que uses (debug o release) por la nueva URL
#   3. Haz Sync Project en Android Studio y vuelve a instalar la app
# ============================================================

Write-Host ""
Write-Host "=== SLIOR - Cloudflare Tunnel ===" -ForegroundColor Cyan
Write-Host "Asegurate de que el backend esta corriendo en el puerto 8080" -ForegroundColor Yellow
Write-Host ""

# Comprobar que cloudflared está instalado
if (-not (Get-Command cloudflared -ErrorAction SilentlyContinue)) {
    Write-Host "ERROR: cloudflared no esta instalado." -ForegroundColor Red
    Write-Host "Ejecuta: winget install --id Cloudflare.cloudflared" -ForegroundColor Yellow
    exit 1
}

Write-Host "Iniciando tunel hacia http://localhost:8080 ..." -ForegroundColor Green
Write-Host "La URL publica aparecera en unos segundos. Copia la linea que empiece por 'https://'" -ForegroundColor Green
Write-Host "Pulsa Ctrl+C para detener el tunel." -ForegroundColor Gray
Write-Host ""

cloudflared tunnel --url http://localhost:8080
