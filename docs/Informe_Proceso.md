# Informe 1 – Proceso Funcional – Riego Óptimo

**Integrantes:**

- Jann Carlo Martinez Cardona — Código: 2459369
- Cristhian David Zuluaga Gacha — Código: 2459542
- Miguel Angel Sanclemente Mejia — Código: 2459488
- Rigoberto Ospina Martinez — Código: 2459734

---

## Introducción

Este informe describe el proceso funcional del proyecto Riego Óptimo, mostrando los mecanismos de recursión y acumulación y evidencias de su implementación en Scala.

El problema a resolver es la programación óptima del riego en una finca compuesta por tablones, minimizando simultáneamente el costo por sufrimiento de los cultivos (por regar demasiado tarde o demasiado pronto) y el costo de movilidad del sistema de riego entre tablones. Es relevante porque balancea calidad del cultivo y costos operativos. Se busca optimizar la función objetivo $CR^{\Pi}_F + CM^{\Pi}_F$ sobre todas las programaciones $\Pi$. El abordaje utiliza programación funcional pura en Scala (recursión, estructuras inmutables y funciones de alto orden) y, en un informe complementario, paralelización de datos y tareas para evaluar posibles aceleraciones en problemas de mayor tamaño.

## Enunciado y formalización

- Finca: $F=\langle T_0,\dots,T_{n-1} \rangle$, con $T_i=\langle ts_i, tr_i, p_i \rangle$.
- Programación de riego: $\Pi=\langle \pi_0,\dots,\pi_{n-1} \rangle$ (permutación de $\{0,\dots,n-1\}$).
- Tiempos de inicio:
  $$ t^{\Pi}_{\pi_0}=0, \quad t^{\Pi}_{\pi_j}=t^{\Pi}_{\pi_{j-1}} + tr_{\pi_{j-1}} \text{ para } j=1,\dots,n-1. $$
- Costo de riego por tablón $i$:
  $$ CR^{\Pi}_F[i] = \begin{cases}
  ts_i - (t^{\Pi}_i + tr_i), & \text{si } ts_i - tr_i \ge t^{\Pi}_i \\
  p_i\,\big((t^{\Pi}_i + tr_i) - ts_i\big), & \text{de lo contrario}
  \end{cases} $$
- Costo total de riego: $CR^{\Pi}_F=\sum_{i=0}^{n-1} CR^{\Pi}_F[i]$.
- Costo de movilidad: $CM^{\Pi}_F=\sum_{j=0}^{n-2} DF[\pi_j,\pi_{j+1}]$, con $DF$ simétrica y $DF[i,i]=0$.
- Objetivo: $\min_{\Pi}\big( CR^{\Pi}_F + CM^{\Pi}_F \big)$.

---

## Pila de llamadas – generación de programaciones (`permutar`)

Ejemplo con $n=3$, lista inicial $[0,1,2]$:

```mermaid
flowchart TD
  A[permutar([0,1,2])] --> B[permutar([1,2])] --> C[permutar([2])]
  C --> D[permutar([]) => [[]]]
  D --> E[insertar 2 en todas las posiciones de [] => [[2]]]
  E --> F[insertar 1 en [[2]] => [[1,2],[2,1]]]
  F --> G[insertar 0 en cada perm => [[0,1,2],[1,0,2],[1,2,0],[0,2,1],[2,0,1],[2,1,0]]]
```

La recursión combina “insertar en todas las posiciones” generando todas las $3!$ permutaciones sin duplicados.

Interpretación: el diagrama muestra la construcción sistemática de todas las permutaciones a partir de la lista base, evidenciando recursión estructural y ausencia de duplicados por inserción en posiciones distintas; es clave para garantizar cobertura completa de $\Pi$.

### Evidencia de programación funcional (código real)

Recursión e inmutabilidad (`Vector`) y funciones de alto orden (`flatMap`, `foldLeft`):

```scala
def insertarEnTodasPosiciones(x: Int, v: Vector[Int]): Vector[Vector[Int]] =
  v.indices.foldLeft(Vector(Vector(x) ++ v))((acc,i) => acc :+ (v.take(i+1) ++ Vector(x) ++ v.drop(i+1)))

def permutar(lista: Vector[Int]): Vector[ProgRiego] =
  if(lista.isEmpty) Vector(Vector())
  else {
    val resto = permutar(lista.tail)
    resto.flatMap(p => insertarEnTodasPosiciones(lista.head, p))
  }
```

Acumulación con `foldLeft` (cálculo de tiempos) y encapsulación funcional mediante funciones internas:

```scala
def tIR(f: Finca, pi: ProgRiego): TiempoInicioRiego = {
  def tiemposEnOrden(orden: ProgRiego): Vector[Int] =
    orden.foldLeft((Vector[Int](), 0)) { case ((acc, acum), tablon) =>
      (acc :+ acum, acum + treg(f, tablon))
    }._1
  val tOrden = tiemposEnOrden(pi)
  Vector.tabulate(f.length)(i => tOrden(pi.indexOf(i)))
}
```

Observaciones:
- Inmutabilidad: uso de `Vector` y acumuladores sin mutar estado global.
- Alto orden: `foldLeft`, `flatMap`, `map`, `tabulate`.
- Encapsulación: funciones locales (`tiemposEnOrden`) que aíslan lógica.

## Proceso de `tIR` (acumulación por `foldLeft`)

Para una programación $\Pi=\langle \pi_0,\pi_1,\pi_2 \rangle$:

```mermaid
sequenceDiagram
  participant F as f
  participant T as tIR
  Note over T: acum=0
  T->>F: treg(f, \pi_0)
  Note over T: t[\pi_0]=0; acum+=tr_{\pi_0}
  T->>F: treg(f, \pi_1)
  Note over T: t[\pi_1]=acum; acum+=tr_{\pi_1}
  T->>F: treg(f, \pi_2)
  Note over T: t[\pi_2]=acum; acum+=tr_{\pi_2}
```

El vector final asocia cada tablón $i$ con su $t^{\Pi}_i$ vía la posición de $i$ dentro de $\Pi$.

Interpretación: la secuencia evidencia acumulación determinista del tiempo mediante `foldLeft` y su correspondencia con la recurrencia matemática de $t^{\Pi}$; es relevante porque asegura trazabilidad entre el orden de riego y los tiempos calculados.

---

## Conclusión

Los procesos funcionales descritos (recursión para generar permutaciones y acumulación con `foldLeft` para calcular tiempos) ofrecen una trazabilidad clara del estado y garantizan soluciones expresivas sin mutabilidad. Los diagramas Mermaid complementan la comprensión del flujo de llamadas y la construcción del resultado.

El algoritmo es completo porque explora exhaustivamente las $n!$ permutaciones de tablones y toma el mínimo de todos los costos calculados; por lo tanto, siempre encuentra la solución óptima.

Recomendación general: el enfoque funcional es adecuado para trazabilidad y corrección; el paralelismo es recomendable solo para entradas grandes.
