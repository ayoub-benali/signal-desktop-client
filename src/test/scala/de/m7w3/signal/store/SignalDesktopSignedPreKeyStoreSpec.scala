package de.m7w3.signal.store

import org.scalatest.{FlatSpec, Matchers}
import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.state.{PreKeyRecord, SignedPreKeyRecord}

class SignalDesktopSignedPreKeyStoreSpec extends FlatSpec with Matchers with TestStore {

  behavior of SignalDesktopSignedPreKeyStore.getClass.getSimpleName

  it should "contain stored signedPreKeys only" in {
    val signedPreKeyId = 1
    val record = TestUtils.generateSignedPreKeyRecord(signedPreKeyId, localIdentity.keyPair.getPrivateKey)

    protocolStore.containsSignedPreKey(signedPreKeyId) shouldBe false
    protocolStore.storeSignedPreKey(signedPreKeyId, record)

    protocolStore.containsSignedPreKey(signedPreKeyId) shouldBe true
  }

  it should "load stored signedPreKeys only" in {
    val signedPreKeyId = 2
    val record = TestUtils.generateSignedPreKeyRecord(signedPreKeyId, localIdentity.keyPair.getPrivateKey)

    an [InvalidKeyIdException] should be thrownBy protocolStore.loadSignedPreKey(signedPreKeyId)

    protocolStore.storeSignedPreKey(signedPreKeyId, record)
    val loadedRecord = protocolStore.loadSignedPreKey(signedPreKeyId)
    loadedRecord.serialize() shouldEqual record.serialize()
  }

  it should "load all signedPreKeys" in {
    import scala.collection.JavaConverters.collectionAsScalaIterableConverter
    val signedPreKeyId1 = 3
    val record1 = TestUtils.generateSignedPreKeyRecord(signedPreKeyId1, localIdentity.keyPair.getPrivateKey)
    val signedPreKeyId2 = 4
    val record2 = TestUtils.generateSignedPreKeyRecord(signedPreKeyId2, localIdentity.keyPair.getPrivateKey)
    val records = Set(
      record1.serialize(),
      record2.serialize()
    )

    val loadedSignedPreKeys = protocolStore.loadSignedPreKeys().asScala.map(_.serialize())
    loadedSignedPreKeys.foreach { keyBytes =>
      records should contain(keyBytes)
    }
  }

  it should "not contain or load removed signedPreKeys" in {

    val signedPreKeyId = 5
    val record = TestUtils.generateSignedPreKeyRecord(signedPreKeyId, localIdentity.keyPair.getPrivateKey)


    protocolStore.containsSignedPreKey(signedPreKeyId) shouldBe false
    an [InvalidKeyIdException] should be thrownBy protocolStore.loadSignedPreKey(signedPreKeyId)

    protocolStore.storeSignedPreKey(signedPreKeyId, record)

    protocolStore.containsSignedPreKey(signedPreKeyId) shouldBe true
    protocolStore.loadSignedPreKey(signedPreKeyId) shouldBe a [SignedPreKeyRecord]

    protocolStore.removeSignedPreKey(signedPreKeyId)

    protocolStore.containsSignedPreKey(signedPreKeyId) shouldBe false
    an [InvalidKeyIdException] should be thrownBy protocolStore.loadSignedPreKey(signedPreKeyId)
  }
}
