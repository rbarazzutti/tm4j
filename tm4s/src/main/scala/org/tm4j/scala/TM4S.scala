package org.tm4j.scala

import java.util.concurrent.Callable

import org.tm4j.{TM4J, TMContext, TMExecutor}

import scala.language.implicitConversions

/**
 *
 */
object TM4S {
  private implicit def functionToCallable[R](f: ⇒ R): Callable[R] = new Callable[R] {
    def call: R = f
  }
  
  def transaction[R](f: ⇒ R)(implicit context: TMContext = TM4J.defaultContext): R = TM4J.getExecutor.execute(f)

  def executor=TM4J.getExecutor

  def stats = TM4J.getStats
}
