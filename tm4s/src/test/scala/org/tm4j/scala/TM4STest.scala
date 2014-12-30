package org.tm4j.scala

import org.scalatest.FunSuite

import TM4S._

class TM4STest extends FunSuite {
  val n = 500
  val t = 200

  test(s"$t concurrent threads each one executing $n transactions") {
    var a = 0
    var b = 0

    case class Mutator(v: Int) extends Thread {
      var corrupted = false

      override def run(): Unit = {
        for (i ← 1 to n) {
          transaction {
            if (a != b)
              corrupted = true
            a += v
            b += v
          }
          // wait 100 µs to avoid to increase contention
          Thread.sleep(0, 100)
        }
      }
    }

    val threads = (0 until t).map(i ⇒ Mutator(2 * i - t + 1))

    threads.foreach(_.start())

    threads.foreach(_.join())

    threads.foreach(t ⇒ assert(!t.corrupted))

    assert(stats.getSerials == n * t)
    assert(stats.getTransactions == n * t)

    assert(a == 0)
    assert(b == 0)
  }
}
