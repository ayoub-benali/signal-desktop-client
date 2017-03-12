package de.m7w3.signal.controller

import java.io.{ByteArrayInputStream, InputStream}
import java.time.{LocalDateTime, ZoneOffset}

import de.m7w3.signal.store.model.{AttachmentHelpers, GroupWithMembers}
import org.whispersystems.signalservice.api.messages.multidevice.DeviceContact

sealed trait LastMessage {
  def timestamp: LocalDateTime
}
case class LastTextMessage(msg: String, timestamp: LocalDateTime) extends LastMessage {
  override def toString: String = msg
}
case class MediaMessage(timestamp: LocalDateTime) extends LastMessage {
  override def toString: String = "Media Message"
}

sealed trait ChatEntry {
  def name: String
  def lastMessage: LastMessage
  def avatar: Option[InputStream]
}

case class GroupChatEntry(group: GroupWithMembers, lastMessage: LastMessage) extends ChatEntry {
  override val name: String = group.group.name.getOrElse(group.members.map(_.name).mkString(","))
  override val avatar: Option[InputStream] = group.group.avatar.map(new ByteArrayInputStream(_))
}

case class ContactChatEntry(contact: DeviceContact, lastMessage: LastMessage) extends ChatEntry {
  override val name: String = contact.getName.or(contact.getNumber)

  override val avatar: Option[InputStream] = {
    val avatarBytes = AttachmentHelpers.streamToArray(contact.getAvatar)
    avatarBytes.map(new ByteArrayInputStream(_))
  }
}

object ChatEntry {
  val descByLastMessageOrdering: Ordering[ChatEntry] = Ordering.by(
    entry => entry.lastMessage.timestamp.toEpochSecond(ZoneOffset.UTC) * -1 -> entry.name)
}
