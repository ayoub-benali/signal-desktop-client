package de.m7w3.signal.controller

import scala.util.{Failure, Success}

import de.m7w3.signal.{Main, AccountHelper, ApplicationContext}
import scalafx.Includes._
import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.geometry.Pos
import scalafx.scene.{Parent, Scene}
import scalafx.scene.control.{PasswordField, Label, Button}
import scalafx.scene.layout.VBox

object UnlockDB {

  def load(context: ApplicationContext): Parent = {

    object Unlock extends VBox {
      this.alignment = Pos.Center
      private val msg = new Label("Please enter the password to unlock the database")
      private val password = new PasswordField()
      private val button = new Button("Unlock")
      button.defaultButtonProperty().bind(password.focusedProperty())
      button.onAction = (a: ActionEvent) => {
        Platform.runLater{
          context.tryLoadExistingStore(password.getText()) match {
            case Success(s) => {
              Main.store = s
              val data = s.getRegistrationData
              Main.account = AccountHelper(data.userName, data.password)
              val root = ChatsList.load(context)
              Main.stage = new PrimaryStage {
                title = "Welcome"
                scene = new Scene(root)
              }
            }
            case Failure(t) => {
              t.printStackTrace
              // TODO: show an error message
            }
          }
        }
      }
      this.children = List(msg, password, button)
    }
    Unlock
  }
}