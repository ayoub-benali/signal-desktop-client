package de.m7w3.signal.messages

import java.io.IOException

import de.m7w3.signal.{Constants, Logging}
import de.m7w3.signal.store.SignalDesktopProtocolStore
import de.m7w3.signal.store.model.Registration
import org.whispersystems.libsignal.util.guava.Optional
import org.whispersystems.signalservice.api.{SignalServiceMessagePipe, SignalServiceMessageSender}
import org.whispersystems.signalservice.api.crypto.UntrustedIdentityException
import org.whispersystems.signalservice.api.messages.multidevice.SignalServiceSyncMessage

trait MessageSender {
  def send(message: SignalServiceSyncMessage): Unit
}

case class SignalMessageSender(userId: String, password: String, deviceId: Int, store: SignalDesktopProtocolStore) extends MessageSender with Logging {
  val messageSender = new SignalServiceMessageSender(
    Constants.SERVICE_URLS,
    userId,
    password,
    deviceId,
    store,
    Constants.USER_AGENT,
    Optional.absent[SignalServiceMessagePipe],
    Optional.absent[SignalServiceMessageSender.EventListener])

  @throws[IOException]
  @throws[UntrustedIdentityException]
  def send(message: SignalServiceSyncMessage): Unit = {
    try
      messageSender.sendMessage(message)
    catch {
      case e: UntrustedIdentityException => {
        logger.error("untrusted identity encountered", e)
        //store.saveIdentity(e.getE164Number, e.getIdentityKey, TrustLevel.UNTRUSTED)
        throw e
      }
      case e: Throwable =>
        logger.error(s"error sending message $message", e)
        throw e
    }
  }
}

object SignalMessageSender {
  def apply(registration: Registration, store: SignalDesktopProtocolStore): SignalMessageSender = {
    new SignalMessageSender(registration.userName, registration.password, registration.deviceId, store)
  }
}
