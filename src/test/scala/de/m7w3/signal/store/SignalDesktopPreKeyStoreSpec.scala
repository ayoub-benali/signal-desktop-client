package de.m7w3.signal.store

import org.scalatest.{FlatSpec, Matchers}
import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.state.PreKeyRecord

class SignalDesktopPreKeyStoreSpec extends FlatSpec with Matchers with TestStore {

  behavior of SignalDesktopPreKeyStore.getClass.getSimpleName

  it should "contain stored prekeys only" in {
    val preKeyId = 1
    val keyPair = Curve.generateKeyPair()
    val record = new PreKeyRecord(preKeyId, keyPair)

    protocolStore.containsPreKey(preKeyId) shouldBe false
    protocolStore.storePreKey(preKeyId, record)

    protocolStore.containsPreKey(preKeyId) shouldBe true
  }

  it should "load stored prekeys only" in {
    val preKeyId = 2
    val keyPair = Curve.generateKeyPair()
    val record = new PreKeyRecord(preKeyId, keyPair)

    an [InvalidKeyIdException] should be thrownBy protocolStore.loadPreKey(preKeyId)

    protocolStore.storePreKey(preKeyId, record)
    val preKeyRecord = protocolStore.loadPreKey(preKeyId)
    preKeyRecord.serialize() shouldEqual record.serialize()

  }

  it should "not contain or load removed prekeys" in {

    val preKeyId = 2
    val keyPair = Curve.generateKeyPair()
    val record = new PreKeyRecord(preKeyId, keyPair)

    protocolStore.containsPreKey(preKeyId) shouldBe false
    an [InvalidKeyIdException] should be thrownBy protocolStore.loadPreKey(preKeyId)

    protocolStore.storePreKey(preKeyId, record)

    protocolStore.containsPreKey(preKeyId) shouldBe true
    protocolStore.loadPreKey(preKeyId) shouldBe a [PreKeyRecord]

    protocolStore.removePreKey(preKeyId)

    protocolStore.containsPreKey(preKeyId) shouldBe false
    an [InvalidKeyIdException] should be thrownBy protocolStore.loadPreKey(preKeyId)
  }
}
