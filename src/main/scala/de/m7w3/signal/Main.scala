package de.m7w3.signal

import java.security.Security

import de.m7w3.signal.Registration.{Step1, Step2, Step3}
import org.whispersystems.libsignal.logging.SignalProtocolLoggerProvider

import scala.reflect.runtime.universe._
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Pos
import scalafx.scene.layout.StackPane
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

  var account: AccountHelper = _
  // val APP_NAME = "signal-desktop"
  // val VERSION = "0.0.1"

  val signalDesktopConfig = Config.optionParser.parse(parameters.raw, Config.SignalDesktopConfig())
  signalDesktopConfig.foreach { config =>
    val appContext = ApplicationContext(config)

    val dependencies = Map[Type, Any](
      typeOf[ApplicationContext], appContext
    )

    val primaryScene = if (appContext.profileDirExists && appContext.profileIsInitialized) {
      // start chatsList
      val root = loadFXML("/de/m7w3/signal/recent_chats_list.fxml", dependencies)
      new Scene(root)
    } else {
      // TODO: detect and handle error cases
      // show welcome and registration screen
      new Scene(800, 600) {
        val stack = new StackPane
        stack.alignment = Pos.Center
        stack.getChildren.add(Step1)
        stack.getChildren.add(Step2)
        stack.getChildren.add(Step3)
        root = stack
      }
    }
    stage = new PrimaryStage {
      title = "Welcome"
      scene = primaryScene
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
