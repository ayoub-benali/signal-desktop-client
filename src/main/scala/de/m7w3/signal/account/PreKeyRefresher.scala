package de.m7w3.signal.account

import java.security.InvalidKeyException

import de.m7w3.signal.store.SignalDesktopProtocolStore
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.state.{PreKeyRecord, SignedPreKeyRecord}
import org.whispersystems.libsignal.util.Medium
import org.whispersystems.signalservice.api.SignalServiceAccountManager

import scala.util.{Success, Try}

case class PreKeyRefreshResult(oneTimePreKeys: List[PreKeyRecord], lastResortKey: PreKeyRecord, signedPreKeyRecord: SignedPreKeyRecord)

trait PreKeyRefresher {

  def accountManager: SignalServiceAccountManager

  def refreshPreKeys(store: SignalDesktopProtocolStore): PreKeyRefreshResult = {
    import scala.collection.JavaConverters.seqAsJavaListConverter

    val oneTimePreKeys = generatePreKeys(store)
    val lastResortKey = getOrGenerateLastResortPreKey(store)
    val signedPreKeyRecord = generateSignedPreKey(store.getIdentityKeyPair(), store)

    accountManager.setPreKeys(store.getIdentityKeyPair().getPublicKey, lastResortKey, signedPreKeyRecord, oneTimePreKeys.asJava)
    PreKeyRefreshResult(oneTimePreKeys, lastResortKey, signedPreKeyRecord)
  }

  def generatePreKeys(store: SignalDesktopProtocolStore): List[PreKeyRecord] = {
    val records = (0 until PreKeyRefresher.PREKEY_BATCH_SIZE).map(i => {
      val preKeyId = store.preKeyStore.incrementAndGetPreKeyId()
      val keyPair = Curve.generateKeyPair()
      val record = new PreKeyRecord(preKeyId, keyPair)
      store.storePreKey(preKeyId, record)
      record
    })
    records.toList
  }

  def getOrGenerateLastResortPreKey(store: SignalDesktopProtocolStore): PreKeyRecord = {
    Try(store.containsPreKey(Medium.MAX_VALUE)) match {
      case Success(true) => store.loadPreKey(Medium.MAX_VALUE)
      case _ => {
        store.removePreKey(Medium.MAX_VALUE)
        val keyPair = Curve.generateKeyPair()
        val record = new PreKeyRecord(Medium.MAX_VALUE, keyPair)
        store.storePreKey(Medium.MAX_VALUE, record)
        record
      }
    }
  }

  def generateSignedPreKey(identityKeyPair: IdentityKeyPair, store: SignalDesktopProtocolStore): SignedPreKeyRecord = {
    try {
      val nextSignedPreKeyId = store.signedPreKeyStore.getSignedPreKeyId
      val keyPair = Curve.generateKeyPair()
      val signature = Curve.calculateSignature(identityKeyPair.getPrivateKey, keyPair.getPublicKey.serialize())

      val record = new SignedPreKeyRecord(nextSignedPreKeyId, System.currentTimeMillis(), keyPair, signature)
      store.storeSignedPreKey(nextSignedPreKeyId, record)
      store.signedPreKeyStore.incrementAndGetSignedPreKeyId()
      record
    } catch {
      case e: InvalidKeyException => throw new AssertionError(e)
    }
  }
}

object PreKeyRefresher {
  val PREKEY_BATCH_SIZE = 100
}
