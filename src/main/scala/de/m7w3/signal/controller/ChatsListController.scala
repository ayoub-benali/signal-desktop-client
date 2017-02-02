package de.m7w3.signal.controller

import java.net.URL
import java.time.LocalDateTime

import de.m7w3.signal.ApplicationContext

import scala.reflect.runtime.universe.{Type, typeOf}
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.scene.control.{Button, ListCell, ListView}
import scalafxml.core.macros.sfxml
import scalafxml.core.{DependenciesByType, FXMLView}


@sfxml
class ChatsListController(
    val newChatBtn: Button,
    val chatsListView: ListView[ChatEntry],
    applicationContext: ApplicationContext) {

  val chatEntries = ObservableBuffer(
    ChatEntry("Ayoub", LocalDateTime.now().minusHours(1L), LastTextMessage("you are awesome")),
    ChatEntry("Whoop II", LocalDateTime.now().minusDays(1L), LastTextMessage("we are awesome")),
    ChatEntry("Me, Myself, I", LocalDateTime.now().minusDays(1L), MediaMessage)
  )
  chatsListView.cellFactory = (lv: ListView[ChatEntry]) => {
    new ChatEntryListCell(applicationContext)
  }
  chatsListView.setItems(chatEntries)

  def onNewChat(event: ActionEvent): Unit = {
    println("new chat pressed")

    // show contacts search list
    /*
    val btn = new Button("switch")
    btn.id = "switch"
    val scene = newChatBtn.getScene
    val storeRoot = scene.getRoot
    btn.onAction = (a: ActionEvent) => {
      scene.setRoot(storeRoot)
    }
    val pane = new AnchorPane()
    pane.children.add(btn)
    scene.setRoot(pane)
    */
  }

  newChatBtn.onAction = this.onNewChat _
}


class ChatEntryListCell(appCtx: ApplicationContext) extends ListCell[ChatEntry] {

  val fxmlUri: URL = getClass.getResource("/de/m7w3/signal/chat_entry.fxml")
  val baseDependencies: Map[Type, Any] = Map[Type, Any](
    typeOf[ApplicationContext] -> appCtx
  )
  item.onChange { (observable, oldValue, newValue) =>
    if (newValue != null) {
      val dependencies = baseDependencies + (typeOf[ChatEntry] -> newValue)
      graphic = FXMLView(fxmlUri, new DependenciesByType(dependencies))
    }
  }
}
