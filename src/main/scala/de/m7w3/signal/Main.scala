package de.m7w3.signal

import java.security.Security

import de.m7w3.signal.controller.UnlockDB
import org.whispersystems.libsignal.logging.SignalProtocolLoggerProvider

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

  val signalDesktopConfig = Config.optionParser.parse(parameters.raw, Config.SignalDesktopConfig())
  signalDesktopConfig.foreach { config =>
    val ctxBuilder = ContextBuilder(config)
    val root = if (ctxBuilder.profileDirExists && ctxBuilder.profileIsInitialized) {
      UnlockDB(ctxBuilder)
    } else {
      // show welcome and registration screen
      DeviceRegistration.load(ctxBuilder)
    }
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
