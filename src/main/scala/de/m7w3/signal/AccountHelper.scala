package de.m7w3.signal

import java.net.URLEncoder
import java.security.InvalidKeyException

import scala.util.{Success, Try}
import de.m7w3.signal.store.SignalDesktopProtocolStore
import de.m7w3.signal.store.model.Identity
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.state.{PreKeyRecord, SignedPreKeyRecord}
import org.whispersystems.libsignal.util.{KeyHelper, Medium}
import org.whispersystems.signalservice.api.SignalServiceAccountManager
import org.whispersystems.signalservice.internal.util.Base64

case class AccountHelper(userId: String){

  val PREKEY_BATCH_SIZE = 100
  // some init to create a SignalServiceAccountManager
  val password = Util.getSecret(20)
  lazy val temporaryIdentity = KeyHelper.generateIdentityKeyPair()
  val accountManager = new SignalServiceAccountManager(Constants.URL, LocalKeyStore, userId, password, Constants.USER_AGENT)

  def getNewDeviceURL(): String = {
    val uuid = URLEncoder.encode(accountManager.getNewDeviceUuid(), "UTF-8")
    val publicKey = URLEncoder.encode(Base64.encodeBytesWithoutPadding(temporaryIdentity.getPublicKey().serialize()), "UTF-8")
    val url = s"tsdevice:/?uuid=$uuid&pub_key=$publicKey"
    url
  }

  def finishDeviceLink(deviceName: String, store: SignalDesktopProtocolStore): Unit = {
      val signalingKey = Util.getSecret(52)
      val temporaryRegistrationId = KeyHelper.generateRegistrationId(false)
      val ret = accountManager.finishNewDeviceRegistration(temporaryIdentity, signalingKey, false, true, temporaryRegistrationId, deviceName)
      val deviceId = ret.getDeviceId()
      val username = ret.getNumber()
      val identity = Identity(deviceId, temporaryIdentity.serialize())
      store.identityKeyStore.initialize(identity)
      refreshPreKeys(store)
      // requestSyncGroups();
      // requestSyncContacts();
      store.save(username, deviceId, password, signalingKey)
  }

  def refreshPreKeys(store: SignalDesktopProtocolStore): Unit = {
    import scala.collection.JavaConverters.seqAsJavaListConverter

    val oneTimePreKeys = generatePreKeys(store)
    val lastResortKey = getOrGenerateLastResortPreKey(store)
    val signedPreKeyRecord = generateSignedPreKey(store.getIdentityKeyPair(), store)

    accountManager.setPreKeys(store.getIdentityKeyPair().getPublicKey(), lastResortKey, signedPreKeyRecord, oneTimePreKeys.asJava)
  }

  def generatePreKeys(store: SignalDesktopProtocolStore): List[PreKeyRecord] = {
    val records = (0 until PREKEY_BATCH_SIZE).map(i => {
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
      val signature = Curve.calculateSignature(identityKeyPair.getPrivateKey(), keyPair.getPublicKey().serialize())

      val record = new SignedPreKeyRecord(nextSignedPreKeyId, System.currentTimeMillis(), keyPair, signature)
      store.storeSignedPreKey(nextSignedPreKeyId, record)
      store.signedPreKeyStore.incrementAndGetSignedPreKeyId
      record
    } catch {
      case e: InvalidKeyException => throw new AssertionError(e)
    }
  }
}
