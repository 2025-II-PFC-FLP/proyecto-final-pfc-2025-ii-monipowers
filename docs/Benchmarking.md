# Benchmarking – Riego Óptimo

## Metodología

- Entorno: ejecución local en la misma máquina del proyecto.
- Entradas: fincas y matrices de distancia generadas por utilidades del proyecto (aleatorias controladas).
- Métrica: tiempo de ejecución por función; aceleración `accel = (1 - par/seq) * 100`.
- Repeticiones: múltiples mediciones por tarea; se reporta un valor representativo.
- Observación: para tamaños pequeños el overhead de paralelismo domina.

## Resultados base (n = 5)

| Tarea | Secuencial (ms) | Paralelo (ms) |
|---|---:|---:|
| costoRiegoFinca | 0.2117 | 0.8165 |
| costoMovilidad | 0.0106 | 0.4998 |
| generarProgramacionesRiego | 0.3300 | 0.2601 |
| ProgramacionRiegoOptimo | 1.1351 | 1.9253 |

**Aceleración (%)**: \(\text{accel}=(1-\frac{par}{seq})\times 100\).

| Tarea | Secuencial (ms) | Paralelo (ms) | Aceleración (%) |
|---|---:|---:|---:|
| costoRiegoFinca | 0.2117 | 0.8165 | −285.6 |
| costoMovilidad | 0.0106 | 0.4998 | −4 612.3 |
| generarProgramacionesRiego | 0.3300 | 0.2601 | +21.2 |
| ProgramacionRiegoOptimo | 1.1351 | 1.9253 | −69.6 |

Interpretación: para n=5, la paralelización genera overhead y no mejora el rendimiento.

La ganancia real estuvo limitada por el overhead de creación y sincronización de tareas. Para arreglos pequeños, el costo de gestionar el paralelismo supera el ahorro de dividir el trabajo.

## Extensión sugerida

- Ampliar tamaños: `n = 10, 20, 30, 40, 50`.
- Esperado: a medida que crece `n`, el trabajo útil por tarea crece y el overhead relativo se reduce.

## Amdahl y relación con el código

- Ley de Amdahl: \( S = \frac{1}{(1-p) + \frac{p}{N}} \). Para mayor tamaño, el \(p\) efectivo aumenta.
- Código: `par.map` crea/coordina tareas; `minBy(_._2)` combina resultados preservando la optimalidad. Si el trabajo por elemento es pequeño, el overhead domina.

## Notas

- Los valores son representativos y orientativos para análisis; pueden variar según el entorno.
