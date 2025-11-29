package taller
import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RiegoOptimoTest extends AnyFunSuite {
  //TEST DE tIR
  test("tIR: un solo tablón debe iniciar en 0") {
    val f = Vector((5, 3, 1))
    val pi = Vector(0)
    assert(RiegoOptimo.tIR(f, pi) == Vector(0))
  }

  test("tIR: dos tablones secuenciales") {
    val f = Vector((5, 2, 1), (4, 3, 2))
    val pi = Vector(0, 1)
    assert(RiegoOptimo.tIR(f, pi) == Vector(0, 2))
  }

  test("tIR: orden alterado cambia tiempos") {
    val f = Vector((5, 2, 1), (4, 3, 2), (7, 4, 1))
    val pi = Vector(1, 0, 2)
    assert(RiegoOptimo.tIR(f, pi) == Vector(3, 0, 5))
  }

  test("tIR: tiempos acumulados más largos") {
    val f = Vector((10, 5, 1), (8, 4, 2), (7, 6, 1))
    val pi = Vector(0, 2, 1)
    assert(RiegoOptimo.tIR(f, pi) == Vector(0, 5 + 6, 5))
  }

  test("tIR: programa invertido") {
    val f = Vector((5, 2, 1), (6, 4, 2), (7, 3, 3))
    val pi = Vector(2, 1, 0)
    assert(RiegoOptimo.tIR(f, pi) == Vector(4 + 3, 3, 0))
  }
  //TEST DE costoRiegoTablon
  test("costoRiegoTablon: riego temprano sin penalización") {
    val f = Vector((10, 2, 1))
    val pi = Vector(0)
    assert(RiegoOptimo.costoRiegoTablon(0, f, pi) == 10 - 2)
  }

  test("costoRiegoTablon: riego exacto al límite") {
    val f = Vector((5, 3, 2))
    val pi = Vector(0)
    assert(RiegoOptimo.costoRiegoTablon(0, f, pi) == 5 - 3)
  }

  test("costoRiegoTablon: riego tardío penalizado") {
    val f = Vector((5, 4, 3))
    val pi = Vector(0)
    assert(RiegoOptimo.costoRiegoTablon(0, f, pi) == 1)
  }

  test("costoRiegoTablon: penalización mayor por prioridad alta") {
    val f = Vector((6, 5, 10))
    val pi = Vector(0)
    assert(RiegoOptimo.costoRiegoTablon(0, f, pi) == 1)
  }

  test("costoRiegoTablon: riego tardío después de varios tablones") {
    val f = Vector(
      (10, 3, 1),
      (8, 4, 2)
    )
    val pi = Vector(0, 1)

    assert(RiegoOptimo.costoRiegoTablon(1, f, pi) == 8 - 7)
  }

  //TEST DE costoRiegoFinca
  test("costoRiegoFinca: finca con un solo tablón") {
    val f = Vector((10, 3, 1))
    val pi = Vector(0)
    assert(RiegoOptimo.costoRiegoFinca(f, pi) == (10 - 3))
  }

  test("costoRiegoFinca: suma de dos tablones") {
    val f = Vector((8, 2, 1), (7, 3, 2))
    val pi = Vector(0, 1)

    val esperado =
      RiegoOptimo.costoRiegoTablon(0, f, pi) +
        RiegoOptimo.costoRiegoTablon(1, f, pi)

    assert(RiegoOptimo.costoRiegoFinca(f, pi) == esperado)
  }

  test("costoRiegoFinca: programa alterado afecta los tiempos") {
    val f = Vector((10, 3, 1), (7, 2, 2))
    val pi = Vector(1, 0)
    assert(RiegoOptimo.costoRiegoFinca(f, pi) ==
      RiegoOptimo.costoRiegoTablon(0, f, pi) +
        RiegoOptimo.costoRiegoTablon(1, f, pi))
  }

  test("costoRiegoFinca: varios tablones sin penalización") {
    val f = Vector((10, 3, 1), (12, 4, 1), (8, 2, 1))
    val pi = Vector(0, 1, 2)
    val r = RiegoOptimo.costoRiegoFinca(f, pi)
    assert(r == 13)
  }

  test("costoRiegoFinca: finca con penalizaciones") {
    val f = Vector((5, 4, 3), (6, 5, 2))
    val pi = Vector(0, 1)
    assert(RiegoOptimo.costoRiegoFinca(f, pi) ==
      RiegoOptimo.costoRiegoTablon(0, f, pi) +
        RiegoOptimo.costoRiegoTablon(1, f, pi))
  }

  //TEST DE costoMovilidad
  test("costoMovilidad: movilidad simple entre dos tablones") {
    val pi = Vector(0, 1)
    val d = Vector(
      Vector(0, 5),
      Vector(5, 0)
    )
    assert(RiegoOptimo.costoMovilidad(Vector(), pi, d) == 5)
  }

  test("costoMovilidad: tres tablones encadenados") {
    val pi = Vector(0, 2, 1)
    val d = Vector(
      Vector(0, 4, 3),
      Vector(4, 0, 6),
      Vector(3, 6, 0)
    )
    assert(RiegoOptimo.costoMovilidad(Vector(), pi, d) == (3 + 6))
  }

  test("costoMovilidad: programa invertido") {
    val pi = Vector(2, 1, 0)
    val d = Vector(
      Vector(0, 2, 4),
      Vector(2, 0, 3),
      Vector(4, 3, 0)
    )
    assert(RiegoOptimo.costoMovilidad(Vector(), pi, d) == (3 + 2))
  }

  test("costoMovilidad: recorrido largo") {
    val pi = Vector(0, 1, 2, 3)
    val d = Vector(
      Vector(0, 1, 2, 3),
      Vector(1, 0, 4, 5),
      Vector(2, 4, 0, 6),
      Vector(3, 5, 6, 0)
    )
    assert(RiegoOptimo.costoMovilidad(Vector(), pi, d) == (1 + 4 + 6))
  }

  test("costoMovilidad: todos los movimientos iguales") {
    val pi = Vector(0, 1, 2)
    val d = Vector(
      Vector(0, 10, 10),
      Vector(10, 0, 10),
      Vector(10, 10, 0)
    )
    assert(RiegoOptimo.costoMovilidad(Vector(), pi, d) == 20)
  }

}