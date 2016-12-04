package de.m7w3.signal.controller

import de.m7w3.signal.{Main, AccountHelper, ApplicationContext}
import scalafx.Includes._
import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.scene.{Parent, Scene}
import scalafx.scene.control.{PasswordField, Label, Button}
import scala.util.Try
import scalafx.scene.image.ImageView
import scalafx.scene.layout.GridPane
import scalafx.geometry.Insets

object UnlockDB {

  def load(context: ApplicationContext): Parent = {

    val img =  new ImageView(this.getClass().getResource("/images/circle-x.png").toString())
    val button = new Button("Unlock")
    val password = new PasswordField{
      promptText = "Password"
      prefColumnCount = 10
      text.onChange{button.disable = text.toString().trim.isEmpty}
    }
    img.visible = false
    button.disable = true
    button.defaultButtonProperty().bind(password.focusedProperty())
    button.onAction = (a: ActionEvent) => {
      button.disable = true
      Platform.runLater{
        val result: Try[Unit] = for{
          s <- context.tryLoadExistingStore(password.getText(), skipCache = true)
          data <- Try(s.getRegistrationData)
        } yield{
            Main.store = s
            val data = s.getRegistrationData
            Main.account = AccountHelper(data.userName, data.password)
            val root = ChatsList.load(context)
            Main.stage = new PrimaryStage {
              title = "Welcome"
              scene = new Scene(root)
          }
        }
        result.failed.foreach(t => {
            img.visible = true
            button.disable = true
            t.printStackTrace()
          }
        )
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