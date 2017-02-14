package de.m7w3.signal

import de.m7w3.signal.events.{EventDispatcher, EventPublisher, SignalDesktopEventDispatcher}
import de.m7w3.signal.store.model.Schema
import de.m7w3.signal.store.{DBActionRunner, DatabaseLoader, SignalDesktopApplicationStore, SignalDesktopProtocolStore}
import monix.reactive.Observable
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import org.slf4j.LoggerFactory
import slick.driver.H2Driver.api._

import scala.util.Try

trait ApplicationContext


case class ContextBuilder(config: Config.SignalDesktopConfig) {

  private val logger = LoggerFactory.getLogger(getClass)
  val databaseLoader = DatabaseLoader(config.profileDir.getAbsolutePath)

  if (config.verbose) {
    Configurator.setRootLevel(Level.DEBUG)
  }

  def profileIsInitialized: Boolean = {
    // simple check if database file exists
    // because for all other checks we'd need a password
    databaseLoader.dbFilePath.toFile.exists()
  }

  def profileDirExists: Boolean = config.profileDir.exists()

  def buildWithExistingStore(password: String): Try[InitiatedContext] = {
    databaseLoader.loadDatabase(password, onlyIfExists = true)
      .map(DBActionRunner(_, config.databaseTimeout, config.verbose))
      .map(dbRunner => {
        val protocolStore = SignalDesktopProtocolStore(dbRunner)
        val applicationStore = SignalDesktopApplicationStore(dbRunner)
        val regData = protocolStore.getRegistrationData()
        val accountHelper = AccountHelper(regData.userName, regData.password)
        InitiatedContext(
          accountHelper,
          dbRunner,
          protocolStore,
          applicationStore
        )
      })
  }

  def buildWithNewStore(accountHelper: AccountHelper, password: String): Try[InitiatedContext] = {
    databaseLoader.loadDatabase(password)
      .map(DBActionRunner(_, config.databaseTimeout, config.verbose))
      .map(dbRunner => {
        logger.info("initializing database...")
        dbRunner.run(DBIO.seq(
          Schema.schema.create
        ))
        logger.info("database successfully initialized")
        dbRunner
      }).map(dbRunner => {
        val store = SignalDesktopProtocolStore(dbRunner)
        val applicationStore = SignalDesktopApplicationStore(dbRunner)
        InitiatedContext(
          accountHelper,
          dbRunner,
          store,
          applicationStore)
    })
  }
}

case class InitiatedContext(account: AccountHelper,
                            dBActionRunner: DBActionRunner,
                            protocolStore: SignalDesktopProtocolStore,
                            applicationStore: SignalDesktopApplicationStore) extends SignalDesktopEventDispatcher with ApplicationContext




