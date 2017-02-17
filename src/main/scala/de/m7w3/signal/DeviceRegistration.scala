package de.m7w3.signal

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.net.InetAddress

import account.AccountHelper
import de.m7w3.signal.controller.MainView
import de.m7w3.signal.messages.MessageReceiver
import monix.execution.atomic.Atomic

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Parent
import scalafx.scene.control.{Button, Label, PasswordField, TextField}
import scalafx.scene.image.Image
import scalafx.scene.layout._

object DeviceRegistration{

  def load(contextBuilder: ContextBuilder, appContextRef: Atomic[Option[ApplicationContext]]): Parent = {

    case class Step1() extends VBox with Logging {
      alignment = Pos.Center
      minHeight = 400
      minWidth = 600

      private val hello = new Label("Hello")
      private val appRequired = new Label("You must have Signal for Android for using this client")
      private val haveApp = new Button("I have Signal for Android")
      haveApp.defaultButtonProperty().bind(haveApp.focusedProperty())
      haveApp.onAction = (a: ActionEvent) => {
        logger.debug("loading Step 2 UI...")
        this.getScene.setRoot(Step2())
      }
      this.children = List(hello, appRequired, haveApp)
    }

    case class Step2() extends GridPane with Logging {
      val ok = new Button("Register")
      ok.disable = true

      val hostname = scala.util.Try(InetAddress.getLocalHost).map(_.getHostName())

      val deviceName = new TextField{
        promptText = hostname.map(h => s"signal client on $h").getOrElse("")
        prefColumnCount = 10
        text.onChange{ok.disable = oneFieldEmpty()}
      }

      val userId = new TextField{
        promptText = "+49123456789"
        prefColumnCount = 10
        text.onChange{ok.disable = oneFieldEmpty()}
      }

      val dbPassword = new PasswordField{
        promptText = "**********"
        prefColumnCount = 10
        text.onChange{ok.disable = oneFieldEmpty()}
      }

      def oneFieldEmpty(): Boolean = deviceName.text.value.trim.isEmpty || userId.text.value.trim.isEmpty || dbPassword.text.value.trim.isEmpty

      ok.onAction = (a: ActionEvent) => {
        Future {
          val password = Util.getSecret(20)
          val account = AccountHelper(userId.getText(), password)
          //TODO: show a progress bar while the future is not complete
          val generateUrl: () => String = () => account.generateNewDeviceURL()
          val outputStream = QRCodeGenerator.generate(generateUrl)
          logger.debug("QR code created from new deviceURL")
          Platform.runLater {
            logger.debug("launching Step 3 UI...")
            val step = Step3(account, dbPassword.getText(), deviceName.getText(), outputStream)
            this.getScene.setRoot(step)
          }
        }
      }
      ok.defaultButtonProperty().bind(dbPassword.focusedProperty())

      alignment = Pos.Center
      hgap = 10
      vgap = 20
      padding = Insets(20, 150, 10, 10)
      add(new Label("Device name:"), 0, 0)
      add(deviceName, 1, 0)
      add(new Label("Phone number: "), 0, 1)
      add(userId, 1, 1)
      add(new Label("Password:"), 0, 2)
      add(dbPassword, 1, 2)
      add(ok, 0, 3)
    }

    case class Step3(account: AccountHelper, password: String, deviceName: String, outputStream: ByteArrayOutputStream) extends GridPane{
      val image = new Image(new ByteArrayInputStream(outputStream.toByteArray), 300D, 300D, true, true)
      val backgroundImage = new BackgroundImage(image, BackgroundRepeat.NoRepeat, BackgroundRepeat.NoRepeat, BackgroundPosition.Center, BackgroundSize.Default)

      val qrCode = new StackPane
      qrCode.prefWidth = 300D
      qrCode.prefHeight = 300D
      qrCode.setBackground(new Background(Array(backgroundImage)))

      val finish = new Button("Finish")
      finish.onAction = (a: ActionEvent) => Future {
        // TODO show a progress bar
        contextBuilder.buildWithNewStore(account, password) match {
          case Success(c) =>
            appContextRef.set(Some(c))
            account.finishDeviceLink(deviceName, c.protocolStore)
            MessageReceiver.initialize(c)
            Platform.runLater {
              val root = MainView.load(c)
              this.getScene.setRoot(root)
            }
          case _ => {
            //TODO: log exception
          }
        }
      }
      finish.defaultButtonProperty().bind(finish.focusedProperty())

      alignment = Pos.Center
      hgap = 10
      vgap = 20
      padding = Insets(20, 150, 10, 10)
      add(new Label("Open Signal on your phone and go to Settings > Devices. Click on the plus icon and scan the above QR code."), 0, 0)
      add(qrCode, 0, 1)
      add(finish, 0, 2)
    }

    Step1()
  }
}
