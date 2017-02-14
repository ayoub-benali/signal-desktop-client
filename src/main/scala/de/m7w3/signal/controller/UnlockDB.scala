package de.m7w3.signal.controller

import de.m7w3.signal.ContextBuilder
import de.m7w3.signal.messages.MessageReceiver
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, Label, PasswordField}
import scalafx.scene.image.ImageView
import scalafx.scene.layout.GridPane

case class UnlockDB(context: ContextBuilder) extends GridPane {

  private val logger = LoggerFactory.getLogger(getClass)

  hgap = 10
  vgap = 20
  padding = Insets(20, 150, 10, 10)

  minHeight = 400
  minWidth = 600

  val img = new ImageView(this.getClass.getResource("/images/circle-x.png").toString)
  img.id = "errorImage"
  val button = new Button{
    text = "Unlock"
    id = "unlock"
  }
  val password = new PasswordField{
    id = "password"
    promptText = "Password"
    prefColumnCount = 10
    text.onChange {
      button.disable = text.toString().trim.isEmpty
    }
  }
  img.visible = false
  button.disable = true
  button.defaultButtonProperty().bind(password.focusedProperty())
  button.onAction = (a: ActionEvent) => {
    button.disable = true
    img.visible = false
    Future {
      context.buildWithExistingStore(password.getText()) match {
        case Success(c) =>
          MessageReceiver.initialize(c)
          Platform.runLater {
            val root = MainView.load(c)
            this.getScene.setRoot(root)
          }
        case Failure(t) =>
          img.visible = true
          button.disable = true
          logger.error("login failure: ", t)
      }
    }
  }

  add(new Label("Please enter the password to unlock the database"), 0, 0)
  add(new Label("Password:"), 0, 1)
  add(password, 1, 1)
  add(img, 2, 1)
  add(button, 0, 2)
}
