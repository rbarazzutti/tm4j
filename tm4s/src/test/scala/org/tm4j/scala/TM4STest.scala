package org.tm4j.scala

import org.scalatest.FunSuite

import TM4S._

class TM4STest extends FunSuite {
  val n = 50000;

  test(n + " concurrent mutations") {
    var a = true
    var b = true

    case class BooleanMutator(v: Boolean) extends Thread {
      var corrupted = false

      override def run(): Unit = {

        for (i ← 1 to n) {
          transaction {
            if (a != b)
              corrupted = true
            a = v
            b = v
          }
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
    assert(stats.getSerialCommitsCount == n * 2)
    assert(stats.getTransactionCount == n * 2)
  }
}
