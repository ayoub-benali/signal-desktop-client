package de.m7w3.signal

import org.whispersystems.libsignal.util.KeyHelper
import org.whispersystems.signalservice.api.SignalServiceAccountManager
import java.net.URLEncoder
import org.whispersystems.signalservice.internal.util.Base64

case class AccountHelper(userId: String){
  // some init to create a SignalServiceAccountManager
  val temporaryPassword = Util.getSecret(20)
  lazy val temporaryIdentity = KeyHelper.generateIdentityKeyPair()
  val accountManager = new SignalServiceAccountManager(Constants.URL, LocalKeyStore, userId, temporaryPassword, Constants.USER_AGENT)

  def getNewDeviceURL(): String = {
    val deviceID = URLEncoder.encode(accountManager.getNewDeviceUuid(), "UTF-8")
    val publicKey = URLEncoder.encode(Base64.encodeBytesWithoutPadding(temporaryIdentity.getPublicKey().serialize()), "UTF-8")
    val url = s"tsdevice:/?uuid=$deviceID&pub_key=$publicKey"
    url
  }

  def finishDeviceLink(deviceName: String) = {
      val signalingKey = Util.getSecret(52)
      val temporaryRegistrationId = KeyHelper.generateRegistrationId(false)
      val ret = accountManager.finishNewDeviceRegistration(temporaryIdentity, signalingKey, false, true, temporaryRegistrationId, deviceName)
      val deviceId = ret.getDeviceId()
      val username = ret.getNumber()
      println(deviceId)
      println(username)
      // refreshPreKeys();
      // requestSyncGroups();
      // requestSyncContacts();
      // save();
  }
}