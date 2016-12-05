package de.m7w3.signal.store

import de.m7w3.signal.store.model.Schema
import org.scalatest.{BeforeAndAfterEach, Suite}
import slick.driver.H2Driver.api._

import scala.concurrent.duration._

trait TestDatabase extends BeforeAndAfterEach { self: Suite =>

  val databaseName: String = "signal-desktop-test"
  var database: Database = _
  var dBActionRunner: DBActionRunner = _

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    database = Database.forURL("jdbc:h2:mem:signal-test;DB_CLOSE_DELAY=-1", driver="org.h2.Driver")
    dBActionRunner = DBActionRunner(database, 10.seconds, verbose = true)
    dBActionRunner.run(DBIO.seq(
      Schema.schema.create
    ))
  }

  override protected def afterEach(): Unit = {
    dBActionRunner.run(DBIO.seq(
      Schema.schema.drop
    ))
    database.close()

    super.afterEach()
  }
}