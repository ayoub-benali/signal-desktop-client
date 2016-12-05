package de.m7w3.signal

import de.m7w3.signal.Config.SignalDesktopConfig
import scala.concurrent.duration._
import java.io.File
import de.m7w3.signal.store.SignalDesktopProtocolStore
import scala.util.{Success, Try}
import org.junit.rules.ExternalResource
import de.m7w3.signal.store.{DBActionRunner, TestUtils}
import slick.driver.H2Driver.api._
import scala.concurrent.duration._
import de.m7w3.signal.store.model.Schema
import org.whispersystems.libsignal.SignalProtocolAddress
import de.m7w3.signal.store.SignalDesktopProtocolStore

/* because we have to use JUnit for the UI tests, currently we cannot combine BeforeAndAfterEach and JUnitSuiteLike traits :/
 * therefore I had to duplicated the code that is in TestDatabase and TestStore here because both extend BeforeAndAfterEach
 * which makes them unusable with JUnit
 */

trait ApplicationContextTest {

  val databaseName: String = "signal-desktop-test"
  var database: Database = _
  var context: ApplicationContext = _
  var dBActionRunner: DBActionRunner = _
  var protocolStore: SignalDesktopProtocolStore = _

  val localIdentity = TestUtils.generateIdentity
  val remoteIdentity = TestUtils.generateIdentity

  val localAddress: SignalProtocolAddress = new SignalProtocolAddress("+49123456789", 1)
  val remoteAddress: SignalProtocolAddress = new SignalProtocolAddress("+49987654321", 2)

  val resource = new ExternalResource() {
    override def before(): Unit = {
      database = Database.forURL("jdbc:h2:mem:signal-test;DB_CLOSE_DELAY=-1", driver="org.h2.Driver")
      dBActionRunner = DBActionRunner(database, 10.seconds, verbose = true)
      dBActionRunner.run(DBIO.seq(
        Schema.schema.create
      ))
      protocolStore = SignalDesktopProtocolStore(dBActionRunner)
      context = {
        new ApplicationContext(SignalDesktopConfig(false, 1.seconds, new File("foo"))){
          override def profileDirExists: Boolean = true
          override def profileIsInitialized: Boolean = true
          override def createNewProtocolStore(password: String): SignalDesktopProtocolStore = protocolStore
          override def tryLoadExistingStore(password: String, skipCache: Boolean): Try[SignalDesktopProtocolStore] = Success(protocolStore)
        }
      }
    }
    override def after(): Unit = {
      dBActionRunner.run(DBIO.seq(
        Schema.schema.drop
      ))
      database.close()
    }
  }
}