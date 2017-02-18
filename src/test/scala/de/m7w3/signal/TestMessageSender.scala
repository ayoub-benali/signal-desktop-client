package de.m7w3.signal

import de.m7w3.signal.messages.MessageSender
import org.whispersystems.signalservice.api.messages.multidevice.SignalServiceSyncMessage

import scala.collection.mutable

object TestMessageSender extends MessageSender {

  val queue = mutable.Queue.empty[SignalServiceSyncMessage]

  override def send(message: SignalServiceSyncMessage): Unit = {
    queue += message
  }
}
