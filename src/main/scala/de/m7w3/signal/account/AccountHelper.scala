package de.m7w3.signal.account

import de.m7w3.signal.messages.MessageSender
import de.m7w3.signal.store.model.Registration

trait AccountHelper extends PreKeyRefresher {
  def countAvailablePreKeys(): Int
  def requestSyncGroups(): Unit
  def requestSyncContacts(): Unit
}

object AccountHelper {
  def apply(registration: Registration, messageSender: MessageSender): AccountHelper = {
    AccountHelperImpl(registration.userName, registration.password, registration.deviceId, messageSender)
  }
}




