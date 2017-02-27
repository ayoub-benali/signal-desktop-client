package de.m7w3.signal.controller

import java.net.URL
import java.time.LocalDateTime

import de.m7w3.signal.{ApplicationContext, Logging}
import de.m7w3.signal.events.{ContactsSyncedEvent, GroupsSyncedEvent, SignalDesktopEvent, SimpleEventListener}
import monix.execution.Cancelable

import scala.collection.JavaConverters.asJavaCollectionConverter
import scala.reflect.runtime.universe.{Type, typeOf}
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.scene.control.{Button, ListCell, ListView}
import scalafxml.core.macros.sfxml
import scalafxml.core.{DependenciesByType, FXMLView}


@sfxml
class ChatsListController(
    val newChatBtn: Button,
    val chatsListView: ListView[ChatEntry],
    applicationContext: ApplicationContext) extends SimpleEventListener {

  val chatEntries: ObservableBuffer[ChatEntry] = ObservableBuffer.empty
  chatsListView.items = chatEntries

  chatsListView.cellFactory = (lv: ListView[ChatEntry]) => {
    new ChatEntryListCell(applicationContext)
  }

  reloadGroups() // load groups initially, then register for events

  val registration: Cancelable = applicationContext.register(this)

  def reloadGroups(): Unit = {
    val groups = applicationContext.applicationStore.getGroups
    chatEntries.setAll(groups.map(
      group => ChatEntry(
        // TODO for groups without name use the id
        group.group.name.getOrElse(group.members.map(_.name).mkString(",")),
        LocalDateTime.now().minusHours(1L), // TODO: get ts of the latest message in that group
        LastTextMessage("boom") // TODO: get latest group message
      )
    ).asJavaCollection)
    ()
  }

  override val handle: PartialFunction[SignalDesktopEvent, Unit] = {
    case GroupsSyncedEvent => reloadGroups()
    case ContactsSyncedEvent =>
      // TODO: reload and display contacts
      //reloadGroups()
  }

  def onNewChat(event: ActionEvent): Unit = {
    println("new chat pressed")

  }

  newChatBtn.onAction = this.onNewChat _
}


class ChatEntryListCell(appCtx: ApplicationContext) extends ListCell[ChatEntry] with Logging {

  val fxmlUri: URL = getClass.getResource("/de/m7w3/signal/chat_entry.fxml")
  val baseDependencies: Map[Type, Any] = Map[Type, Any](
    typeOf[ApplicationContext] -> appCtx
  )

  item.onChange { (observable, oldValue, newValue) =>
    if (newValue != null) {
      logger.debug(s"item.onChange: $newValue")
      val dependencies = baseDependencies + (typeOf[ChatEntry] -> newValue)
      Platform.runLater {
        graphic = FXMLView(fxmlUri, new DependenciesByType(dependencies))
      }
    } else {
      // item is reused, ui should be cleaned
      graphic = null
    }
  }
}
