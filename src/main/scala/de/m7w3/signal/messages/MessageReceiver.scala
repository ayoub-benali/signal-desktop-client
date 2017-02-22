package de.m7w3.signal.messages

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{Executors, ThreadFactory, TimeUnit, TimeoutException}

import de.m7w3.signal.events.{EventPublisher, PreKeyEvent, ReceiptEvent}
import de.m7w3.signal.store.model.Registration
import de.m7w3.signal.{ApplicationContext, Constants, Logging}
import org.whispersystems.libsignal._
import org.whispersystems.signalservice.api.crypto.SignalServiceCipher
import org.whispersystems.signalservice.api.push.SignalServiceAddress
import org.whispersystems.signalservice.api.{SignalServiceMessagePipe, SignalServiceMessageReceiver}

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext

/**
  * receives messages on a separate thread and hands them on to a message handler for further processing
  */
case class MessageReceiver(cipher: SignalServiceCipher,
                           messageReceiver: SignalServiceMessageReceiver,
                           messageHandler: MessageHandler,
                           eventPublisher: EventPublisher,
                           timeoutMillis: Long) extends Logging {

  val threadFactory = new ThreadFactory {
    override def newThread(r: Runnable): Thread = new Thread(r, "signal-desktop-message-receiver")
  }
  val ec: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1, threadFactory))
  val keepOnRockin: AtomicBoolean = new AtomicBoolean(true)

  logger.info("start receiving messages")
  ec.execute(new Runnable {
    override def run(): Unit = {
      receiveMessages()
    }
  })

  def receiveMessages(): Unit = {
    val messagePipe = messageReceiver.createMessagePipe()
    try{
      doReceiveMessages(messagePipe)
    } finally {
      messagePipe.shutdown()
    }
  }

  @tailrec
  final def doReceiveMessages(pipe: SignalServiceMessagePipe): Unit = {
    try {
      val envelope = pipe.read(timeoutMillis, TimeUnit.MILLISECONDS)

      if (envelope.isReceipt) {
        // TODO: handle
        logger.debug(s"received receipt from ${envelope.getSource}")
        // not handled yet, just an example, maybe we don't need an event here
        eventPublisher.publishEvent(ReceiptEvent.fromEnevelope(envelope))
      } else {
        if (envelope.isSignalMessage) {
          logger.debug(s"got signalmessage from ${envelope.getSourceAddress.getNumber} ${envelope.getSourceDevice}")
          // TODO: handle
        }
        val content = cipher.decrypt(envelope)

        if (content.getDataMessage.isPresent) {
          messageHandler.handleDataMessage(envelope, content.getDataMessage.get())
        } else if (content.getSyncMessage.isPresent) {
          messageHandler.handleSyncMessage(envelope, content.getSyncMessage.get())
        } else {
          logger.debug("no content in received message")
        }

        // do this after decryption
        if (envelope.isPreKeySignalMessage) {
          logger.debug("received prekey signal message")
          eventPublisher.publishEvent(PreKeyEvent(envelope, content))
        }
      }

    } catch {
      case te: TimeoutException =>
        logger.debug(s"timeout waiting for messages...")
      case e: InvalidVersionException =>
        logger.warn("invalid version", e)
        // TODO: what would moxie do?
      case e @ (_:InvalidMessageException | _:InvalidKeyIdException | _:InvalidKeyException) =>
        logger.warn("corrupt message", e)
        // TODO: what would moxie do?
      case nse: NoSessionException =>
        logger.warn("no session for this message", nse)
      // TODO: what would moxie do?
      case lme: LegacyMessageException =>
        logger.warn("legacy message", lme)
      // TODO: what would moxie do?
      case dme: DuplicateMessageException =>
        logger.warn("duplicate message", dme)
      // TODO: what would moxie do?
      case uie: UntrustedIdentityException =>
        logger.warn("untrusted identity", uie)
      // TODO: what would moxie do?
    }
    if (keepOnRockin.get()) {
      doReceiveMessages(pipe)
    } else {
      logger.info("stopped receiving messages.")
    }
  }
}

object MessageReceiver {

  def initialize(context: ApplicationContext): MessageReceiver = {
    val data: Registration = context.protocolStore.getRegistrationData()
    val signalMessageReceiver: SignalServiceMessageReceiver = new SignalServiceMessageReceiver(
      Constants.SERVICE_URLS,
      data.userName,
      data.password,
      data.deviceId,
      data.signalingKey,
      Constants.USER_AGENT
    )
    val messageHandler = new SignalDesktopMessageHandler(
      context.applicationStore,
      signalMessageReceiver)
    val signalServiceCipher = new SignalServiceCipher(new SignalServiceAddress(data.userName), context.protocolStore)
    MessageReceiver(
      signalServiceCipher,
      signalMessageReceiver,
      messageHandler,
      context.asInstanceOf[EventPublisher],
      10 * 1000L
    )
  }
}
