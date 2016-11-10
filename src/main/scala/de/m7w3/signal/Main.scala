package de.m7w3.signal

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Insets
import scalafx.scene._
import scalafx.scene.control.{Label, Button}
import scalafx.scene.layout.StackPane
import javafx.scene.layout.VBox

object Step1 extends VBox{
  val hello = new Label("Hello")
  val appRequired = new Label("You must have Signal for Android for using this client")
  val haveApp = new Button("I have Signal for Android")
  // haveApp.onAction

  getChildren.add(hello)
  getChildren.add(appRequired)
  getChildren.add(haveApp)
}

object Main extends JFXApp {

  stage = new PrimaryStage {
    title = "Welcome"
    scene = new Scene(800, 600) {
      // content = List(button)
      val stack = new StackPane
      // step1.children += List()
      stack.getChildren.add(Step1)
      root = stack
    }
  }
}