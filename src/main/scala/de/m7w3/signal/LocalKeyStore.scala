package de.m7w3.signal

import org.whispersystems.signalservice.api.push.TrustStore
import java.io.InputStream
import java.security.KeyStore

case class LocalKeyStore() extends TrustStore {
  def  getKeyStoreInputStream(): InputStream = {
    getClass().getResource("/whisper.store2").openStream()
  }

  def getKeyStorePassword(): String = {
    "whisper"
  }
}