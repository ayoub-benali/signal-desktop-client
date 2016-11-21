package de.m7w3.signal.store

import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.whispersystems.libsignal.SignalProtocolAddress

class SignalDesktopIdentityKeyStoreSpec extends FlatSpec with Matchers with BeforeAndAfterAll with TestStore {
  behavior of "SignalDesktopIdentityKeyStore"


  it should "fail if not initialized" in {
    an [NoSuchElementException] should be thrownBy protocolStore.getIdentityKeyPair
    an [NoSuchElementException] should be thrownBy protocolStore.getLocalRegistrationId
  }

  it should "return always the same local identity data after initialization" in {
    protocolStore.identityKeyStore.initialize(localIdentity)
    protocolStore.getIdentityKeyPair shouldBe localIdentity.keyPair
    protocolStore.getLocalRegistrationId shouldBe localIdentity.registrationId
  }

  it should "consider missing trustedIdentities as not trusted" in {
    protocolStore.identityKeyStore.initialize(localIdentity)
    val address = new SignalProtocolAddress("test", 1)

    protocolStore.isTrustedIdentity(address, remoteIdentity.keyPair.getPublicKey) shouldBe false
  }

  it should "consider saved identities as trusted" in {
    protocolStore.identityKeyStore.initialize(localIdentity)
    val address = new SignalProtocolAddress("test", 1)
    protocolStore.saveIdentity(address, remoteIdentity.keyPair.getPublicKey)
    protocolStore.isTrustedIdentity(address, remoteIdentity.keyPair.getPublicKey) shouldBe true
  }
}
