package de.m7w3.signal.messages

import org.whispersystems.signalservice.api.messages.multidevice.SignalServiceSyncMessage
import org.whispersystems.signalservice.api.messages.{SignalServiceDataMessage, SignalServiceEnvelope}

trait MessageHandler {
  def handleSyncMessage(envelope: SignalServiceEnvelope, syncMessage: SignalServiceSyncMessage)
  def handleDataMessage(envelope: SignalServiceEnvelope, dataMessage: SignalServiceDataMessage)
}
