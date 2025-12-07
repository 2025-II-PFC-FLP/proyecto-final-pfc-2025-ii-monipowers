package taller
import org.scalameter._
import RiegoOptimo._

object App {
  def main(args: Array[String]): Unit = {

    val n = 5 // tamaño de prueba
    val (finca, dist) = Benchmarking.fincaYDistancia(n)

    val pi = (0 until n).toVector

    println(s"\n====== Benchmarking RiegoOptimo (n = $n) ======\n")

    // ================================
    // costoRiegoFinca
    // ================================
    val tCostoRiegoFincaSeq = withWarmer(new Warmer.Default) measure {
      costoRiegoFinca(finca, pi)
    }

    val tCostoRiegoFincaPar = withWarmer(new Warmer.Default) measure {
      costoRiegoFincaPar(finca, pi)
    }

    // ================================
    // costoMovilidad
    // ================================
    val tCostoMovSeq = withWarmer(new Warmer.Default) measure {
      costoMovilidad(finca, pi, dist)
    }

    val tCostoMovPar = withWarmer(new Warmer.Default) measure {
      costoMovilidadPar(finca, pi, dist)
    }

    // ================================
    // generarProgramacionesRiego
    // ================================
    val tGenProgSeq = withWarmer(new Warmer.Default) measure {
      generarProgramacionesRiego(finca)
    }

    val tGenProgPar = withWarmer(new Warmer.Default) measure {
      generarProgramacionesRiegoPar(finca)
    }

    // ================================
    // ProgramacionRiegoOptimo
    // ================================
    val tOptSeq = withWarmer(new Warmer.Default) measure {
      ProgramacionRiegoOptimo(finca, dist)
    }

    val tOptPar = withWarmer(new Warmer.Default) measure {
      ProgramacionRiegoOptimoPar(finca, dist)
    }

    // ================================
    // RESULTADOS
    // ================================
    println("======== RESULTADOS ========\n")

    println(s"costoRiegoFinca Secuencial:  $tCostoRiegoFincaSeq")
    println(s"costoRiegoFinca Paralelo:     $tCostoRiegoFincaPar\n")

    println(s"costoMovilidad Secuencial:   $tCostoMovSeq")
    println(s"costoMovilidad Paralelo:      $tCostoMovPar\n")

    println(s"generarProgramacionesRiego Secuencial: $tGenProgSeq")
    println(s"generarProgramacionesRiego Paralelo:    $tGenProgPar\n")

    println(s"ProgramacionRiegoOptimo Secuencial: $tOptSeq")
    println(s"ProgramacionRiegoOptimo Paralelo:    $tOptPar\n")

    println("============================================\n")
  }
}
