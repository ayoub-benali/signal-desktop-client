package de.m7w3.signal

import de.m7w3.signal.store.SignalDesktopProtocolStore
import de.m7w3.signal.controller.UnlockDB
import java.security.Security

import org.whispersystems.libsignal.logging.SignalProtocolLoggerProvider

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene

object App {
  val NAME = "signal-desktop"
  val VERSION = "0.0.1"
  val AUTHOR = "motherflippers"
}

object Main extends JFXApp {
  Security.insertProviderAt(new org.bouncycastle.jce.provider.BouncyCastleProvider(), 1)
  SignalProtocolLoggerProvider.setProvider(new ProtocolLogger())

  var store: SignalDesktopProtocolStore = _
  var account: AccountHelper = _
  val signalDesktopConfig = Config.optionParser.parse(parameters.raw, Config.SignalDesktopConfig())
  signalDesktopConfig.foreach { config =>
    val appContext = ApplicationContext(config)
    val root = if (appContext.profileDirExists && appContext.profileIsInitialized) {
      UnlockDB.load(appContext)
    } else {
      // TODO: detect and handle error cases
      // show welcome and registration screen
      DeviceRegistration.load(appContext)
    }

    // val root = UnlockDB.load(appContext)
    stage = new PrimaryStage {
      title = "Welcome"
      scene = new Scene(root)
    }
  }

  override def stopApp(): Unit = {
    // TODO: call shutdownExecutor on signal accountManager
    // cleanup shit
    println("bye!")
    super.stopApp()
  }
}
