package de.m7w3.signal.controller

import de.m7w3.signal.{ApplicationContext, AccountHelper}
import scalafx.scene.Parent
import scalafx.scene.layout.VBox
import scalafx.geometry.Pos
import scalafx.scene.control.Label
import scalafx.scene.control.Button
import scalafx.event.ActionEvent
import scalafx.scene.control.PasswordField
import scala.util.Success
import scalafx.Includes._
import de.m7w3.signal.Main


object UnlockDB {

  def load(context: ApplicationContext): Parent = {

    object Unlock extends VBox {
      this.alignment = Pos.Center
      private val msg = new Label("Please enter the password to unlock the database")
      private val password = new PasswordField()
      private val button = new Button("Unlock")
      button.onAction = (a: ActionEvent) => {
        context.tryLoadExistingStore(password.getText()) match {
          case Success(s) => {
            Main.store = s
            val data = s.getRegistrationData
            Main.account = AccountHelper(data.userName, data.password)
          }
          case _ => // TODO: show an error message
        }
      }
      this.children = List(msg, button)
    }

    Unlock
  }
}