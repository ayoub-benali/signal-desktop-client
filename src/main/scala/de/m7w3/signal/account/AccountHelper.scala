package de.m7w3.signal.account

import de.m7w3.signal.store.SignalDesktopProtocolStore
import org.whispersystems.libsignal.state.{PreKeyRecord, SignedPreKeyRecord}

trait AccountHelper {
  def generateNewDeviceURL(): String
  def finishDeviceLink(deviceName: String, store: SignalDesktopProtocolStore): Unit
  def refreshPreKeys(store: SignalDesktopProtocolStore): PreKeyRefreshResult
  def countAvailablePreKeys(): Int
}

case class PreKeyRefreshResult(oneTimePreKeys: List[PreKeyRecord], lastResortKey: PreKeyRecord, signedPreKeyRecord: SignedPreKeyRecord)


object AccountHelper {
  val PREKEY_BATCH_SIZE = 100

  def apply(userId: String, password: String): AccountHelper = {
    AccountHelperImpl(userId, password)
  }
}
