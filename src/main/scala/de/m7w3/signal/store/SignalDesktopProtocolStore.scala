package de.m7w3.signal.store

import java.util

import de.m7w3.signal.store.model.{Registration, RegistrationData, Schema}
import org.whispersystems.libsignal.state.{PreKeyRecord, SessionRecord, SignalProtocolStore, SignedPreKeyRecord}
import org.whispersystems.libsignal.{IdentityKey, IdentityKeyPair, SignalProtocolAddress}
import slick.driver.H2Driver.api._

import scala.concurrent.duration._

case class SignalDesktopProtocolStore(dbRunner: DBActionRunner) extends SignalProtocolStore {

  // TODO: use DI
  val identityKeyStore = SignalDesktopIdentityKeyStore(dbRunner)
  val preKeyStore = SignalDesktopPreKeyStore(dbRunner)
  val sessionStore = SignalDesktopSessionStore(dbRunner)
  val signedPreKeyStore = SignalDesktopSignedPreKeyStore(dbRunner)


  override def storeSignedPreKey(signedPreKeyId: Int, record: SignedPreKeyRecord): Unit =
    signedPreKeyStore.storeSignedPreKey(signedPreKeyId, record)

  override def loadSignedPreKey(signedPreKeyId: Int): SignedPreKeyRecord =
    signedPreKeyStore.loadSignedPreKey(signedPreKeyId)

  override def loadSignedPreKeys(): util.List[SignedPreKeyRecord] =
    signedPreKeyStore.loadSignedPreKeys()

  override def containsSignedPreKey(signedPreKeyId: Int): Boolean =
    signedPreKeyStore.containsSignedPreKey(signedPreKeyId)

  override def removeSignedPreKey(signedPreKeyId: Int): Unit =
    signedPreKeyStore.removeSignedPreKey(signedPreKeyId)

  override def getIdentityKeyPair: IdentityKeyPair =
    identityKeyStore.getIdentityKeyPair

  override def isTrustedIdentity(address: SignalProtocolAddress, identityKey: IdentityKey): Boolean =
    identityKeyStore.isTrustedIdentity(address, identityKey)

  override def getLocalRegistrationId: Int = identityKeyStore.getLocalRegistrationId

  override def saveIdentity(address: SignalProtocolAddress, identityKey: IdentityKey): Unit =
    identityKeyStore.saveIdentity(address, identityKey)

  override def containsPreKey(preKeyId: Int): Boolean =
    preKeyStore.containsPreKey(preKeyId)

  override def loadPreKey(preKeyId: Int): PreKeyRecord =
    preKeyStore.loadPreKey(preKeyId)

  override def removePreKey(preKeyId: Int): Unit =
    preKeyStore.removePreKey(preKeyId)

  override def storePreKey(preKeyId: Int, record: PreKeyRecord): Unit =
    preKeyStore.storePreKey(preKeyId, record)

  override def containsSession(address: SignalProtocolAddress): Boolean =
    sessionStore.containsSession(address)

  override def loadSession(address: SignalProtocolAddress): SessionRecord =
    sessionStore.loadSession(address)

  override def storeSession(address: SignalProtocolAddress, record: SessionRecord): Unit =
    sessionStore.storeSession(address, record)

  override def deleteAllSessions(name: String): Unit =
    sessionStore.deleteAllSessions(name)

  override def getSubDeviceSessions(name: String): util.List[Integer] =
    sessionStore.getSubDeviceSessions(name)

  override def deleteSession(address: SignalProtocolAddress): Unit =
    sessionStore.deleteSession(address)

  def save(userName: String, deviceId: Int, password: String, signalingKey: String): Unit = {
    val registration = Registration(userName, deviceId, password, signalingKey)
    dbRunner.run(RegistrationData.insert(registration))
    ()
  }
}
