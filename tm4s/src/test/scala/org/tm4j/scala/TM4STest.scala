package org.tm4j.scala

import org.scalatest.FunSuite

import TM4S._

class TM4STest extends FunSuite {
  test("Fifty tausend concurrent mutations") {
    var a = true
    var b = true
    case class BooleanMutator(v: Boolean) extends Thread {
      var corrupted = false

      override def run(): Unit = {

        for (i ← 1 to 50000) {
          if (
            atomic {
              val check = (a != b)
              a = v
              b = v
              check
            }) corrupted = true
        }
      }
    }

    val x = new BooleanMutator(true)
    val y = new BooleanMutator(false)

    x.start()
    y.start()

    x.join()
    y.join()

    assert(!x.corrupted && !y.corrupted)
  }

}
