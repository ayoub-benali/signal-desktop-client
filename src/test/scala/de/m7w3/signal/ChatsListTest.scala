package de.m7w3.signal

import javafx.stage.Stage

import de.m7w3.signal.Config.SignalDesktopConfig
import org.junit.rules.{TemporaryFolder, TestRule}
import org.junit.{Rule, Test}
import org.scalatest.junit.{AssertionsForJUnit, JUnitSuiteLike}
import org.testfx.api.FxAssert._
import org.testfx.framework.junit.ApplicationTest
import org.testfx.matcher.base.NodeMatchers._
import org.testfx.util.WaitForAsyncUtils

import scala.reflect.runtime.universe._
import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.stage.{Stage => SStage}
import scalafxml.core.{DependenciesByType, FXMLView}

class ChatsListTest extends ApplicationTest with JUnitSuiteLike with AssertionsForJUnit {


  val _profileDir = new TemporaryFolder()

  @Rule
  def profileDir: TestRule = _profileDir

  override def start(stage: Stage): Unit = {
    val lxmlUri = getClass.getResource("/de/m7w3/signal/recent_chats_list.fxml")
    require(lxmlUri != null, "lxmlUri not found")

    val config = SignalDesktopConfig(profileDir = _profileDir.newFolder("profileDir"))

    val appContext = ApplicationContext(config)

    val dependencies = Map[Type, Any](
      typeOf[ApplicationContext] -> appContext
    )

    val root = FXMLView(lxmlUri, new DependenciesByType(dependencies))
    val scene = new Scene(root)
    val sStage = new SStage(stage)
    sStage.setScene(scene)
    sStage.show()
  }

  @Test
  def viewChats(): Unit = {

    verifyThat("#chatsList", isVisible)
    verifyThat("#switch", isNull)

    clickOn("#newChatBtn")
    WaitForAsyncUtils.waitForFxEvents()
    verifyThat("#switch", isVisible)
    verifyThat("#chatsList", isNull)

    clickOn("#switch")
    WaitForAsyncUtils.waitForFxEvents()
    verifyThat("#chatsList", isVisible)
    verifyThat("#switch", isNull)
  }
}
