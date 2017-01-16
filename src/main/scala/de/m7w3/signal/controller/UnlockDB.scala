package de.m7w3.signal.controller

import de.m7w3.signal.{AccountHelper, ApplicationContext, Main}

import scala.util.{Failure, Success, Try}
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.geometry.Insets
import scalafx.scene.Parent
import scalafx.scene.control.{Button, Label, PasswordField}
import scalafx.scene.image.ImageView
import scalafx.scene.layout.GridPane

object UnlockDB {

  def load(context: ApplicationContext): Parent = {

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
      Platform.runLater{
        val result: Try[Unit] = for{
          s <- context.tryLoadExistingStore(password.getText(), skipCache = true)
          data <- Try(s.getRegistrationData())
        } yield {
            // TODO: don't use Main here, use the passed-in application context
            Main.store = s
            Main.account = AccountHelper(data.userName, data.password)
            val root = ChatsList.load(context)
            button.getScene.setRoot(root)
        }
        result match {
          case Success(_) =>
            img.visible = false
            button.disable = false
          case Failure(t) =>
            img.visible = true
            button.disable = true
            t.printStackTrace()
        }
      }
    }

    val grid = new GridPane()
    grid.hgap = 10
    grid.vgap = 20
    grid.padding = Insets(20, 150, 10, 10)
    grid.add(new Label("Please enter the password to unlock the database"), 0, 0)
    grid.add(new Label("Password:"), 0, 1)
    grid.add(password, 1, 1)
    grid.add(img, 2, 1)
    grid.add(button, 0, 2)

    grid
  }
}
