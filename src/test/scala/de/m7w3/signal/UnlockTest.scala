package de.m7w3.signal

import javafx.stage.Stage

import org.junit.{Rule, Test}
import org.scalatest.junit.{AssertionsForJUnit, JUnitSuiteLike}
import org.testfx.api.FxAssert._
import org.testfx.framework.junit.ApplicationTest
import org.testfx.matcher.base.NodeMatchers._
import org.testfx.util.WaitForAsyncUtils

import scalafx.scene.Scene
import scalafx.stage.{Stage => SStage}
import de.m7w3.signal.controller.UnlockDB

class UnlockTest extends ApplicationTest with JUnitSuiteLike with AssertionsForJUnit {

  val internalApplicationContext = new ApplicationContextRule

  @Rule
  def applicationContext: ApplicationContextRule = internalApplicationContext

  override def start(stage: Stage): Unit = {
    val root = UnlockDB.load(internalApplicationContext.get())
    val scene = new Scene(root)
    val sStage = new SStage(stage)
    sStage.setScene(scene)
    sStage.show()
  }

  @Test
  def viewChats(): Unit = {
    verifyThat("#unlock", isVisible())
    verifyThat("#unlock", isDisabled())
    verifyThat("#errorImage", isInvisible())
    clickOn("#password").write("whatever")
    clickOn("#unlock")
    WaitForAsyncUtils.waitForFxEvents()
    verifyThat("#errorImage", isVisible())
  }
}
