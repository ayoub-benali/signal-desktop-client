package de.m7w3.signal

import javafx.scene.layout.VBox

import scopt.OptionParser

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.event.ActionEvent
import scalafx.geometry.Pos
import scalafx.scene._
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.layout.StackPane
import scalafxml.core.{DependenciesByType, FXMLView, NoDependencyResolver}

object Step1 extends VBox{
  this.alignment = Pos.Center
  private val hello = new Label("Hello")
  private val appRequired = new Label("You must have Signal for Android for using this client")
  private val haveApp = new Button("I have Signal for Android")
  haveApp.onAction = (a: ActionEvent) => {
    Step1.setVisible(false)
    Step2.setVisible(true)
  }
  this.children = List(hello, appRequired, haveApp)
}

object Step2 extends VBox{
  Step2.setVisible(false)
  this.alignment = Pos.Center
  private val device = new Label("Enter a name for this device:")
  val deviceName = new TextField()
  // TODO: set reasonable width
  val ok = new Button("OK")
  ok.onAction = (a: ActionEvent) => {
    Step2.setVisible(false)
    Step3.setVisible(true)
  }
  this.children = List(device, deviceName, ok)
}

object Step3 extends VBox{
  Step3.setVisible(false)
  this.alignment = Pos.Center
  val qrCode = new StackPane
  private val label = new Label("Open Signal on your phone and go to Settings > Devices. Click on the plus icon and scan the above QR code.")
  this.children = List(qrCode, label)
}

object Main extends JFXApp {

  val APP_NAME = "signal-desktop"
  val VERSION = "0.0.1"

  case class SignalDeskTopConfig(fxml: Boolean = true, verbose: Boolean = false)

  val optionParser = new OptionParser[SignalDeskTopConfig](APP_NAME) {
    head(APP_NAME, VERSION)
    opt[Unit]("verbose").action( (_, c) =>
      c.copy(verbose = true) ).text("provide verbose output on console")

    opt[Unit]("fxml").action( (_, c) =>
      c.copy(fxml = true) ).text("launch fxml ui")
  }
  val signalDesktopConfig = optionParser.parse(parameters.raw, SignalDeskTopConfig())

  val primaryScene = if (signalDesktopConfig.exists(_.fxml)) {
    val lxmlUri = getClass.getResource("/de/m7w3/signal/recent_chats_list.fxml")
    require(lxmlUri != null, "lxmlUri not found")
    val root = FXMLView(lxmlUri, new DependenciesByType(Map.empty))
    new Scene(root)
  } else {
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

  override def stopApp(): Unit = {
    // cleanup shit
    println("bye!")
    super.stopApp()
  }
}
