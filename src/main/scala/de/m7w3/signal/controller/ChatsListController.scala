package de.m7w3.signal.controller

import java.net.URL
import java.time.LocalDateTime

import de.m7w3.signal.events.{ContactsSyncedEvent, GroupsSyncedEvent, SignalDesktopEvent, SimpleEventListener}
import de.m7w3.signal.{ApplicationContext, Logging}
import monix.eval.Task
import monix.execution.Cancelable
import monix.execution.Scheduler.Implicits.global

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

  reloadChats() // load groups, contacts initially, then register for events

  val registration: Cancelable = applicationContext.register(this)

  def reloadChats(): Unit = {
    Task {
      val groups = applicationContext.applicationStore.getGroups
      val groupEntries = groups.map(g =>
        GroupChatEntry(g, LastTextMessage(s"Hello ${g.group.name}", LocalDateTime.now().minusHours(1L)))
      )

      val contacts = applicationContext.applicationStore.getContacts
      val contactEntries = contacts.map(c =>
        ContactChatEntry(c, LastTextMessage(s"Hello ${c.getName.or(c.getNumber)}", LocalDateTime.now().minusMinutes(30L)))
      )

      val sortedEntries = (groupEntries ++ contactEntries).sorted(ChatEntry.descByLastMessageOrdering)
      chatEntries.setAll(sortedEntries.asJavaCollection)
    }.runAsync
    ()
  }

  override val handle: PartialFunction[SignalDesktopEvent, Unit] = {
    case GroupsSyncedEvent|ContactsSyncedEvent => reloadChats()
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
