package de.m7w3.signal

import java.nio.file.{Path, Paths}

import de.m7w3.signal.exceptions.DatabaseDoesNotExistException
import de.m7w3.signal.store.model.Identity
import de.m7w3.signal.store.{DBActionRunner, SignalDesktopProtocolStore}
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import org.slf4j.LoggerFactory
import slick.driver.H2Driver.api._

import scala.util.{Failure, Success, Try}

case class ApplicationContext(config: Config.SignalDesktopConfig) {

  private val logger = LoggerFactory.getLogger(getClass)
  val databaseContext = DatabaseContext(config)

  if (config.verbose) {
    Configurator.setRootLevel(Level.DEBUG)
  }

  def profileIsInitialized: Boolean = {
    // simple check if database file exists
    // because for all other checks we'd need a password
    databaseContext.dbPath.toFile.exists()
  }

  def profileDirExists: Boolean = config.profileDir.exists()

  def createNewProtocolStore(identity: Identity, password: String): SignalDesktopProtocolStore = {
    databaseContext.dbActionRunner(password, onlyIfExists = false) match {
      case Failure(t) =>
        logger.error("error creating new protocol store", t)
        throw t
      case Success(dBActionRunner) =>
        val store = SignalDesktopProtocolStore(dBActionRunner)
        store.identityKeyStore.initialize(identity)
        store
    }
  }

  def tryLoadExistingStore(password: String): Try[SignalDesktopProtocolStore] = {
    databaseContext.dbActionRunner(password, onlyIfExists = true)
      .map(SignalDesktopProtocolStore)
  }
}

case class DatabaseContext(config: Config.SignalDesktopConfig) {

  val DB_NAME = "signal-desktop.db"
  val DB_USER = "signal-desktop"

  val CONNECTION_PROPERTIES = Map("CIPHER" -> "AES")

  val LOAD_CONNECTION_PROPERTIES = CONNECTION_PROPERTIES + ("IFEXISTS" -> "TRUE")

  var database: Option[Database] = None

  def dbPath: Path = Paths.get(config.profileDir.getAbsolutePath, DB_NAME)

  def connectionProperties(onlyIfExists: Boolean): String = {
    val props = if (onlyIfExists) LOAD_CONNECTION_PROPERTIES else CONNECTION_PROPERTIES
    props.map { case (k, v) => s"$k=$v"} mkString(";", ";", "")
  }

  def dbUrl(onlyIfExists: Boolean): String = s"jdbc:h2:file:${dbPath.toString};${connectionProperties(onlyIfExists)}"

  private def loadDatabase(password: String, onlyIfExists: Boolean = false): Try[Database] = {
    database match {
      case None =>
        if (onlyIfExists) {
          Failure(new DatabaseDoesNotExistException)
        } else {

          val passwords: String = s"$password $password" //TODO: is this secure?
          Success(Database.forURL(
            url = dbUrl(onlyIfExists),
            user = DB_USER,
            password = passwords,
            driver = "org.h2.Driver"))
        }
      case Some(db) => Success(db)
    }
  }

  def dbActionRunner(password: String, onlyIfExists: Boolean = false): Try[DBActionRunner] = {
    loadDatabase(password, onlyIfExists).map(
      DBActionRunner(_, timeout = config.databaseTimeout, verbose = config.verbose)
    )
  }
}
