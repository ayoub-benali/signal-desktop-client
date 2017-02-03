package de.m7w3.signal

import java.util.concurrent.TimeUnit
import javafx.scene.Node
import javafx.stage.Stage

import de.m7w3.signal.controller.UnlockDB
import de.m7w3.signal.exceptions.DatabaseDoesNotExistException
import org.junit.Test
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalatest.junit.{AssertionsForJUnit, JUnitSuiteLike}
import org.scalatest.mockito.MockitoSugar
import org.testfx.api.FxAssert._
import org.testfx.framework.junit.ApplicationTest
import org.testfx.matcher.base.NodeMatchers._
import org.testfx.util.WaitForAsyncUtils

import scala.util.{Failure, Success}
import scalafx.scene.Scene
import scalafx.stage.{Stage => SStage}

class UnlockTest extends ApplicationTest with JUnitSuiteLike with AssertionsForJUnit with MockitoSugar {

  val password = "abc"
  val wrong = "whatever"
  val contextBuilder = mock[ContextBuilder]
  val initiatedContext = mock[InitiatedContext]
  Mockito.when(contextBuilder.buildWithExistingStore(ArgumentMatchers.eq(password))).thenReturn(Success(initiatedContext))
  Mockito.when(contextBuilder.buildWithExistingStore(ArgumentMatchers.eq(wrong))).thenReturn(Failure(
    new DatabaseDoesNotExistException("nope", null)
  ))

  override def start(stage: Stage): Unit = {
    val root = UnlockDB(contextBuilder)
    val scene = new Scene(root)
    val sStage = new SStage(stage)
    sStage.setScene(scene)
    sStage.show()
  }

  @Test
  def wrongPassword(): Unit = {
    verifyThat("#unlock", isVisible)
    verifyThat("#unlock", isDisabled)
    verifyThat("#errorImage", isInvisible)
    clickOn("#password").write(wrong)
    verifyThat("#unlock", isEnabled)
    clickOn("#unlock")

    WaitForAsyncUtils.waitFor(10L, TimeUnit.SECONDS, lookup("#errorImage").query[Node]().visibleProperty())
    verifyThat("#errorImage", isVisible)
    verifyThat("#unlock", isDisabled)
  }

  @Test
  def rightPassword(): Unit = {

    verifyThat("#unlock", isVisible)
    verifyThat("#unlock", isDisabled)
    verifyThat("#errorImage", isInvisible)

    clickOn("#password").write(password)
    verifyThat("#unlock", isEnabled)
    clickOn("#unlock")
    WaitForAsyncUtils.waitForFxEvents()
  }
}
