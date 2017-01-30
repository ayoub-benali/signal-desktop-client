package de.m7w3.signal.store

import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.whispersystems.libsignal.SignalProtocolAddress

class SignalDesktopIdentityKeyStoreSpec extends FlatSpec with Matchers with BeforeAndAfterAll with TestStore {

  behavior of "SignalDesktopIdentityKeyStore"


  it should "fail if not initialized" in {
    a [NoSuchElementException] should be thrownBy protocolStore.getIdentityKeyPair
    a [NoSuchElementException] should be thrownBy protocolStore.getLocalRegistrationId
  }

  it should "return always the same local identity data after initialization" in {
    protocolStore.identityKeyStore.initialize(localIdentity)
    protocolStore.getIdentityKeyPair shouldBe localIdentity.keyPair
    protocolStore.getLocalRegistrationId shouldBe localIdentity.registrationId
  }

  it should "consider missing trustedIdentities as trusted" in {
    protocolStore.identityKeyStore.initialize(localIdentity)
    val address = new SignalProtocolAddress("test", 1)

    protocolStore.isTrustedIdentity(address, remoteIdentity.keyPair.getPublicKey) shouldBe true
  }

  it should "consider saved identities as trusted" in {
    protocolStore.identityKeyStore.initialize(localIdentity)
    val address = new SignalProtocolAddress("test", 1)
    protocolStore.saveIdentity(address, remoteIdentity.keyPair.getPublicKey)
    protocolStore.isTrustedIdentity(address, remoteIdentity.keyPair.getPublicKey) shouldBe true
  }

  it should "consider different keys as untrusted" in {
    protocolStore.identityKeyStore.initialize(localIdentity)
    val address = new SignalProtocolAddress("test", 1)
    protocolStore.saveIdentity(address, remoteIdentity.keyPair.getPublicKey)
    protocolStore.isTrustedIdentity(address, anotherIdentity.keyPair.getPublicKey)
  }
}
