package de.m7w3.signal.store.model

case class IncomingTextMessage(
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