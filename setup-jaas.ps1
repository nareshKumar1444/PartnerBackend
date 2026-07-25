# Opens JaaS API Keys page and the public key file folder for upload.
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$keysDir = Join-Path $root "jaas-keys"
$pubKey = Join-Path $keysDir "jwt.key.pub.pem"

Write-Host "App ID: vpaas-magic-cookie-d315eabb12ff4c7e855951f7b571ae6d"
Write-Host "Upload this file to JaaS API Keys:" -ForegroundColor Cyan
Write-Host "  $pubKey"
Write-Host ""
Write-Host "After upload, copy Key ID (kid) and save to:" -ForegroundColor Yellow
Write-Host "  $keysDir\kid.txt"
Write-Host ""

Start-Process "https://jaas.8x8.vc/#/apikeys"
Start-Process explorer.exe $keysDir

if (Test-Path $pubKey) {
  Get-Content $pubKey -Raw | Set-Clipboard
  Write-Host "Public key copied to clipboard - paste in JaaS Add API Key field." -ForegroundColor Green
}
