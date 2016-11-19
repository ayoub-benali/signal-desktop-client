package de.m7w3.signal.store

import de.m7w3.signal.store.model.Identity
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.whispersystems.libsignal.{IdentityKey, IdentityKeyPair, SignalProtocolAddress}
import org.whispersystems.libsignal.ecc.Curve

import scala.util.Random

class SignalDesktopIdentityKeyStoreSpec extends FlatSpec with Matchers with BeforeAndAfterAll with TestDatabase {
  behavior of "SignalDesktopIdentityKeyStore"

  val identity: Identity = generateIdentity
  val remoteIdentity = generateIdentity
  var keyStore: SignalDesktopIdentityKeyStore = _


  def generateIdentity: Identity = {
    val ecKeyPair = Curve.generateKeyPair()
    val keyPair = new IdentityKeyPair(new IdentityKey(ecKeyPair.getPublicKey), ecKeyPair.getPrivateKey)
    val registrationId = Random.nextInt(16379) + 1 // 16380
    Identity(registrationId, keyPair.serialize())
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    keyStore = SignalDesktopIdentityKeyStore(dBActionRunner)

  }

  it should "fail if not initialized" in {
    an [NoSuchElementException] should be thrownBy keyStore.getIdentityKeyPair
    an [NoSuchElementException] should be thrownBy keyStore.getLocalRegistrationId
  }

  it should "return always the same local identity data after initialization" in {
    keyStore.initialize(identity)
    keyStore.getIdentityKeyPair shouldBe identity.keyPair
    keyStore.getLocalRegistrationId shouldBe identity.registrationId
  }

  it should "consider missing trustedIdentities as not trusted" in {
    keyStore.initialize(identity)
    val address = new SignalProtocolAddress("test", 1)

    keyStore.isTrustedIdentity(address, remoteIdentity.keyPair.getPublicKey) shouldBe false
  }

  it should "consider saved identities as trusted" in {
    keyStore.initialize(identity)
    val address = new SignalProtocolAddress("test", 1)
    keyStore.saveIdentity(address, remoteIdentity.keyPair.getPublicKey)
    keyStore.isTrustedIdentity(address, remoteIdentity.keyPair.getPublicKey) shouldBe true
  }
}
