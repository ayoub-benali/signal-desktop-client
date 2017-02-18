package de.m7w3.signal.account

import de.m7w3.signal.store.SignalDesktopProtocolStore
import de.m7w3.signal.store.model.Registration

trait AccountInitializationHelper extends PreKeyRefresher{
  def generateNewDeviceURL(): String
  def finishDeviceRegistration(deviceName: String, store: SignalDesktopProtocolStore): Registration
}

object AccountInitializationHelper {
  def apply(userId: String, password: String): AccountInitializationHelper = {
    AccountInitializationHelperImpl(userId, password)
  }
}