# Diseño

Las dos apps usan la misma paleta y la misma fuente. Manrope (SIL Open Font License) está en `ios/PresionMTB/Fonts` y en `android/app/src/main/res/font`. La licencia está en `third_party/manrope/OFL.txt`. El archivo empaquetado es el recorte latino de Fontsource, suficiente para el español.

## Color

| Uso | Oscuro | Claro |
| --- | --- | --- |
| Fondo | `#0E1110` | `#F3F5EF` |
| Superficie | `#171C1A` | `#FFFFFF` |
| Superficie alta | `#222826` | `#E7EBE3` |
| Texto | `#F4F7F2` | `#141816` |
| Texto secundario | `#B7C0B6` | `#5C675F` |
| Acento | `#C6F54A` | `#4E6B12` |
| Texto sobre acento lima | `#1A2208` | `#1A2208` |
| Línea | `#3A433C` | `#D5DBD0` |

El botón principal siempre lleva fondo `#C6F54A` y texto `#1A2208`. En oscuro el número de presión es lima. En claro es tinta, con una barra lima, para que se lea sobre blanco.

El icono de lanzamiento es un anillo lima sobre fondo `#0E1110` en los dos sistemas.

## Tipo

Manrope en regular, medium, semibold y bold. La presión recomendada va muy grande. La unidad preferida del perfil (psi o bar) es el número principal y la otra unidad queda debajo.

## Estructura

Cuatro secciones: Calcular, Bicis, Historial y Perfil. Controles segmentados para modalidad, terreno, montaje, carcasa, medida de rueda y unidad. Tarjetas separadas para la rueda delantera y la trasera.

El radio de las tarjetas es 20 y el de los botones 14.
