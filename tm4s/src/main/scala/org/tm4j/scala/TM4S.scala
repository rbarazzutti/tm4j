package org.tm4j.scala

import java.util.concurrent.Callable

import org.tm4j.{TM4J, TMContext, TMExecutor}

import scala.language.implicitConversions

object TM4S {
  private implicit def functionToCallable[R](f: ⇒ R): Callable[R] = new Callable[R] {
    def call: R = f
  }

  private lazy val defaultContext = new TMContext {
    override def getMaxNumberOfRetries: Int = 76
  }

  def atomic[R](f: ⇒ R)(implicit context: TMContext = defaultContext): R = TM4J.getExecutor.execute(f)
}
