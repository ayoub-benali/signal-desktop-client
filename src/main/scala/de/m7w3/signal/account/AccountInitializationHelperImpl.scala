package de.m7w3.signal.account

import java.net.URLEncoder

import de.m7w3.signal.store.SignalDesktopProtocolStore
import de.m7w3.signal.store.model.{Identity, Registration}
import de.m7w3.signal.{Constants, Logging, TemporaryIdentity, Util}
import org.whispersystems.libsignal.util.KeyHelper
import org.whispersystems.signalservice.api.SignalServiceAccountManager
import org.whispersystems.signalservice.internal.util.Base64

private[account] case class AccountInitializationHelperImpl(userId: String, password: String)
  extends PreKeyRefresher
    with AccountInitializationHelper
    with Logging {

  val accountManager = new SignalServiceAccountManager(Constants.SERVICE_URLS, userId, password, Constants.USER_AGENT)

  override def generateNewDeviceURL(): String = {
    val uuid = URLEncoder.encode(accountManager.getNewDeviceUuid, "UTF-8")
    val publicKey = URLEncoder.encode(Base64.encodeBytesWithoutPadding(TemporaryIdentity.get.getPublicKey.serialize()), "UTF-8")
    val url = s"tsdevice:/?uuid=$uuid&pub_key=$publicKey"
    url
  }

  override def finishDeviceRegistration(deviceName: String, store: SignalDesktopProtocolStore): Registration = {
    val signalingKey = Util.getSecret(52)
    val temporaryRegistrationId = KeyHelper.generateRegistrationId(false)
    val ret = accountManager.finishNewDeviceRegistration(TemporaryIdentity.get, signalingKey, false, true, temporaryRegistrationId, deviceName)
    val deviceId = ret.getDeviceId
    val userName = ret.getNumber
    val identity = Identity(temporaryRegistrationId, ret.getIdentity.serialize())
    store.identityKeyStore.initialize(identity)
    refreshPreKeys(store)

    val registration = Registration(userName, deviceId, password, signalingKey)
    store.save(registration)
    registration
  }

}
