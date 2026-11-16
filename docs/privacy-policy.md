# Política de privacidad

Última actualización: 16 de noviembre de 2026.

Presión MTB calcula una presión de partida para cubiertas de mountain bike. La app no tiene cuenta ni servidor propio.

## Datos que se quedan en el teléfono

- Perfil: nombre opcional, peso, estatura, edad, temperatura habitual del garaje y unidad preferida (psi o bar).
- Bicicletas: nombre, medida de rueda, peso y el montaje de cada rueda.
- Historial: lugar, temperatura, presiones recomendadas y usadas, y la fecha.

Esos datos viven en el dispositivo. Puedes exportarlos a un archivo JSON e importarlos en otro teléfono. Borrar una bici o una salida la oculta y deja una marca de tiempo para poder sincronizar más adelante. Hoy esa marca no se envía a ningún servidor.

El peso entra en el cálculo. La estatura y la edad solo se guardan en el perfil.

## Ubicación y clima

Si tocas «Usar mi ubicación», la app pide permiso de ubicación mientras está en uso. No hay seguimiento en segundo plano.

Con esa posición ocurre lo siguiente:

- El geocodificador del sistema devuelve el nombre del lugar.
- La latitud y la longitud se envían a [Open-Meteo](https://open-meteo.com) para obtener la temperatura actual. Open-Meteo no exige una clave. Su propio tratamiento de la consulta está en su política de privacidad.

Si guardas la salida, el lugar, la temperatura y, si existían, las coordenadas quedan en el historial del teléfono. Puedes escribir la temperatura y el lugar a mano. La calculadora funciona sin ubicación y sin conexión.

No hay publicidad, no hay seguimiento entre apps y no vendemos datos.

## Copias del sistema

Android e iOS pueden incluir los datos de la app en la copia de seguridad del teléfono, protegida por la cuenta del sistema.

## Edad

La app no está dirigida a menores de 13 años. La edad del perfil es opcional, la escribes tú y no sale del teléfono.

## Contacto

Para una consulta de privacidad, abre un issue en [github.com/kzeta/mtb-calc](https://github.com/kzeta/mtb-calc/issues).
