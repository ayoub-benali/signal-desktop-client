package de.m7w3.signal

import java.io.ByteArrayInputStream

import scalafx.Includes._
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.geometry.Pos
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.image.Image
import scalafx.scene.layout.{VBox, StackPane, BackgroundSize, BackgroundRepeat, BackgroundPosition, BackgroundImage, Background}
import scalafx.scene.Parent

object DeviceRegistration{

  def load(context: ApplicationContext): Parent = {
    object Step1 extends VBox {
      this.alignment = Pos.Center
      private val hello = new Label("Hello")
      private val appRequired = new Label("You must have Signal for Android for using this client")
      private val haveApp = new Button("I have Signal for Android")
      haveApp.onAction = (a: ActionEvent) => {
        this.setVisible(false)
        Step2.setVisible(true)
      }
      this.children = List(hello, appRequired, haveApp)
    }

    object Step2 extends VBox {
      this.setVisible(false)
      this.alignment = Pos.Center
      private val deviceLable = new Label("Enter a name for this device:")
      val deviceName = new TextField()
      val userIdLable = new Label("Enter your device phone number: ")
      val userId = new TextField()
      val dbPasswordLable = new Label("Enter a password to encrypt the database")
      val dbPassword = new TextField()
      // TODO: set reasonable width
      val ok = new Button("OK")
      ok.onAction = (a: ActionEvent) => {
        this.setVisible(false)
        Step3.setVisible(true)
        // TODO: do this in a better way
        Main.account = AccountHelper(userId.getText())
        Platform.runLater{
          val outputstream = QRCodeGenerator.generate(() => Main.account.getNewDeviceURL)
          val image = new Image(new ByteArrayInputStream(outputstream.toByteArray), 600D, 600D, true, true)
          val backgroundImage = new BackgroundImage(image, BackgroundRepeat.NoRepeat, BackgroundRepeat.NoRepeat, BackgroundPosition.Center, BackgroundSize.Default)
          Step3.qrCode.setBackground(new Background(Array(backgroundImage)))
        }
      }
      this.children = List(deviceLable, deviceName, userIdLable, userId, ok)
    }

    object Step3 extends VBox{
      this.setVisible(false)
      this.alignment = Pos.Center
      private val label = new Label("Open Signal on your phone and go to Settings > Devices. Click on the plus icon and scan the above QR code.")
      val qrCode = new StackPane
      qrCode.prefWidth = 600D
      qrCode.prefHeight = 600D
      val finish = new Button("Finish")
      finish.onAction = (a: ActionEvent) => {
        Platform.runLater{
          Main.store = context.createNewProtocolStore(Step2.dbPassword.getText())
          Main.account.finishDeviceLink(Step2.deviceName.getText(), Main.store)
        }
      }

      this.children = List(qrCode, label, finish)
    }

    val stack = new StackPane
    stack.alignment = Pos.Center
    stack.children = List(Step1, Step2, Step3)
    stack
  }
}