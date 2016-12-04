package de.m7w3.signal

import java.nio.file.{Path, Paths}

import de.m7w3.signal.store.model.Schema
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
    databaseContext.dbFilePath.toFile.exists()
  }

  def profileDirExists: Boolean = config.profileDir.exists()

  def createNewProtocolStore(password: String): SignalDesktopProtocolStore = {
    databaseContext.dbActionRunner(password, onlyIfExists = false, skipCache = false) match {
      case Failure(t) =>
        logger.error("error creating new protocol store", t)
        throw t
      case Success(dBActionRunner) =>
        databaseContext.initializeDatabase(dBActionRunner) // initialize
        SignalDesktopProtocolStore(dBActionRunner)
    }
  }

  def tryLoadExistingStore(password: String, skipCache: Boolean): Try[SignalDesktopProtocolStore] = {
    databaseContext.dbActionRunner(password, onlyIfExists = true, skipCache)
      .map(SignalDesktopProtocolStore(_))
  }
}

case class DatabaseContext(config: Config.SignalDesktopConfig) {

  val DB_NAME = "signal-desktop"
  val DB_USER = "signal-desktop"

  val CONNECTION_PROPERTIES: Map[String, String] = Map(
    "CIPHER" -> "AES"
  )

  val LOAD_CONNECTION_PROPERTIES: Map[String, String] = CONNECTION_PROPERTIES + ("IFEXISTS" -> "TRUE")

  var database: Option[Database] = None

  /**
    * path of the database to be used in the JDBC URL
    */
  def uriDbPath: Path = Paths.get(config.profileDir.getAbsolutePath, DB_NAME)

  /**
    * actual path of the database
    */
  def dbFilePath: Path = Paths.get(uriDbPath.toString + ".mv.db")

  def connectionProperties(onlyIfExists: Boolean): String = {
    val props = if (onlyIfExists) LOAD_CONNECTION_PROPERTIES else CONNECTION_PROPERTIES
    props.map { case (k, v) => s"$k=$v"} mkString(";", ";", "")
  }

  def dbUrl(onlyIfExists: Boolean): String = s"jdbc:h2:file:${uriDbPath.toString};${connectionProperties(onlyIfExists)}"

  private def loadDatabase(password: String, onlyIfExists: Boolean = false, skipCache: Boolean): Try[Database] = {
    database match {
      case Some(db) if(!skipCache) => Success(db)
      case _ => {
        val passwords: String = s"$password $password" // #YOLO
        val loaded = Try {
            Database.forURL(
            url = dbUrl(onlyIfExists),
            user = DB_USER,
            password = passwords,
            driver = "org.h2.Driver")
          }
        loaded.foreach{ db => database = Some(db) }
        loaded
      }
    }
  }

  def initializeDatabase(dBActionRunner: DBActionRunner): Unit = {
    dBActionRunner.run(DBIO.seq(
      Schema.schema.create
    ))
  }

  def dbActionRunner(password: String, onlyIfExists: Boolean = false, skipCache: Boolean): Try[DBActionRunner] = {
    loadDatabase(password, onlyIfExists, skipCache).map(
      DBActionRunner(_, timeout = config.databaseTimeout, verbose = config.verbose)
    )
  }
}
