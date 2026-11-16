# Fórmula de presión

Punto de partida para cubiertas de MTB. No es una presión definitiva: el terreno, la carcasa real y el manómetro pueden pedir un ajuste de unos 0,5 psi. Respeta siempre el máximo marcado en la cubierta y en la llanta.

La misma función está en Android (`PressureCalculator.kt`), iOS (`PressureCalculator.swift`) y el oráculo [`shared/fixtures/generate.mjs`](../shared/fixtures/generate.mjs). Los casos de [`shared/fixtures/pressure-cases.json`](../shared/fixtures/pressure-cases.json) tienen que coincidir dentro de 0,0001 psi.

## Referencia

Ciclista de 75 kg, bici de 12 kg, cubierta 2,40", llanta interna de 30 mm, tubeless, carcasa trail, modalidad trail, terreno mixto, 20 °C:

- Delantera: **21,5 psi**
- Trasera: **24,0 psi**

La delantera lleva el 42 % de la carga y la trasera el 58 %. Como las dos crecen con el peso total, la presión de cada rueda escala con el peso del conjunto respecto de 87 kg:

```
base = psiReferencia * (pesoCiclista + pesoBici) / 87
```

`psiReferencia` es 21,5 delante y 24,0 detrás.

## Ajustes

Sobre esa base, en este orden:

```
psi = base * (2,40 / anchoPulgadas) ^ 1,4
    + ajusteLlanta
    + ajusteMontaje
    + ajusteModalidad
    + ajusteCarcasa
    + ajusteTerreno
```

Llanta, respecto de 30 mm internos:

- Por debajo de 30: `+0,15 psi` por cada mm.
- Por encima de 30: `−0,08 psi` por cada mm.

Montaje:

| Montaje | psi |
| --- | --- |
| Cámara | +4 |
| Tubeless | 0 |
| Tubeless con inserto | −2,5 |

Modalidad, respecto de trail:

| Modalidad | psi |
| --- | --- |
| XC | +2 |
| Maratón | +1,5 |
| Trail | 0 |
| Enduro | −1,5 |
| Descenso | −1 |

Carcasa:

| Carcasa | psi |
| --- | --- |
| XC ligera | +1,5 |
| Trail | 0 |
| Enduro reforzada | −1 |
| Doble capa | −1,5 |

Terreno:

| Terreno | psi |
| --- | --- |
| Blando | −1,5 |
| Mixto | 0 |
| Seco | +0,5 |
| Roca y raíces | +1,5 |

## Límites

Después de los ajustes:

1. Tubeless e inserto quedan entre 14 y 40 psi. Cámara, entre 18 y 45 psi.
2. Si la cubierta tiene un máximo cargado por el usuario, el resultado no lo supera. Ese tope puede quedar por debajo del mínimo del montaje: manda la cubierta.

## Temperatura

La cifra principal es la presión que hay que poner **ahora**, a la temperatura del lugar. La fórmula de peso no cambia con el clima.

Si inflas en casa y sales a otra temperatura, la presión manométrica sigue la ley de los gases. La presión absoluta es la manométrica más 14,696 psi. Las temperaturas van en kelvin (`°C + 273,15`):

```
psiCasa = (psiSendero + 14,696) * (kelvinCasa / kelvinSendero) - 14,696
```

Si el sendero está más frío que el garaje, en casa hay que poner más presión para llegar al valor del sendero.

## Presentación

Los valores guardados son psi sin redondear. En pantalla:

- psi al medio psi más cercano (`floor(psi * 2 + 0,5) / 2`, medio hacia arriba en positivos)
- bar = psi / 14,503773773, al 0,05 bar más cercano con el mismo redondeo

El rango mostrado es la recomendación redondeada ± 1 psi, sin salir de los límites de esa rueda.

1 bar de esta app son exactamente 14,503773773 psi.
