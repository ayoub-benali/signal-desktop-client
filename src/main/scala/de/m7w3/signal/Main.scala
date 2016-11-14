package de.m7w3.signal

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Pos
import scalafx.scene._
import scalafx.scene.control.{Label, Button, TextField}
import scalafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import scalafx.event.ActionEvent
import java.security.Security

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
    // TODO: trigger the QR code generation
    val result = QRCodeGenerator.generate(deviceName.getText)

    println(result)
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
  Security.insertProviderAt(new org.bouncycastle.jce.provider.BouncyCastleProvider(), 1)
  stage = new PrimaryStage {
    title = "Welcome"
    scene = new Scene(800, 600) {
      val stack = new StackPane
      stack.alignment = Pos.Center
      stack.getChildren.add(Step1)
      stack.getChildren.add(Step2)
      stack.getChildren.add(Step3)
      root = stack
    }
  }
}


object QRCodeGenerator{
  import java.security.SecureRandom
  import org.whispersystems.signalservice.internal.util.Base64
  import org.whispersystems.libsignal.util.KeyHelper
  import org.whispersystems.signalservice.api.SignalServiceAccountManager
  import org.whispersystems.signalservice.internal.util.Base64
  import java.net.URLEncoder

  def generate(deviceName: String) = {
    val secret = Array[Byte](20)
    SecureRandom.getInstance("SHA1PRNG").nextBytes(secret)
    val temporaryPassword = Base64.encodeBytes(secret)
    val temporaryIdentity = KeyHelper.generateIdentityKeyPair()
    val accountManager = new SignalServiceAccountManager(Constants.URL, LocalKeyStore(), null, temporaryPassword, Constants.USER_AGENT)
    val deviceID = URLEncoder.encode(accountManager.getNewDeviceVerificationCode(), "UTF-8")
    val publicKey = URLEncoder.encode(Base64.encodeBytesWithoutPadding(temporaryIdentity.getPublicKey().serialize()), "UTF-8")
    val qrString = s"tsdevice:/?uuid=$deviceID&pub_key=$publicKey"
  }
}