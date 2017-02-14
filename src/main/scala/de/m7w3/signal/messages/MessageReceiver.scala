package de.m7w3.signal.messages

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{Executors, ThreadFactory, TimeUnit, TimeoutException}

import de.m7w3.signal.store.{SignalDesktopApplicationStore, SignalDesktopProtocolStore}
import de.m7w3.signal.{Constants, InitiatedContext, LocalKeyStore, Logging}
import de.m7w3.signal.store.model.Registration
import org.whispersystems.libsignal.InvalidVersionException
import org.whispersystems.signalservice.api.crypto.SignalServiceCipher
import org.whispersystems.signalservice.api.messages.{SignalServiceContent, SignalServiceEnvelope}
import org.whispersystems.signalservice.api.push.SignalServiceAddress
import org.whispersystems.signalservice.api.{SignalServiceMessagePipe, SignalServiceMessageReceiver}
import org.whispersystems.signalservice.internal.push.SignalServiceProtos.Content
import org.whispersystems.signalservice.internal.push.SignalServiceUrl

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext

/**
  * receives messages on a separate thread and hands them on to a message handler for further processing
  */
case class MessageReceiver(cipher: SignalServiceCipher,
                           messageReceiver: SignalServiceMessageReceiver,
                           messageHandler: MessageHandler,
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
        logger.debug(s"got receipt from ${envelope.getSourceAddress} ${envelope.getSourceDevice}")
        // TODO: handle
      } else if (envelope.isSignalMessage) {
        logger.debug(s"got signalmessage from ${envelope.getSourceAddress} ${envelope.getSourceDevice}")
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
        logger.debug(s"got prekeys from ${envelope.getSourceAddress} ${envelope.getSourceDevice}")

        // TODO: handle
        // refresh prekeys
        // if less than 10 prekeys available - refresh:
        // create new prekeys, new signed prekey and publish using accountmanager.setPreKeys
        // and set active signedprekey id
        // clean prekeys - delete signedprekeys that are older than 7 days and not active anymore
      }

    } catch {
      case te: TimeoutException =>
        logger.debug(s"timeout waiting for messages...")
        // ignore
      case e: InvalidVersionException => // ignore
    }
    if (keepOnRockin.get()) {
      doReceiveMessages(pipe)
    } else {
      logger.info("stopped receiving messages.")
    }
  }
}

object MessageReceiver {

  def initialize(context: InitiatedContext): MessageReceiver = {
    val data: Registration = context.protocolStore.getRegistrationData()
    val signalMessageReceiver: SignalServiceMessageReceiver = new SignalServiceMessageReceiver(
      Array(new SignalServiceUrl(Constants.URL, LocalKeyStore)),
      data.userName,
      data.password,
      data.deviceId,
      data.signalingKey,
      Constants.USER_AGENT
    )
    val messageHandler = new SignalDesktopMessageHandler(
      context.applicationStore,
      context.account.accountManager,
      signalMessageReceiver)
    val signalServiceCipher = new SignalServiceCipher(new SignalServiceAddress(data.userName), context.protocolStore)
    MessageReceiver(
      signalServiceCipher,
      signalMessageReceiver,
      messageHandler,
      10 * 1000L
    )
  }
}
