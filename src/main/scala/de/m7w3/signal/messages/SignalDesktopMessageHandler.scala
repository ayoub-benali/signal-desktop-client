package de.m7w3.signal.messages
import java.nio.file.Files
import java.util

import de.m7w3.signal.Logging
import de.m7w3.signal.store.SignalDesktopApplicationStore
import org.whispersystems.signalservice.api.SignalServiceMessageReceiver
import org.whispersystems.signalservice.api.messages.multidevice._
import org.whispersystems.signalservice.api.messages.{SignalServiceAttachment, SignalServiceDataMessage, SignalServiceEnvelope}

import scala.annotation.tailrec
import scala.collection.JavaConverters.collectionAsScalaIterableConverter


class SignalDesktopMessageHandler(signalDesktopApplicationStore: SignalDesktopApplicationStore,
                                  messageReceiver: SignalServiceMessageReceiver) extends MessageHandler with Logging {

  def handleBlockedList(envelope: SignalServiceEnvelope, message: BlockedListMessage) = {
    logger.debug(s"got blockedlist message [{}]", message.getNumbers.asScala.mkString(", "))
  }

  def handleContacts(envelope: SignalServiceEnvelope, contacts: SignalServiceAttachment): Unit = {
    logger.debug("received contacts sync message")
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
        logger.debug("received contact {}", deviceContact.getNumber)
        signalDesktopApplicationStore.saveContact(deviceContact)
        processContacts(contactsStream)
      case None =>
        // end
    }
  }

  def handleGroups(envelope: SignalServiceEnvelope, attachment: SignalServiceAttachment): Unit = {
    logger.debug("received groups sync message")
    val iStream = if (attachment.isStream) {
      attachment.asStream().getInputStream
    } else {
      // TODO: put into local data folder
      val tmpFile = Files.createTempFile("groups", "groups").toFile
      tmpFile.deleteOnExit() // maybe do more, delete earlier?
      messageReceiver.retrieveAttachment(attachment.asPointer(), tmpFile)
    }
    val groupsStream = new DeviceGroupsInputStream(iStream)
    processGroups(groupsStream)
  }

  def processGroups(groupsStream: DeviceGroupsInputStream): Unit = {
    Option(groupsStream.read()) match {
      case Some(deviceGroup: DeviceGroup) =>
        logger.debug("received group {}", deviceGroup.getName)
        signalDesktopApplicationStore.saveGroup(deviceGroup)
        processGroups(groupsStream)
      case None =>
        // end
    }
  }

  def handleRead(envelope: SignalServiceEnvelope, messages: util.List[ReadMessage]): Unit = {
    logger.debug("received read message from [{}]", messages.asScala.map(m => m.getSender).mkString(", "))
  }

  def handleRequest(envelope: SignalServiceEnvelope, message: RequestMessage): Unit = {
    logger.debug("received request message [{}]", message.getRequest)
  }

  def handleSent(envelope: SignalServiceEnvelope, message: SentTranscriptMessage): Unit = {
    logger.debug("received sent message [{}: {}]", message.getMessage, message.getTimestamp)
  }

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
    logger.debug(s"received data message [${dataMessage.getBody} ${dataMessage.getGroupInfo}]")
  }
}
