package de.m7w3.signal.resources

import de.m7w3.signal.store.{DBActionRunner, DatabaseLoader, SignalDesktopApplicationStore, SignalDesktopProtocolStore}
import de.m7w3.signal.store.model.Schema
import slick.driver.H2Driver.api._

import scala.concurrent.duration._

/**
  * resources for usage in scalatest suite and junitsuitelike tests
  */
trait TestResource {
  def setupResource(): Unit
  def tearDownResource(): Unit
}

trait DatabaseResource extends TestResource {
  val databaseName: String = "signal-desktop-test"
  var database: Database = _
  var dbActionRunner: DBActionRunner = _

  override def setupResource(): Unit = {
    database = Database.forURL("jdbc:h2:mem:signal-test;DB_CLOSE_DELAY=-1", driver="org.h2.Driver")
    dbActionRunner = DBActionRunner(database, 10.seconds, verbose = true)
    dbActionRunner.run(DBIO.seq(
      Schema.schema.create
    ))
  }

  override def tearDownResource(): Unit = {
    dbActionRunner.run(DBIO.seq(
      Schema.schema.drop
    ))
    database.close()
  }
}

trait StoreResource extends DatabaseResource {

  var protocolStore: SignalDesktopProtocolStore = _
  var applicationStore: SignalDesktopApplicationStore = _

  override def setupResource(): Unit = {
    super.setupResource()
    protocolStore = SignalDesktopProtocolStore(dbActionRunner)
    applicationStore = SignalDesktopApplicationStore(dbActionRunner)
  }
}

