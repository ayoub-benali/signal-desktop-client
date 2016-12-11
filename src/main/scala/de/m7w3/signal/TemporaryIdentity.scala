package de.m7w3.signal

import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.util.KeyHelper

object TemporaryIdentity {
  private lazy val temporaryIdentity: IdentityKeyPair = KeyHelper.generateIdentityKeyPair()

  def get: IdentityKeyPair = temporaryIdentity
}
