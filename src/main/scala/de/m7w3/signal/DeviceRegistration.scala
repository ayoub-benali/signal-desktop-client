package de.m7w3.signal

import java.io.ByteArrayInputStream
import net.glxn.qrgen.javase.QRCode
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.event.ActionEvent
import scalafx.geometry.Pos
import scalafx.Includes._
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.image.Image
import scalafx.scene.layout.Background
import scalafx.scene.layout.BackgroundImage
import scalafx.scene.layout.BackgroundPosition
import scalafx.scene.layout.BackgroundRepeat
import scalafx.scene.layout.BackgroundSize
import scalafx.scene.layout.StackPane
import scalafx.scene.layout.VBox

package object Registration{

  object Step1 extends VBox {
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

  object Step2 extends VBox {
    Step2.setVisible(false)
    this.alignment = Pos.Center
    private val deviceLable = new Label("Enter a name for this device:")
    val deviceName = new TextField()
    val userIdLable = new Label("Enter your device phone number: ")
    val userId = new TextField()
    // TODO: set reasonable width
    val ok = new Button("OK")
    ok.onAction = (a: ActionEvent) => {
      Step2.setVisible(false)
      Step3.setVisible(true)
      Platform.runLater{
        val url = QRCodeGenerator.generate(deviceName.getText, userId.getText)
        val outputstream = QRCode.from(url).stream
        val image = new Image(new ByteArrayInputStream(outputstream.toByteArray))
        val backgroundImage = new BackgroundImage(image, BackgroundRepeat.NoRepeat, BackgroundRepeat.NoRepeat, BackgroundPosition.Center, BackgroundSize.Default)
        Step3.qrCode.setBackground(new Background(Array(backgroundImage)))
      }
    }
    this.children = List(deviceLable, deviceName, userIdLable, userId, ok)
  }

  object Step3 extends VBox{
    Step3.setVisible(false)
    this.alignment = Pos.Center
    private val label = new Label("Open Signal on your phone and go to Settings > Devices. Click on the plus icon and scan the above QR code.")
    val qrCode = new StackPane
    qrCode.prefWidth = 300D
    qrCode.prefHeight = 300D

    this.children = List(qrCode, label)
  }

}