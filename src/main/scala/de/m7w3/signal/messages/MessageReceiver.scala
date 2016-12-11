package de.m7w3.signal.messages

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{Executors, ThreadFactory, TimeUnit, TimeoutException}

import de.m7w3.signal.store.SignalDesktopProtocolStore
import de.m7w3.signal.{Constants, LocalKeyStore}
import org.slf4j.{Logger, LoggerFactory}
import org.whispersystems.libsignal.InvalidVersionException
import org.whispersystems.signalservice.api.crypto.SignalServiceCipher
import org.whispersystems.signalservice.api.messages.{SignalServiceContent, SignalServiceEnvelope}
import org.whispersystems.signalservice.api.push.SignalServiceAddress
import org.whispersystems.signalservice.api.{SignalServiceMessagePipe, SignalServiceMessageReceiver}
import org.whispersystems.signalservice.internal.push.SignalServiceUrl

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext

/**
  * receives messages on a separate thread and hands them on to a message handler for further processing
  */
case class MessageReceiver(protocolStore: SignalDesktopProtocolStore,
                           messageHandler: MessageHandler,
                           username: String,
                           password: String,
                           deviceId: Int,
                           signalingKey: String,
                           timeoutMillis: Long) {

  val logger: Logger = LoggerFactory.getLogger(getClass)
  val threadFactory = new ThreadFactory {
    override def newThread(r: Runnable): Thread = new Thread(r, "signal-desktop-message-receiver")
  }
  val ec: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1, threadFactory))
  val keepOnRockin: AtomicBoolean = new AtomicBoolean(false)
  val service = new SignalServiceUrl(Constants.URL, LocalKeyStore)
  val messageReceiver = new SignalServiceMessageReceiver(
    Array(service),
    username,
    password,
    deviceId,
    signalingKey,
    Constants.USER_AGENT)

  ec.execute(new Runnable {
    override def run() = {
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

      if (envelope.isPreKeySignalMessage) {
        logger.debug(s"got prekeys from ${envelope.getSourceAddress} ${envelope.getSourceDevice}")
      } else if (envelope.isReceipt) {
        logger.debug(s"got receipt from ${envelope.getSourceAddress} ${envelope.getSourceDevice}")
      } else if (envelope.isSignalMessage) {
        logger.debug(s"got signalmessage from ${envelope.getSourceAddress} ${envelope.getSourceDevice}")
      }

      val content = decryptMessage(envelope)


      if (content.getDataMessage.isPresent) {
        messageHandler.handleDataMessage(envelope, content.getDataMessage.get())
      } else if (content.getSyncMessage.isPresent) {
        messageHandler.handleSyncMessage(envelope, content.getSyncMessage.get())
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

  private def decryptMessage(envelope: SignalServiceEnvelope): SignalServiceContent = {
    val cipher = new SignalServiceCipher(new SignalServiceAddress(username), protocolStore)
    cipher.decrypt(envelope)
  }
}
