# Publicar en el App Store

El proyecto está en `ios/PresionMTB.xcodeproj`. Hay que abrirlo en un Mac con Xcode 16 o posterior. Este repositorio se puede editar en Windows, pero el IPA solo se genera en macOS.

- Bundle ID: `com.kzeta.presionmtb`
- Versión de marketing: 1.0.0
- Build: 1 (`CFBundleVersion` / `CURRENT_PROJECT_VERSION`)
- Destino mínimo: iOS 17
- Dispositivos: iPhone y iPad, vertical
- Cifrado: exento (`ITSAppUsesNonExemptEncryption` = no). Solo HTTPS estándar.
- Categoría: Deportes

## En el Mac

1. Abre el proyecto y elige el scheme **PresionMTB**.
2. En Signing & Capabilities, elige tu equipo de Apple Developer. El identificador tiene que existir en Certificates, Identifiers & Profiles, con Location When In Use.
3. El texto de ubicación ya está en `Info.plist`. El manifiesto de privacidad está en `PrivacyInfo.xcprivacy`.
4. Ejecuta los tests del scheme. Usan los mismos casos que Android.
5. Archive con un destino genérico de iOS y súbelo con Organizer, o usa el workflow de GitHub.

La política de privacidad es la misma URL que en Android: `https://kzeta.github.io/mtb-calc/`.

En App Privacy declara ubicación precisa para la función de la app, sin rastreo y sin vincularla a la identidad. Los detalles están en [store-listing.md](store-listing.md).

## GitHub Actions

`.github/workflows/ios-release.yml` se lanza a mano en un runner `macos-15`. Sin secretos, archiva el proyecto con la firma desactivada para comprobar que compila. Con el input de subida activado, espera estos secretos:

- `IOS_DIST_CERT_P12_BASE64`: certificado Apple Distribution en .p12, en base64
- `IOS_DIST_CERT_PASSWORD`
- `IOS_PROVISION_PROFILE_BASE64`: perfil de App Store para `com.kzeta.presionmtb`
- `ASC_KEY_ID`, `ASC_ISSUER_ID`, `ASC_KEY_P8`: clave de API de App Store Connect (el .p8 completo)
- `IOS_TEAM_ID`: el Team ID de la cuenta de Apple Developer

El perfil y el certificado salen de la cuenta de desarrollador. La clave de API se crea en App Store Connect → Users and Access → Integrations. No las pongas en el repositorio.

El workflow exporta el IPA y, si la subida está activada, lo envía con `xcrun altool`. La ficha, las capturas y la revisión se completan en App Store Connect.
