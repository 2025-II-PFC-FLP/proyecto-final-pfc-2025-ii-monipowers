package taller

object Benchmarking {

  def fincaYDistancia(n: Int): (RiegoOptimo.Finca, RiegoOptimo.Distancia) = {
    val f = RiegoOptimo.fincaAlAzar(n)
    val d = RiegoOptimo.distanciaAlAzar(n)
    (f, d)
  }

}
