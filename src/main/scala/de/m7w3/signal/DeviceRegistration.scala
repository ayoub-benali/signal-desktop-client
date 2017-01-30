package de.m7w3.signal

import java.io.ByteArrayInputStream

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import de.m7w3.signal.controller.ChatsList
import scalafx.Includes._
import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.geometry.Pos
import scalafx.scene.{Parent, Scene}
import scalafx.scene.control.{Button, Label, PasswordField, TextField}
import scalafx.scene.image.Image
import scalafx.scene.layout.{Background, BackgroundImage, BackgroundPosition, BackgroundRepeat, BackgroundSize, StackPane, VBox, GridPane}
import scalafx.geometry.Insets
import java.net.InetAddress
import scala.util.Success

object DeviceRegistration{

  def load(context: InitialContext): Parent = {

    case class Step1() extends VBox {
      alignment = Pos.Center
      private val hello = new Label("Hello")
      private val appRequired = new Label("You must have Signal for Android for using this client")
      private val haveApp = new Button("I have Signal for Android")
      haveApp.defaultButtonProperty().bind(haveApp.focusedProperty())
      haveApp.onAction = (a: ActionEvent) => {
        this.getScene().setRoot(Step2())
      }
      this.children = List(hello, appRequired, haveApp)
    }

    case class Step2() extends GridPane {
      val ok = new Button("Register")
      ok.disable = true

      val hostname = scala.util.Try(InetAddress.getLocalHost()).map(_.getHostName())

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
        val password = Util.getSecret(20)
        val account = AccountHelper(userId.getText(), password)
        //TODO: show a progress bar while the future is not complete
        Platform.runLater{
          println("pressed")
          Future(QRCodeGenerator.generate(() => account.getNewDeviceURL)).map(outputstream => {
            val image = new Image(new ByteArrayInputStream(outputstream.toByteArray), 300D, 300D, true, true)
            val step = Step3(account, dbPassword.getText(), deviceName.getText())
            val backgroundImage = new BackgroundImage(image, BackgroundRepeat.NoRepeat, BackgroundRepeat.NoRepeat, BackgroundPosition.Center, BackgroundSize.Default)
            step.qrCode.setBackground(new Background(Array(backgroundImage)))
            step.finish.disable = false
            this.getScene().setRoot(step)
          })
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

    case class Step3(account: AccountHelper, password: String, deviceName: String) extends GridPane{

      val qrCode = new StackPane
      qrCode.prefWidth = 300D
      qrCode.prefHeight = 300D
      val finish = new Button("Finish")
      finish.disable = true
      finish.onAction = (a: ActionEvent) => Platform.runLater{
        // TODO show a progress bar
        val store = context.createNewProtocolStore(password)
        account.finishDeviceLink(deviceName, store)
        val initiatedContext = ApplicationContextBuilder.setStore(store)
        .setAccount(account)
        .setInitialContext(context)
        .build()

        initiatedContext match {
          case Success(c) => {
            val root = ChatsList.load(c)
            Main.stage = new PrimaryStage {
              title = "Welcome"
              scene = new Scene(root)
            }
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