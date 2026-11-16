# Creates an upload keystore outside the repository.
# Back it up. Play App Signing cannot rebuild it from this source tree.

$ErrorActionPreference = "Stop"
$dest = Join-Path $env:USERPROFILE "presion-mtb-upload.jks"
if (Test-Path $dest) {
    Write-Host "Ya existe $dest. No lo reemplazo."
    exit 1
}

$keytool = Get-ChildItem "$env:USERPROFILE\.gradle\jdks" -Recurse -Filter keytool.exe -ErrorAction SilentlyContinue |
    Select-Object -First 1 -ExpandProperty FullName
if (-not $keytool) {
    $cmd = Get-Command keytool -ErrorAction SilentlyContinue
    if ($cmd) { $keytool = $cmd.Source }
}
if (-not $keytool) {
    Write-Host "No encontré keytool. Abre una terminal de Android Studio, o compila el proyecto una vez para que Gradle descargue el JDK, y vuelve a ejecutar este script."
    exit 1
}

& $keytool -genkeypair -v `
    -keystore $dest `
    -alias upload `
    -keyalg RSA `
    -keysize 2048 `
    -validity 10000

Write-Host ""
Write-Host "Keystore: $dest"
Write-Host "Copia android/keystore.properties.example a android/keystore.properties y completa las contraseñas."
