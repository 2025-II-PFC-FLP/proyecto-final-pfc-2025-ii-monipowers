package taller

object RiegoOptimo {
  type Tablon = (Int, Int, Int)
  type Finca = Vector[Tablon]
  type Distancia = Vector[Vector[Int]]
  type ProgRiego = Vector[Int]
  type TiempoInicioRiego = Vector[Int]

  val random = new scala.util.Random()
  def fincaAlAzar(long: Int): Finca = {
    val v = Vector.fill(long)(
      (random.nextInt(long * 2) + 1,
        random.nextInt(long) + 1,
        random.nextInt(4) + 1)
    )
    v
  }

  def distanciaAlAzar(long: Int): Distancia = {
    val v = Vector.fill(long, long)(random.nextInt(long * 3) + 1)
    Vector.tabulate(long, long)((i, j) =>
      if (i < j) v(i)(j)
      else if (i == j) 0
      else v(j)(i)
    )
  }

  def tsup(f: Finca, i: Int): Int = f(i)._1
  def treg(f: Finca, i: Int): Int = f(i)._2
  def prio(f: Finca, i: Int): Int = f(i)._3

  def tIR(f: Finca, pi: ProgRiego): TiempoInicioRiego = {
    def tiemposEnOrden(orden: ProgRiego): Vector[Int] =
      orden.foldLeft((Vector[Int](), 0)) { case ((acc, acum), tablon) =>
        (acc :+ acum, acum + treg(f, tablon))
      }._1
    val tOrden = tiemposEnOrden(pi)
    Vector.tabulate(f.length)(i =>
      tOrden(pi.indexOf(i))
    )
  }

  def costoRiegoTablon(i: Int, f: Finca, pi: ProgRiego): Int = {
    val tiempoInicio = tIR(f, pi)(i)
    val tiempoFinal  = tiempoInicio + treg(f, i)

    if (tsup(f, i) - treg(f, i) >= tiempoInicio) {
      tsup(f, i) - tiempoFinal
    } else {
      prio(f, i) * (tiempoFinal - tsup(f, i))
    }
  }

  def costoRiegoFinca(f: Finca, pi: ProgRiego): Int = {
    (0 until f.length).map(i => costoRiegoTablon(i, f, pi)).sum
  }

  def costoMovilidad(f: Finca, pi: ProgRiego, d: Distancia): Int = {
    (0 until pi.length - 1).map(j => d(pi(j))(pi(j + 1))).sum
  }

  def generarProgramacionesRiego(f: Finca): Vector[ProgRiego] = {

    def insertarEnTodasPosiciones(x: Int, v: Vector[Int]): Vector[Vector[Int]] =
      v.indices.foldLeft(Vector(Vector(x) ++ v))((acc,i) => acc :+ (v.take(i+1) ++ Vector(x) ++ v.drop(i+1)))

    def permutar(lista: Vector[Int]): Vector[ProgRiego] =
      if(lista.isEmpty) Vector(Vector())
      else {
        val resto = permutar(lista.tail)
        resto.flatMap(p => insertarEnTodasPosiciones(lista.head, p))
      }

    permutar((0 until f.length).toVector)
  }

}
