package de.m7w3.signal.controller

import java.time.format.DateTimeFormatter
import java.time.{Duration, LocalDateTime}

import de.m7w3.signal.ApplicationContext

import scalafx.scene.control.Label
import scalafx.scene.image.ImageView
import scalafxml.core.macros.sfxml

sealed trait LastMessage
case class LastTextMessage(msg: String) extends LastMessage {
  override def toString: String = msg
}
case object MediaMessage extends LastMessage {
  override def toString: String = "Media Message"
}

case class ChatEntry(name: String, lastMessageDate: LocalDateTime, lastMessage: LastMessage)

@sfxml
class ChatEntryController(chatEntryImageView: ImageView,
                          chatEntryNameLabel: Label,
                          chatEntryLastMessageLabel: Label,
                          chatEntry: ChatEntry,
                          applicationContext: ApplicationContext) {


  def initializeView(): Unit = {
    chatEntryNameLabel.text = chatEntry.name
    val fromNow = Duration.between(chatEntry.lastMessageDate, LocalDateTime.now()).abs()
    val fromNowMillis = fromNow.toMillis
    val timeLabel = if (fromNowMillis > Duration.ofDays(1L).toMillis) {
      s"${chatEntry.lastMessageDate.format(ChatEntryFormatter.DAY_MONTH_FMT)}"
    } else if (fromNowMillis > Duration.ofHours(1L).toMillis) {
      s"${fromNow.toHours}h ago"
    } else {
      s"${fromNow.toMinutes}m ago"
    }
    val msg = truncateTo(chatEntry.lastMessage.toString, 20)
    chatEntryLastMessageLabel.text = s"($timeLabel) $msg"
  }

  private def truncateTo(s: String, num: Int) = {
    val sLen = s.length
    val truncation = if (sLen > num) "..." else ""
    s.substring(0, Math.min(num, sLen)) + truncation
  }

  initializeView()
}


object ChatEntryFormatter {
  val DAY_MONTH_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("d.M.")
}
