package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RiegoOptimoTest extends AnyFunSuite {
  //TEST tIR
  test("tIR: primer tablon inicia en 0") {
    val f = Vector((10, 2, 1), (10, 3, 2))
    val pi = Vector(0, 1)
    assert(RiegoOptimo.tIR(f, pi)(0) == 0)
  }

  test("tIR: segundo tablon inicia en el tiempo del primero") {
    val f = Vector((10, 2, 1), (10, 3, 2))
    val pi = Vector(0, 1)
    assert(RiegoOptimo.tIR(f, pi)(1) == 2)
  }

  test("tIR: respeta el orden del programa") {
    val f = Vector((10, 2, 1), (10, 5, 2), (10, 1, 1))
    val pi = Vector(2, 0, 1)
    val tiempos = RiegoOptimo.tIR(f, pi)
    assert(tiempos(2) == 0 && tiempos(0) == 1 && tiempos(1) == 3)
  }

  test("tIR: devolucion igual al número de tablones") {
    val f = Vector.fill(5)((10, 2, 1))
    val pi = Vector(0,1,2,3,4)
    assert(RiegoOptimo.tIR(f, pi).size == 5)
  }

  test("tIR: tiempos siempre crecientes según pi") {
    val f = Vector((10,1,1),(10,2,1),(10,3,1))
    val pi = Vector(1,0,2)
    val t = RiegoOptimo.tIR(f, pi)
    assert(t(1) < t(0) + 2 && t(0) < t(2))
  }

  //TEST costoRiegoTablon
  test("costoRiegoTablon: sin retraso genera costo pequeño") {
    val f = Vector((10, 2, 1))
    val pi = Vector(0)
    assert(RiegoOptimo.costoRiegoTablon(0, f, pi) == 8)
  }

  test("costoRiegoTablon: retraso aumenta costo") {
    val f = Vector((2, 3, 2)) // se riega tarde
    val pi = Vector(0)
    assert(RiegoOptimo.costoRiegoTablon(0, f, pi) > 0)
  }

  test("costoRiegoTablon: prioridad afecta penalización") {
    val f1 = Vector((2, 3, 1))
    val f2 = Vector((2, 3, 5))
    val pi = Vector(0)
    assert(RiegoOptimo.costoRiegoTablon(0, f2, pi) > RiegoOptimo.costoRiegoTablon(0, f1, pi))
  }

  test("costoRiegoTablon: ningún tablon da costo negativo") {
    val f = Vector((10, 3, 2))
    val pi = Vector(0)
    assert(RiegoOptimo.costoRiegoTablon(0, f, pi) >= 0)
  }

  test("costoRiegoTablon: costo calculado para cualquier orden") {
    val f = Vector((10,1,1),(5,3,2))
    val pi = Vector(1,0)
    assert(RiegoOptimo.costoRiegoTablon(1, f, pi) >= 0)
  }

  //TEST costoRiegoFinca
  test("costoRiegoFinca: suma varios costos") {
    val f = Vector((10,2,1),(10,3,1))
    val pi = Vector(0,1)
    val c = RiegoOptimo.costoRiegoFinca(f, pi)
    assert(c == RiegoOptimo.costoRiegoTablon(0,f,pi) + RiegoOptimo.costoRiegoTablon(1,f,pi))
  }

  test("costoRiegoFinca: finca vacía da costo 0") {
    val f = Vector()
    val pi = Vector()
    assert(RiegoOptimo.costoRiegoFinca(f, pi) == 0)
  }

  test("costoRiegoFinca: nunca negativo") {
    val f = Vector((1,3,2))
    val pi = Vector(0)
    assert(RiegoOptimo.costoRiegoFinca(f, pi) >= 0)
  }

  test("costoRiegoFinca: depende del orden") {
    val f = Vector((10,2,1),(5,4,2))
    val pi1 = Vector(0,1)
    val pi2 = Vector(1,0)
    assert(RiegoOptimo.costoRiegoFinca(f, pi1) != RiegoOptimo.costoRiegoFinca(f, pi2))
  }

  test("costoRiegoFinca: tamaño de la finca igual al número de costos") {
    val f = Vector((10,1,1),(10,1,1),(10,1,1))
    val pi = Vector(0,1,2)
    val c = RiegoOptimo.costoRiegoFinca(f, pi)
    assert(c >= 0)
  }

  //TEST costoMovilidad
  test("costoMovilidad: movimiento simple entre dos tablones") {
    val d = Vector(Vector(0,5), Vector(5,0))
    val pi = Vector(0,1)
    assert(RiegoOptimo.costoMovilidad(Vector(), pi, d) == 5)
  }

  test("costoMovilidad: rutas más largas suman más") {
    val d = Vector(Vector(0,1,10), Vector(1,0,2), Vector(10,2,0))
    val pi = Vector(0,1,2)
    assert(RiegoOptimo.costoMovilidad(Vector(), pi, d) == 1 + 2)
  }

  test("costoMovilidad: programa de riego de un solo tablon da 0") {
    val d = Vector(Vector(0))
    val pi = Vector(0)
    assert(RiegoOptimo.costoMovilidad(Vector(), pi, d) == 0)
  }

  test("costoMovilidad: usa la distancia según el orden exacto") {
    val d = Vector(
      Vector(0,5,100),
      Vector(5,0,7),
      Vector(100,7,0)
    )
    val pi = Vector(2,1,0)
    assert(RiegoOptimo.costoMovilidad(Vector(), pi, d) == 7 + 5)
  }

  test("costoMovilidad: distancia nunca negativa") {
    val d = RiegoOptimo.distanciaAlAzar(4)
    val pi = Vector(0,1,2,3)
    assert(RiegoOptimo.costoMovilidad(Vector(), pi, d) >= 0)
  }


}
