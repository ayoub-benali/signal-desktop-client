package de.m7w3.signal

import javafx.stage.Stage

import org.junit.Test
import org.scalatest.junit.{AssertionsForJUnit, JUnitSuiteLike}
import org.testfx.api.FxAssert._
import org.testfx.framework.junit.ApplicationTest
import org.testfx.matcher.base.NodeMatchers._
import org.testfx.util.WaitForAsyncUtils

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.stage.{Stage => SStage}
import de.m7w3.signal.controller.UnlockDB

object UnlockTest extends ApplicationTest with JUnitSuiteLike with AssertionsForJUnit with ApplicationContextTest{

  override def start(stage: Stage): Unit = {
    val root = UnlockDB.load(context)
    val scene = new Scene(root)
    val sStage = new SStage(stage)
    sStage.setScene(scene)
    sStage.show()
  }

  @Test
  def viewChats(): Unit = {

    // verifyThat("#chatsList", isVisible)
    // verifyThat("#switch", isNull)

    // clickOn("#newChatBtn")
    // WaitForAsyncUtils.waitForFxEvents()
    // verifyThat("#switch", isVisible)
    // verifyThat("#chatsList", isNull)

    // clickOn("#switch")
    // WaitForAsyncUtils.waitForFxEvents()
    // verifyThat("#chatsList", isVisible)
    // verifyThat("#switch", isNull)
  }
}
