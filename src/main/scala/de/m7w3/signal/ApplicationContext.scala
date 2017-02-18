package de.m7w3.signal

import de.m7w3.signal.account.{AccountHelper, AccountInitializationHelper}
import de.m7w3.signal.events.SignalDesktopEventDispatcher
import de.m7w3.signal.messages.{MessageReceiver, MessageSender, SignalMessageSender}
import de.m7w3.signal.store.model.Schema
import de.m7w3.signal.store.{DBActionRunner, DatabaseLoader, SignalDesktopApplicationStore, SignalDesktopProtocolStore}
import monix.execution.atomic.Atomic
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import slick.driver.H2Driver.api._

import scala.util.Try


case class ContextBuilder(config: Config.SignalDesktopConfig) extends Logging {

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

  def buildWithExistingStore(password: String): Try[ApplicationContext] = {
    databaseLoader.loadDatabase(password, onlyIfExists = true)
      .map(DBActionRunner(_, config.databaseTimeout, config.verbose))
      .map(dbRunner => {
        val protocolStore = SignalDesktopProtocolStore(dbRunner)
        val applicationStore = SignalDesktopApplicationStore(dbRunner)
        val regData = protocolStore.getRegistrationData()
        val messageSender = SignalMessageSender(regData, protocolStore)
        val accountHelper = AccountHelper(regData, messageSender)
        ApplicationContext(
          messageSender,
          accountHelper,
          dbRunner,
          protocolStore,
          applicationStore
        )
      })
  }

  def buildWithNewStore(accountInitHelper: AccountInitializationHelper, deviceName: String, password: String): Try[ApplicationContext] = {
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
        val registrationData = accountInitHelper.finishDeviceRegistration(deviceName, store)
        val messageSender = SignalMessageSender(registrationData, store)
        val accountHelper = AccountHelper(registrationData, messageSender)
        ApplicationContext(
          messageSender,
          accountHelper,
          dbRunner,
          store,
          applicationStore)
    })
  }
}

case class ApplicationContext(messageSender: MessageSender,
                              account: AccountHelper,
                              dBActionRunner: DBActionRunner,
                              protocolStore: SignalDesktopProtocolStore,
                              applicationStore: SignalDesktopApplicationStore) extends SignalDesktopEventDispatcher {
  override def close(): Unit = {
    super.close()
    dBActionRunner.close()
  }
}

object ApplicationContext {

  private val current = Atomic(None.asInstanceOf[Option[ApplicationContext]])

  def getCurrent: Option[ApplicationContext] = current.get

  /**
    * initialize additional components that rely on the application context,
    * thus can't be instantiated/initialized earlier.
    */
  def initialize(applicationContext: ApplicationContext): Unit = {
    current.set(Some(applicationContext))
    MessageReceiver.initialize(applicationContext)
    Listeners.initialize(applicationContext)
  }
}




