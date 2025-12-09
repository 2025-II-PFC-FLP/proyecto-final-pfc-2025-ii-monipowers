# Informe 2 – Corrección y Validación – Riego Óptimo

## Introducción

Este informe describe la validación del proyecto Riego Óptimo, mostrando la corrección mediante inducción y evidencias de su implementación en Scala a través de pruebas unitarias.

El objetivo matemático es el mismo presentado en el Informe 1.

El problema aborda la programación óptima del riego en una finca con tablones, relevante por el equilibrio entre calidad del cultivo y costos operativos. Validamos que las funciones (`tIR`, costos y generación de programaciones) cumplen la especificación mediante argumentos formales y pruebas representativas.

> Referencia: ver formalización completa en `Informe 1 – Proceso Funcional`, sección "Enunciado y formalización".

## Correspondencia función ↔ fórmula (implementación vs. especificación)

| Función Scala | Fórmula/Definición matemática |
|---|---|
| `tIR(f, \Pi)` | $t^{\Pi}_{\pi_0}=0$; $t^{\Pi}_{\pi_j}=t^{\Pi}_{\pi_{j-1}}+tr_{\pi_{j-1}}$ |
| `costoRiegoTablon(i,f,\Pi)` | $CR^{\Pi}_F[i]=\begin{cases} ts_i-(t^{\Pi}_i+tr_i), & \text{si } ts_i-tr_i\ge t^{\Pi}_i \\ p_i\big((t^{\Pi}_i+tr_i)-ts_i\big), & \text{de lo contrario} \end{cases}$ |
| `costoRiegoFinca(f,\Pi)` | $CR^{\Pi}_F=\sum_{i=0}^{n-1} CR^{\Pi}_F[i]$ |
| `costoMovilidad(f,\Pi,d)` | $CM^{\Pi}_F=\sum_{j=0}^{n-2} DF[\pi_j,\pi_{j+1}]$ |
| `generarProgramacionesRiego(f)` | Todas las permutaciones $\Pi$ de $\{0,\dots,n-1\}$ |
| `ProgramacionRiegoOptimo(f,d)` | $\arg\min_{\Pi}\big( CR^{\Pi}_F + CM^{\Pi}_F \big)$ |

## Argumentación de corrección

- Corrección de `tIR` (inducción sobre $j$):
  - Base: para $j=0$, $t^{\Pi}_{\pi_0}=0$ por definición; coincide con el primer valor acumulado de `foldLeft` (acum=0).
  - Hipótesis inductiva: suponga que para algún $k\ge 0$ se cumple $t^{\Pi}_{\pi_k}=\sum_{m=0}^{k-1} tr_{\pi_m}$.
  - Paso inductivo: para $k+1$, el acumulador de `foldLeft` suma $tr_{\pi_k}$, de modo que $t^{\Pi}_{\pi_{k+1}}=t^{\Pi}_{\pi_k}+tr_{\pi_k}=\sum_{m=0}^{k} tr_{\pi_m}$. Conclusión: por inducción, la implementación replica la recurrencia.

- Costos de riego y movilidad: `costoRiegoTablon`, `costoRiegoFinca` y `costoMovilidad` traducen las fórmulas; las sumas sobre índices corresponden a la agregación total definida.

- `generarProgramacionesRiego`: por inducción sobre el tamaño, insertar cabeza en todas las posiciones de cada permutación del resto genera todas las permutaciones únicas de tamaño $k+1$.
  - Base: para lista vacía, `permutar([]) = [[]]` (una única permutación de tamaño $0$).
  - Hipótesis: suponga que `permutar(t)` genera todas las listas de longitud $k$ sin duplicados.
  - Paso: para longitud $k+1$, insertar la cabeza en todas las posiciones de cada lista de longitud $k$ produce exactamente todas las permutaciones de longitud $k+1$; no hay duplicados porque cada inserción ocupa una posición distinta.

- `ProgramacionRiegoOptimo`: evalúa todas las $n!$ programaciones y retorna $\arg\min_{\Pi}(CR^{\Pi}_F+CM^{\Pi}_F)$; corrección por búsqueda exhaustiva del mínimo.

## Casos de prueba (representativos)

| Función | Entrada | Esperado |
|---|---|---|
| `tIR` | Finca pequeña con $\Pi=[0,1,2]$ | $t=[0, tr_0, tr_0+tr_1]$ |
| `costoRiegoTablon` | Caso con $ts_i - tr_i \ge t^{\Pi}_i$ | $ts_i - (t^{\Pi}_i+tr_i)$ |
| `costoRiegoTablon` | Caso contrario | $p_i\big((t^{\Pi}_i+tr_i)-ts_i\big)$ |
| `costoMovilidad` | $\Pi$ conocida, $DF$ simétrica | $\sum DF[\pi_j,\pi_{j+1}]$ |
| `generarProgramacionesRiego` | $n=3$ | $6$ permutaciones únicas |

### Fragmentos de código relevantes (evidencia)

```scala
def costoRiegoTablon(i: Int, f: Finca, pi: ProgRiego): Int = {
  val tiempoInicio = tIR(f, pi)(i)
  val tiempoFinal  = tiempoInicio + treg(f, i)

  if (tsup(f, i) - treg(f, i) >= tiempoInicio) {
    tsup(f, i) - tiempoFinal
  } else {
    prio(f, i) * (tiempoFinal - tsup(f, i))
  }
}
```

```scala
def ProgramacionRiegoOptimo(f: Finca, d: Distancia): (ProgRiego, Int) = {
  val todas = generarProgramacionesRiego(f)
  val conCostos = todas.map { pi =>
    val cr = costoRiegoFinca(f, pi)
    val cm = costoMovilidad(f, pi, d)
    (pi, cr + cm)
  }
  conCostos.minBy(_._2)
}
```

## Flujo conceptual de validación

```mermaid
flowchart TD
  A[Especificación matemática] --> B[Implementación funcional en Scala]
  B --> C[Pruebas unitarias (inputs con esperado)]
  C --> D[Comparación resultado vs esperado]
  D --> E{¿Coinciden?}
  E -- Sí --> F[Validez empírica]
  E -- No --> G[Revisión de función o caso]
```

Interpretación: el flujo muestra cómo se traduce cada definición matemática a una función concreta en Scala, y cómo las pruebas verifican la correspondencia resultado ↔ esperado. Es relevante porque garantiza trazabilidad entre especificación y código.

---

## Conclusión

La corrección se sustenta en inducción (para `tIR` y generación de permutaciones), traducción fiel de las fórmulas de costo y evidencia empírica mediante pruebas representativas. En conjunto, los resultados muestran concordancia entre la especificación matemática y las implementaciones funcionales en Scala.
El algoritmo es completo porque explora exhaustivamente las $n!$ programaciones y toma el mínimo de todos los costos calculados; por lo tanto, siempre encuentra la solución óptima.
