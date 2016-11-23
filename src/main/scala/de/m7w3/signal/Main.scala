package de.m7w3.signal

import java.security.Security
import org.whispersystems.libsignal.logging.SignalProtocolLoggerProvider
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Pos
import scalafx.scene.layout.StackPane
import scalafx.scene.Scene
import scalafxml.core.{DependenciesByType, FXMLView, NoDependencyResolver}
import scopt.OptionParser

object Main extends JFXApp {
  Security.insertProviderAt(new org.bouncycastle.jce.provider.BouncyCastleProvider(), 1)
  SignalProtocolLoggerProvider.setProvider(new ProtocolLogger())

  var account: AccountHelper = _
  // val APP_NAME = "signal-desktop"
  // val VERSION = "0.0.1"

  // case class SignalDeskTopConfig(fxml: Boolean = true, verbose: Boolean = false)

  // val optionParser = new OptionParser[SignalDeskTopConfig](APP_NAME) {
  //   head(APP_NAME, VERSION)
  //   opt[Unit]("verbose").action( (_, c) =>
  //     c.copy(verbose = true) ).text("provide verbose output on console")

  //   opt[Unit]("fxml").action( (_, c) =>
  //     c.copy(fxml = true) ).text("launch fxml ui")
  // }
  // val signalDesktopConfig = optionParser.parse(parameters.raw, SignalDeskTopConfig())

  // val primaryScene = if (signalDesktopConfig.exists(_.fxml)) {
  //   val lxmlUri = getClass.getResource("/de/m7w3/signal/recent_chats_list.fxml")
  //   require(lxmlUri != null, "lxmlUri not found")
  //   val root = FXMLView(lxmlUri, new DependenciesByType(Map.empty))
  //   new Scene(root)
  // } else {
    val primaryScene = new Scene(800, 600){
      val stack = new StackPane
      stack.alignment = Pos.Center
      stack.children = List(Registration.Step1, Registration.Step2, Registration.Step3)
      root = stack
    }
  // }

  stage = new PrimaryStage {
    title = "Welcome"
    scene = primaryScene
  }

  override def stopApp(): Unit = {
    // TODO: call shutdownExecutor on signal accountManager
    // cleanup shit
    println("bye!")
    super.stopApp()
  }
}