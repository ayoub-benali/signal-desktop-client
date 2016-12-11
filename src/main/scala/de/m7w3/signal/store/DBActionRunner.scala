package de.m7w3.signal.store

import de.m7w3.signal.Logging
import slick.driver.H2Driver.api._
import slick.profile.SqlAction

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

case class DBActionRunner(db: Database, timeout: Duration, verbose: Boolean = false) extends Logging {

  implicit val ec = ExecutionContext.global

  def run[T](action: DBIOAction[T, NoStream, Nothing]) = {
    if (verbose) {
      action match {
        case sqlAction: SqlAction[_, _, _] =>
          sqlAction.statements.foreach(logger.debug)
        case _ =>
      }
    }
    // TODO: how to query synchronously?
    Await.result(db.run(action), timeout)
  }
}
