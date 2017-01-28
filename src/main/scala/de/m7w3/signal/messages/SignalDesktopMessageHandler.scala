package de.m7w3.signal.messages
import java.nio.file.Files
import java.util

import de.m7w3.signal.Logging
import de.m7w3.signal.store.DBActionRunner
import de.m7w3.signal.store.model.Contacts
import org.whispersystems.signalservice.api.SignalServiceMessageReceiver
import org.whispersystems.signalservice.api.messages.multidevice._
import org.whispersystems.signalservice.api.messages.{SignalServiceAttachment, SignalServiceDataMessage, SignalServiceEnvelope}

import scala.annotation.tailrec

class SignalDesktopMessageHandler(dBActionRunner: DBActionRunner,
                                  messageReceiver: SignalServiceMessageReceiver) extends MessageHandler with Logging {

  def handleBlockedList(envelope: SignalServiceEnvelope, message: BlockedListMessage) = ???

  def handleContacts(envelope: SignalServiceEnvelope, contacts: SignalServiceAttachment): Unit = {
    val iStream = if (contacts.isStream) {
      contacts.asStream().getInputStream
    } else {
      val tmpFile = Files.createTempFile("contacts", "contacts").toFile
      tmpFile.deleteOnExit() // maybe do more, delete earlier?
      messageReceiver.retrieveAttachment(contacts.asPointer(), tmpFile)
    }
    val contactsStream = new DeviceContactsInputStream(iStream)
    processContacts(contactsStream)
  }

  @tailrec
  private def processContacts(contactsStream: DeviceContactsInputStream): Unit = {
    Option(contactsStream.read()) match {
      case Some(deviceContact: DeviceContact) =>
        logger.debug("received contact %", deviceContact)
        dBActionRunner.run(Contacts.insert(deviceContact))

        processContacts(contactsStream)
      case None =>
        // end
    }
  }

  def handleGroups(envelope: SignalServiceEnvelope, attachment: SignalServiceAttachment) = ???

  def handleRead(envelope: SignalServiceEnvelope, messages: util.List[ReadMessage]) = ???

  def handleRequest(envelope: SignalServiceEnvelope, message: RequestMessage) = ???

  def handleSent(envelope: SignalServiceEnvelope, message: SentTranscriptMessage) = ???

  override def handleSyncMessage(envelope: SignalServiceEnvelope, syncMessage: SignalServiceSyncMessage): Unit = {
    if (syncMessage.getBlockedList.isPresent) {
      handleBlockedList(envelope, syncMessage.getBlockedList.get())
    } else if (syncMessage.getContacts.isPresent) {
      handleContacts(envelope, syncMessage.getContacts.get())
    } else if (syncMessage.getGroups.isPresent) {
      handleGroups(envelope, syncMessage.getGroups.get())
    } else if (syncMessage.getRead.isPresent) {
      handleRead(envelope, syncMessage.getRead.get())
    } else if (syncMessage.getRequest.isPresent) {
      handleRequest(envelope, syncMessage.getRequest.get())
    } else if (syncMessage.getSent.isPresent) {
      handleSent(envelope, syncMessage.getSent.get())
    }
  }

  override def handleDataMessage(envelope: SignalServiceEnvelope, dataMessage: SignalServiceDataMessage): Unit = {
    error(s"cannot handle data message $dataMessage")
    ???
  }
}
