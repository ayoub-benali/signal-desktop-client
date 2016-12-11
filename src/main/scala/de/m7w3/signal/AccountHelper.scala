package de.m7w3.signal

import java.io.IOException
import java.net.URLEncoder
import java.security.InvalidKeyException

import de.m7w3.signal.store.SignalDesktopProtocolStore
import de.m7w3.signal.store.model.Identity
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.state.{PreKeyRecord, SignedPreKeyRecord}
import org.whispersystems.libsignal.util.guava.Optional
import org.whispersystems.libsignal.util.{KeyHelper, Medium}
import org.whispersystems.signalservice.api.crypto.UntrustedIdentityException
import org.whispersystems.signalservice.api.messages.multidevice.{RequestMessage, SignalServiceSyncMessage}
import org.whispersystems.signalservice.api.{SignalServiceAccountManager, SignalServiceMessageSender}
import org.whispersystems.signalservice.internal.push.{SignalServiceProtos, SignalServiceUrl}
import org.whispersystems.signalservice.internal.util.Base64

import scala.util.{Success, Try}

case class AccountHelper(userId: String, password: String) extends Logging {

  val PREKEY_BATCH_SIZE = 100
  // some init to create a SignalServiceAccountManager
  lazy val temporaryIdentity = KeyHelper.generateIdentityKeyPair()
  val service = new SignalServiceUrl(Constants.URL, LocalKeyStore)
  val accountManager = new SignalServiceAccountManager(Array(service), userId, password, Constants.USER_AGENT)

  def getNewDeviceURL: String = {
    val uuid = URLEncoder.encode(accountManager.getNewDeviceUuid, "UTF-8")
    val publicKey = URLEncoder.encode(Base64.encodeBytesWithoutPadding(TemporaryIdentity.get.getPublicKey.serialize()), "UTF-8")
    val url = s"tsdevice:/?uuid=$uuid&pub_key=$publicKey"
    url
  }

  def finishDeviceLink(deviceName: String, store: SignalDesktopProtocolStore): Unit = {
      val signalingKey = Util.getSecret(52)
      val temporaryRegistrationId = KeyHelper.generateRegistrationId(false)
      val ret = accountManager.finishNewDeviceRegistration(TemporaryIdentity.get, signalingKey, false, true, temporaryRegistrationId, deviceName)
      val deviceId = ret.getDeviceId
      val username = ret.getNumber
      val identity = Identity(deviceId, ret.getIdentity.serialize())
      store.identityKeyStore.initialize(identity)
      refreshPreKeys(store)
      requestSyncGroups(deviceId, store)
      requestSyncContacts(deviceId, store)
      store.save(username, deviceId, password, signalingKey)
  }

  def refreshPreKeys(store: SignalDesktopProtocolStore): Unit = {
    import scala.collection.JavaConverters.seqAsJavaListConverter

    val oneTimePreKeys = generatePreKeys(store)
    val lastResortKey = getOrGenerateLastResortPreKey(store)
    val signedPreKeyRecord = generateSignedPreKey(store.getIdentityKeyPair(), store)

    accountManager.setPreKeys(store.getIdentityKeyPair().getPublicKey, lastResortKey, signedPreKeyRecord, oneTimePreKeys.asJava)
  }

  @throws[IOException]
  private def requestSyncGroups(deviceId: Int, store: SignalDesktopProtocolStore) {
    val r = SignalServiceProtos.SyncMessage.Request.newBuilder.setType(SignalServiceProtos.SyncMessage.Request.Type.GROUPS).build
    val message = SignalServiceSyncMessage.forRequest(new RequestMessage(r))
    try
      sendSyncMessage(message, deviceId, store)

    catch {
      case e: UntrustedIdentityException => {
        logger.error("Error requesting group synchronization", e)
      }
    }
  }

  @throws[IOException]
  private def requestSyncContacts(deviceId: Int, store: SignalDesktopProtocolStore) {
    val r = SignalServiceProtos.SyncMessage.Request.newBuilder.setType(SignalServiceProtos.SyncMessage.Request.Type.CONTACTS).build
    val message = SignalServiceSyncMessage.forRequest(new RequestMessage(r))
    try
      sendSyncMessage(message, deviceId, store)

    catch {
      case e: UntrustedIdentityException => {
        logger.error("Error requesting contact synchronization", e)
      }
    }
  }

  @throws[IOException]
  @throws[UntrustedIdentityException]
  private def sendSyncMessage(message: SignalServiceSyncMessage,
                              deviceId: Int,
                              store: SignalDesktopProtocolStore) {
    val messageSender = new SignalServiceMessageSender(
      Array(service),
      userId,
      password,
      deviceId,
      store,
      Constants.USER_AGENT,
      Optional.absent[SignalServiceMessageSender.EventListener])
    try
      messageSender.sendMessage(message)
    catch {
      case e: UntrustedIdentityException => {
        logger.error("untrusted identity encountered", e)
        //store.saveIdentity(e.getE164Number, e.getIdentityKey, TrustLevel.UNTRUSTED)
        throw e
      }
    }
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
