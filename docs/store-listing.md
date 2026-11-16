# Ficha de las tiendas

Nombre visible: **Presión MTB**

Identificadores: `com.kzeta.presionmtb`

Categoría: Deportes. Clasificación: para todo público / 4+. Sin compras, sin publicidad, sin cuenta.

URL de privacidad, con GitHub Pages desde la carpeta `/docs`:

`https://kzeta.github.io/mtb-calc/`

## Textos

Descripción breve (menos de 80 caracteres):

Presión de cubiertas MTB en psi y bar, según peso, bici y clima.

Descripción completa:

Presión MTB propone una presión de partida para la rueda delantera y la trasera, en psi y en bar.

Tiene en cuenta el peso de hoy, el de la bici, el ancho de cada cubierta, el ancho interno de la llanta, si ruedas con cámara, tubeless o inserto, la carcasa, la modalidad y el terreno. Si lo pides, lee la temperatura del lugar y te dice qué marcar en la bomba de casa para llegar a esa presión en el sendero.

Puedes guardar tus bicis, tu perfil (peso, estatura y edad) y el historial de cada salida: lugar, temperatura y la presión que terminaste usando.

Los datos se quedan en el teléfono. Puedes exportarlos e importarlos. La cifra es un punto de partida: afínala de a medio psi y respeta el máximo de la cubierta y de la llanta.

## Data safety (Play Console)

- Ubicación precisa: se recoge cuando la persona pide el clima. Se envía a Open-Meteo para obtener la temperatura. No se vende y no se usa para seguimiento. Si se guarda la salida, las coordenadas quedan solo en el teléfono.
- Nombre, peso, estatura y edad: opcionales, escritos por la persona, solo en el dispositivo, sin compartir.
- No hay datos de diagnóstico enviados por la app.
- Cifrado en tránsito: la consulta a Open-Meteo va por HTTPS.
- Se puede pedir el borrado borrando el historial, las bicis y los datos de la app en el sistema.

## App Privacy (App Store Connect)

- Datos usados para rastrearte: no.
- Ubicación precisa: sí, vinculada a la funcionalidad de la app, no vinculada a la identidad del usuario, no usada para rastreo.
- Nombre y otros datos del perfil: no salen del dispositivo. Si el formulario pregunta solo por datos que salen del teléfono, declara la ubicación. La política pública describe también lo que se guarda en local.
- Tracking: no.

## Capturas

Hacen falta capturas del teléfono en los tamaños que pide cada tienda. El gráfico de funciones de Play (1024×500) y el icono de 512 px están en `store/` después de generarlos. El icono de iOS de 1024 px está en el catálogo de la app.
