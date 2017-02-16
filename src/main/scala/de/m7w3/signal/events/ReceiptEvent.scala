package de.m7w3.signal.events

import org.whispersystems.signalservice.api.push.SignalServiceAddress

case class ReceiptEvent(source: SignalServiceAddress, sourceDeviceId: Int, timestamp: Long) extends SignalDesktopEvent
