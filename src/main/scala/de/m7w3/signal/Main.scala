package de.m7w3.signal

import de.m7w3.signal.store.SignalDesktopProtocolStore
import java.security.Security

import org.whispersystems.libsignal.logging.SignalProtocolLoggerProvider

import scala.reflect.runtime.universe._
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.{Parent, Scene}
import scalafxml.core.{DependenciesByType, FXMLView}

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

    val dependencies = Map[Type, Any](
      typeOf[ApplicationContext] -> appContext
    )

    val root = if (appContext.profileDirExists && appContext.profileIsInitialized) {
      // start chatsList
      // TODO: show password screen
      // store = appContext.tryLoadExistingStore()
      // account = ???
      loadFXML("/de/m7w3/signal/recent_chats_list.fxml", dependencies)
    } else {
      // TODO: detect and handle error cases
      // show welcome and registration screen
      Registration.load(appContext)
    }

    stage = new PrimaryStage {
      title = "Welcome"
      scene = new Scene(root)
    }
  }

  def loadFXML(resourceUri: String, dependencies: Map[Type, Any]): Parent = {
    val fxmlUri = getClass.getResource(resourceUri)
    require(fxmlUri != null, s"$resourceUri not found")
    FXMLView(fxmlUri, new DependenciesByType(dependencies))
  }

  override def stopApp(): Unit = {
    // TODO: call shutdownExecutor on signal accountManager
    // cleanup shit
    println("bye!")
    super.stopApp()
  }
}
