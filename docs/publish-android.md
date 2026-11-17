# Publicar en Google Play

Requisitos en este equipo: el Android SDK ya está en `%LOCALAPPDATA%\Android\Sdk`. Gradle tiene que arrancar con un JDK 17 o 21 (`JAVA_HOME`). La primera vez descarga otro JDK 17 para compilar. Un JDK 25 no arranca Gradle 8.11. La cuenta de Play Console tiene que ser la del desarrollador.

Identificador: `com.kzeta.presionmtb`. No se puede cambiar después de crear la ficha.

Versión de esta entrega: `versionName` 1.0.0, `versionCode` 2. `targetSdk` 36. `minSdk` 26. Play ya recibió el código 1, así que el siguiente AAB tiene que usar un código mayor.

## Compilar

Desde `android/`:

```
gradlew.bat :app:testDebugUnitTest
gradlew.bat :app:assembleRelease
```

`android/local.properties` apunta al SDK y no se sube al repositorio.

Si no existe `android/keystore.properties`, el AAB de release se firma con la clave de depuración para poder probar el build. Ese archivo no sirve para Play.

## Clave de subida

Play App Signing guarda la clave de firma de la app. Tú conservas una clave de subida. Generala una vez y guarda copias fuera de este repositorio:

```
powershell -File android/scripts/new-upload-keystore.ps1
```

Eso crea `%USERPROFILE%\presion-mtb-upload.jks`. Copia `android/keystore.properties.example` a `android/keystore.properties`, completa las contraseñas y la ruta del archivo. Ese properties también queda fuera de git.

Vuelve a ejecutar `gradlew.bat :app:bundleRelease`. El AAB queda en `android/app/build/outputs/bundle/release/`.

Si pierdes la clave de subida, Play puede pedir un restablecimiento. No hay forma de reconstruirla desde el código.

## Ficha

1. Crea la app en Play Console con el identificador `com.kzeta.presionmtb`.
2. Activa Play App Signing.
3. Sube el AAB a una prueba interna.
4. Completa la ficha con los textos de [store-listing.md](store-listing.md).
5. Publica la política en GitHub Pages (carpeta `/docs` de la rama principal) y pega `https://kzeta.github.io/mtb-calc/`.
6. Responde Data safety como está descrito en la ficha. El permiso de ubicación es solo en primer plano.
7. Clasificación de contenido: utilidad, sin violencia ni compras.
8. Añade capturas de teléfono. El icono de 512 px está en `store/play-icon-512.png` y el gráfico de 1024×500 en `store/feature-graphic.png`.

La primera subida a producción pasa revisión de Google. Una prueba interna sirve para instalarla antes.
