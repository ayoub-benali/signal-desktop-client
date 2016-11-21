package de.m7w3.signal.store

import de.m7w3.signal.store.model.Identity
import org.whispersystems.libsignal.{IdentityKey, IdentityKeyPair}
import org.whispersystems.libsignal.ecc.Curve

import scala.util.Random

object TestUtils {
  def generateIdentity: Identity = {
    val ecKeyPair = Curve.generateKeyPair()
    val keyPair = new IdentityKeyPair(new IdentityKey(ecKeyPair.getPublicKey), ecKeyPair.getPrivateKey)
    val registrationId = Random.nextInt(16379) + 1 // 16380
    Identity(registrationId, keyPair.serialize())
  }
}
