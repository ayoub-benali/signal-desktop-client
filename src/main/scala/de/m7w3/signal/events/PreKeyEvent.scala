package de.m7w3.signal.events

import org.whispersystems.signalservice.api.messages.{SignalServiceContent, SignalServiceEnvelope}

case class PreKeyEvent(envelope: SignalServiceEnvelope, content: SignalServiceContent) extends SignalDesktopEvent
