package de.m7w3.signal

import javafx.stage.Stage

import de.m7w3.signal.account.AccountHelper
import de.m7w3.signal.events.GroupsSyncedEvent
import de.m7w3.signal.store.model.{Group, GroupMember, GroupWithMembers}
import de.m7w3.signal.store.{DBActionRunner, SignalDesktopApplicationStore, SignalDesktopProtocolStore}
import org.junit.Test
import org.mockito.Mockito
import org.scalatest.concurrent.Eventually
import org.scalatest.junit.{AssertionsForJUnit, JUnitSuiteLike}
import org.scalatest.mockito.MockitoSugar
import org.testfx.api.FxAssert._
import org.testfx.framework.junit.ApplicationTest
import org.testfx.matcher.base.NodeMatchers._
import org.testfx.util.WaitForAsyncUtils

import scala.reflect.runtime.universe._
import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.stage.{Stage => SStage}
import scalafxml.core.{DependenciesByType, FXMLView}

class ChatsListTest extends ApplicationTest
  with JUnitSuiteLike
  with AssertionsForJUnit
  with MockitoSugar
  with Eventually {

  val groups: Seq[GroupWithMembers] = Seq(
    GroupWithMembers(
      Group(1, Array[Byte](1, 2, 3), Some("group1"), None, true),
      Seq(
        GroupMember("member1", 1),
        GroupMember("member2", 2)
      )
    ),
    GroupWithMembers(
      Group(2, Array[Byte](1, 2, 3, 4), Some("group2"), None, true),
      Seq(
        GroupMember("member2", 1),
        GroupMember("member3", 2)
      )
    ),
    GroupWithMembers(
      Group(3, Array[Byte](1, 2, 3, 4, 5), Some("group3"), None, false),
      Seq(
        GroupMember("member2", 1),
        GroupMember("member3", 2),
        GroupMember("member4", 3)
      )
    )
  )
  val appStore: SignalDesktopApplicationStore = mock[SignalDesktopApplicationStore]
  val appContext: ApplicationContext = new ApplicationContext(
    TestMessageSender,
    mock[AccountHelper],
    mock[DBActionRunner],
    mock[SignalDesktopProtocolStore],
    appStore
  )

  override def start(stage: Stage): Unit = {
    Mockito.when(appStore.getGroups).thenReturn(groups.slice(0,2))
    Mockito.when(appStore.getContacts).thenReturn(Seq.empty)
    val lxmlUri = getClass.getResource("/de/m7w3/signal/recent_chats_list.fxml")
    require(lxmlUri != null, "lxmlUri not found")

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
  def showUpdateGroupsOnGroupSync(): Unit = {

    // just assert that stuff is there
    verifyThat("#chatsListView", isVisible)
    verifyThat("#newChatBtn", isVisible)
    clickOn("#newChatBtn")

    WaitForAsyncUtils.waitForFxEvents()

    verifyThat("group1", isVisible)
    verifyThat("group2", isVisible)

    Mockito.when(appStore.getGroups).thenReturn(groups)
    appContext.publishEvent(GroupsSyncedEvent)

    WaitForAsyncUtils.waitForFxEvents()

    verifyThat("group1", isVisible)
    verifyThat("group2", isVisible)
    verifyThat("group3", isVisible)
  }
}
