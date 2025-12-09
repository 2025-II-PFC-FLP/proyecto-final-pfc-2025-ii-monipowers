# Informe 3 – Paralelización – Riego Óptimo

## Introducción

Este informe describe la paralelización del proyecto Riego Óptimo, mostrando el enfoque de datos y tareas y evidencias de su implementación en Scala mediante mediciones reales.

El objetivo matemático es el mismo presentado en el Informe 1.

El problema consiste en programar el riego óptimo equilibrando costos de sufrimiento y movilidad; es relevante por su impacto en la calidad y el costo operativo. Evaluamos paralelismo de datos y tareas para analizar si se logra acelerar la ejecución manteniendo la corrección y el estilo funcional.

## Estrategia de paralelización

- Paralelismo de datos con `scala.collection.parallel` (`.par`):
  - `costoRiegoFincaPar`: suma de costos de tablones en paralelo.
  - `costoMovilidadPar`: suma de distancias entre pares consecutivos en paralelo.
- Paralelismo de tareas en `ProgramacionRiegoOptimoPar`: cálculo de $(CR+CM)$ para cada programación en paralelo.

### Código paralelo (evidencia)

Mostramos solo las líneas relevantes; el resto de la función se mantiene igual.

```scala
// costoRiegoFincaPar: paraleliza la suma por tablón
(0 until f.length).par.map(i => costoRiegoTablon(i, f, pi)).sum

// costoMovilidadPar: paraleliza la suma de distancias consecutivas
(0 until pi.length - 1).par.map(j => d(pi(j))(pi(j + 1))).sum

// ProgramacionRiegoOptimoPar: evaluación en paralelo y combinación por mínimo
val conCostos = todas.par.map(pi => (pi, costoRiegoFincaPar(f, pi) + costoMovilidadPar(f, pi, d)))
conCostos.minBy(_._2) // preserva la optimalidad al combinar resultados
```

Transición: A continuación mostramos específicamente qué funciones fueron paralelizadas y por qué.

Qué partes se paralelizan y por qué:
- Datos: la suma de costos por tablón y de distancias por par consecutivo son independientes → se aplican `par.map(...).sum`.
- Tareas: la evaluación de $(CR+CM)$ por programación es independiente → se usa `todas.par.map`.
- Cuellos de botella: combinación del mínimo (`minBy`) y acceso a `Vector` implican sincronización mínima, pero el overhead de creación/gestión de tareas puede dominar para $n$ pequeños.

## Benchmarking

Resumen: Los resultados detallados y tablas se encuentran en `docs/Benchmarking.md`. Aquí mantenemos solo las conclusiones clave y el enlace.

- Para `n=5`, la paralelización genera overhead y no mejora el rendimiento.
- La aceleración negativa ocurre cuando el costo fijo de paralelizar supera el ahorro de ejecutar en paralelo.
- Ver `docs/Benchmarking.md` para metodología, tablas ampliadas y análisis.

## Diagrama conceptual de paralelización

```mermaid
flowchart TD
  A[ProgramacionRiegoOptimoPar] --> B[Descomposición en tareas por programación]
  B --> C[Evaluación (CR+CM) en paralelo]
  C --> D[Combinación de resultados]
  D --> E[Selección de mínimo]
```

Interpretación: el diagrama ilustra que la evaluación de costos por programación es independiente y puede ejecutarse en paralelo; luego se combinan los resultados para seleccionar el mínimo, lo que conecta la estrategia de paralelismo con el objetivo matemático de minimización.

### Análisis y Ley de Amdahl

Para $n=5$ el overhead de paralelizar (configuración de tareas, sincronización) domina. `costoMovilidad` presenta la peor aceleración por trabajo útil muy pequeño frente al costo fijo de paralelismo. `generarProgramacionesRiego` muestra incremento marginal de rendimiento. Según Amdahl,
$$ S = \frac{1}{(1-p) + \frac{p}{N}} $$
cuando el tamaño crece ($p$ efectivo aumenta) el overhead se diluye y se esperan mejores speedups.

La aceleración negativa ocurre cuando el costo fijo de paralelizar (creación de tareas, coordinación y sincronización) supera el tiempo ahorrado por ejecutar en paralelo; es común en entradas pequeñas.

Relación con el código:
- `par.map` crea y coordina tareas; si el trabajo por elemento (tablón o programación) es pequeño, el costo de coordinación supera el beneficio.
- Granularidad: aumentar $n$ incrementa el trabajo útil por tarea y reduce el impacto relativo del overhead.

> Recomendación: extender la tabla a tamaños 10, 20, 30, 40, 50 para observar el cruce donde el paralelismo empieza a ser beneficioso.

## Conclusión

Los resultados muestran que el overhead del paralelismo domina para entradas pequeñas y que el incremento marginal de rendimiento aparece solo cuando el tamaño crece, consistente con la Ley de Amdahl. En este contexto, el paralelismo de datos y tareas es recomendable cuando el trabajo útil por tarea supera el costo fijo de coordinación; las implementaciones mantienen el estilo funcional.

Recomendación general: el enfoque funcional es adecuado para trazabilidad y corrección; el paralelismo es recomendable solo para entradas grandes.
