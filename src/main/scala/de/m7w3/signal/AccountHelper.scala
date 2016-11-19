package de.m7w3.signal

import org.whispersystems.libsignal.util.KeyHelper
import org.whispersystems.signalservice.api.SignalServiceAccountManager
import org.whispersystems.signalservice.internal.util.Base64
import java.net.URLEncoder
import java.security.SecureRandom

case class AccountHelper(userId: String){
  // some init to create a SignalServiceAccountManager
  val secret = Array[Byte](20)
  SecureRandom.getInstance("SHA1PRNG").nextBytes(secret)
  val temporaryPassword = Base64.encodeBytes(secret)
  lazy val temporaryIdentity = KeyHelper.generateIdentityKeyPair()
  val accountManager = new SignalServiceAccountManager(Constants.URL, LocalKeyStore, userId, temporaryPassword, Constants.USER_AGENT)

  def getNewDeviceURL(): String = {
    val deviceID = URLEncoder.encode(accountManager.getNewDeviceUuid(), "UTF-8")
    val publicKey = URLEncoder.encode(Base64.encodeBytesWithoutPadding(temporaryIdentity.getPublicKey().serialize()), "UTF-8")
    val url = s"tsdevice:/?uuid=$deviceID&pub_key=$publicKey"
    url
  }
}