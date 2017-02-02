package de.m7w3.signal

import java.security.Security
import javafx.stage.Stage

import de.m7w3.signal.Config.SignalDesktopConfig
import de.m7w3.signal.store.Addresses
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.rules.{TemporaryFolder, TestRule}
import org.junit.{Rule, Test}
import org.scalatest.junit.{AssertionsForJUnit, JUnitSuiteLike}
import org.scalatest.mock.MockitoSugar
import org.testfx.api.FxAssert._
import org.testfx.framework.junit.ApplicationTest
import org.testfx.matcher.base.NodeMatchers._

import scala.reflect.runtime.universe._
import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.stage.{Stage => SStage}
import scalafxml.core.{DependenciesByType, FXMLView}

class ChatsListTest extends ApplicationTest with JUnitSuiteLike with AssertionsForJUnit with MockitoSugar {

  val appContext: ApplicationContext = mock[InitiatedContext]

  override def start(stage: Stage): Unit = {
    val lxmlUri = getClass.getResource("/de/m7w3/signal/recent_chats_list.fxml")
    require(lxmlUri != null, "lxmlUri not found")

    val dependencies = Map[Type, Any](
      typeOf[InitiatedContext] -> appContext
    )
    val root = FXMLView(lxmlUri, new DependenciesByType(dependencies))
    val scene = new Scene(root)
    val sStage = new SStage(stage)
    sStage.setScene(scene)
    sStage.show()
  }

  @Test
  def viewChats(): Unit = {
    // just assert that stuff is there
    verifyThat("#chatsListView", isVisible)
    verifyThat("#newChatBtn", isVisible)
    clickOn("#newChatBtn")
    verifyThat("Ayoub", isVisible)
    verifyThat("Whoop II", isVisible)

  }
}
