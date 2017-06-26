package de.m7w3.signal.messages
import java.nio.file.Files
import java.util

import de.m7w3.signal.Logging
import de.m7w3.signal.events.{ContactsSyncedEvent, EventPublisher, GroupsSyncedEvent}
import de.m7w3.signal.store.SignalDesktopApplicationStore
import org.whispersystems.signalservice.api.SignalServiceMessageReceiver
import org.whispersystems.signalservice.api.messages.multidevice._
import org.whispersystems.signalservice.api.messages.{SignalServiceAttachment, SignalServiceDataMessage, SignalServiceEnvelope, SignalServiceGroup}
import scala.annotation.tailrec
import scala.collection.JavaConverters.collectionAsScalaIterableConverter
import de.m7w3.signal.store.model.TextMessage

class SignalDesktopMessageHandler(signalDesktopApplicationStore: SignalDesktopApplicationStore,
                                  messageReceiver: SignalServiceMessageReceiver,
                                  eventPublisher: EventPublisher) extends MessageHandler with Logging {

  //def handlePrekeyMessage()

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
    eventPublisher.publishEvent(ContactsSyncedEvent)
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
    eventPublisher.publishEvent(GroupsSyncedEvent)
  }

  @tailrec
  private def processGroups(groupsStream: DeviceGroupsInputStream): Unit = {
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
    }
    if (syncMessage.getContacts.isPresent) {
      handleContacts(envelope, syncMessage.getContacts.get())
    }
    if (syncMessage.getGroups.isPresent) {
      handleGroups(envelope, syncMessage.getGroups.get())
    }
    if (syncMessage.getRead.isPresent) {
      handleRead(envelope, syncMessage.getRead.get())
    }
    if (syncMessage.getRequest.isPresent) {
      handleRequest(envelope, syncMessage.getRequest.get())
    }
    if (syncMessage.getSent.isPresent) {
      handleSent(envelope, syncMessage.getSent.get())
    }
  }

  override def handleDataMessage(envelope: SignalServiceEnvelope, message: SignalServiceDataMessage): Unit = {
    logger.debug(s"received data message [${message.getBody} ${message.getGroupInfo}]")

    if      (message.isEndSession())               handleEndSessionMessage(envelope, message)
    else if (message.isGroupUpdate())              handleGroupMessage(envelope, message)
    else if (message.isExpirationUpdate())         handleExpirationUpdate(envelope, message)
    else if (message.getAttachments().isPresent()) handleMediaMessage(envelope, message)
    else                                           handleTextMessage(envelope, message)


    //TODO: check the groupDatabase
    // if (message.getGroupInfo().isPresent() && groupDatabase.isUnknownGroup(message.getGroupInfo().get().getGroupId())) {
    //   handleUnknownGroupMessage(envelope, message.getGroupInfo().get());
    // }
  }

  def handleEndSessionMessage(envelope: SignalServiceEnvelope, message: SignalServiceDataMessage): Unit = {
    logger.debug(s"received endSession message")
  }

  def handleGroupMessage(envelope: SignalServiceEnvelope, message: SignalServiceDataMessage): Unit = {
    logger.debug(s"received groupUpdate message")
  }

  def handleExpirationUpdate(envelope: SignalServiceEnvelope, message: SignalServiceDataMessage): Unit = {
    logger.debug(s"received expirationUpdate message")
  }

  def handleMediaMessage(envelope: SignalServiceEnvelope, message: SignalServiceDataMessage): Unit = {
    logger.debug(s"received mediaMessage message")
  }

  def handleTextMessage(envelope: SignalServiceEnvelope, message: SignalServiceDataMessage): Unit = {
    logger.debug(s"received textMessage message")
    val body = if(message.getBody().isPresent()) message.getBody().get() else ""
    val group = message.getGroupInfo()
    val groupId = if (group.isPresent()) Some(group.get().getGroupId()) else None

    val textMessage = TextMessage(body, envelope.getSource(), envelope.getSourceDevice(),
      message.getTimestamp(), groupId)
    // textMessage = new IncomingEncryptedMessage(textMessage, body);
    // val  insertResult = database.insertMessageInbox(masterSecret, textMessage);

    // if (insertResult.isPresent()) threadId = insertResult.get().getThreadId();
    // else                          threadId = null;

    // if (smsMessageId.isPresent()) database.deleteMessage(smsMessageId.get());

  }

  def handleUnknownGroupMessage(envelope: SignalServiceEnvelope, message: SignalServiceGroup): Unit = {
    logger.debug(s"received unknownGroupMessage message")
  }
}
