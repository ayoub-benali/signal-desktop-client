package de.m7w3.signal.store


import de.m7w3.signal.store.model.Identity
import org.whispersystems.libsignal.{IdentityKey, IdentityKeyPair, SignalProtocolAddress}
import org.whispersystems.libsignal.ecc.{Curve, ECPrivateKey}
import org.whispersystems.libsignal.state.SignedPreKeyRecord

import scala.util.Random

object TestUtils {
  def generateIdentity: Identity = {
    val ecKeyPair = Curve.generateKeyPair()
    val keyPair = new IdentityKeyPair(new IdentityKey(ecKeyPair.getPublicKey), ecKeyPair.getPrivateKey)
    val registrationId = Random.nextInt(16379) + 1 // 16380
    Identity(registrationId, keyPair.serialize())
  }

  def generateSignedPreKeyRecord(id: Int, privateKey: ECPrivateKey): SignedPreKeyRecord = {
    val signedPreKeyId = 1
    val ts = 0L
    val keyPair = Curve.generateKeyPair()
    val signature = Curve.calculateSignature(
      privateKey,
      keyPair.getPublicKey.serialize()
    )
    new SignedPreKeyRecord(signedPreKeyId, 0L, keyPair, signature)
  }
}

trait Identities {
  val localIdentity = TestUtils.generateIdentity
  val remoteIdentity = TestUtils.generateIdentity
  val anotherIdentity = TestUtils.generateIdentity
}

trait Addresses {
  val localAddress: SignalProtocolAddress = new SignalProtocolAddress("+49123456789", 1)
  val remoteAddress: SignalProtocolAddress = new SignalProtocolAddress("+49987654321", 2)
}
