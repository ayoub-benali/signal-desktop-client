package de.m7w3.signal.store.model

import slick.driver.H2Driver.api._

case class TextMessage(
  val message: String,
  val sender: String,
  val senderDeviceId: Int,
  // val protocol: Int,  // seem to be set to 31337
  // val serviceCenterAddress: String, // "GCM"
  // val replyPathPresent: Boolean, // true
  // val pseudoSubject: String, // ""
  val sentTimestampMillis: Long,
  val groupId: Option[Array[Byte]]
)

class TextMessages(tag: Tag) extends Table[Group](tag, "TEXTMESSAGES") {
  def pk = column[Int]("pk", O.PrimaryKey)
  def message = column[String]("MESSAGE")
  def sender = column[String]("SENDER")
  def senderDeviceId = column[Int]("SENDERDEVICEID")
  def sentTimestampMillis = column[Long]("SENTTIMESTAMPMILLIS")
  def groupId = column[Option[Array[Byte]]]("GROUPID")

  override def *  = (pk, message, sender, senderDeviceId, sentTimestampMillis, groupId) <> (TextMessage.tupled, TextMessage.unapply)
}

object TextMessages{
  val TextMessages = TableQuery[TextMessages]
}