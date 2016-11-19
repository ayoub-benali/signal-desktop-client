package de.m7w3.signal

import java.security.SecureRandom
import org.whispersystems.signalservice.internal.util.Base64

package object Util {
  def getSecret(size: Int): String = {
    val secret = new Array[Byte](size)
    SecureRandom.getInstance("SHA1PRNG").nextBytes(secret)
    Base64.encodeBytes(secret)
  }
}