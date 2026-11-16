# Presión MTB

Calculadora de presión para cubiertas de mountain bike, en dos apps nativas que comparten la fórmula y el formato de los datos.

- Android: Kotlin y Jetpack Compose, en [`android/`](android/).
- iOS: Swift y SwiftUI, en [`ios/`](ios/).

La cifra es un punto de partida en psi y bar, para la rueda delantera y la trasera. Entran el peso, la bici, el ancho de cubierta, la llanta, el montaje, la carcasa, la modalidad y el terreno. Con la ubicación se lee la temperatura del día en Open-Meteo y se calcula qué presión poner en casa.

El perfil guarda peso, estatura y edad. Las bicis y el historial (lugar, temperatura y presión usada) viven en el teléfono. Cada registro lleva identificador y fechas para poder sincronizar más adelante. Hoy se pueden exportar e importar en el JSON descrito en [`docs/sync-schema.json`](docs/sync-schema.json).

## Fórmula

Está escrita en [`docs/formula.md`](docs/formula.md). El oráculo es [`shared/fixtures/generate.mjs`](shared/fixtures/generate.mjs) y los casos, [`shared/fixtures/pressure-cases.json`](shared/fixtures/pressure-cases.json).

Para regenerar los casos:

```
node shared/fixtures/generate.mjs
```

## Android

Hace falta el Android SDK y un JDK 17 o 21 en `JAVA_HOME` para arrancar Gradle. La primera ejecución descarga el JDK 17 con el que compila. Un JDK 25 no sirve para esta versión de Gradle.

```
cd android
gradlew.bat :app:testDebugUnitTest
gradlew.bat :app:assembleDebug
```

Crea `android/local.properties` con `sdk.dir` apuntando al SDK. Cómo firmar y subir a Play: [`docs/publish-android.md`](docs/publish-android.md).

## iOS

Abre `ios/PresionMTB.xcodeproj` en un Mac con Xcode 16 o posterior. El destino mínimo es iOS 17. Cómo firmar y subir al App Store: [`docs/publish-ios.md`](docs/publish-ios.md).

## Privacidad

Los datos de perfil, bicis e historial no salen del teléfono. La ubicación solo se usa si pides la temperatura, y en ese caso las coordenadas se envían a Open-Meteo. Texto para las tiendas: [`docs/privacy-policy.md`](docs/privacy-policy.md). La página de GitHub Pages es [`docs/index.html`](docs/index.html).

Identificador de las dos apps: `com.kzeta.presionmtb`.

Manrope se usa bajo la SIL Open Font License. Ver [`third_party/manrope/OFL.txt`](third_party/manrope/OFL.txt).
