package de.m7w3.signal

import java.io.InputStream

import org.whispersystems.signalservice.api.push.TrustStore

case class LocalKeyStore() extends TrustStore {
  def  getKeyStoreInputStream: InputStream = {
    getClass.getResource("/de/m7w3/signal/whisper.store").openStream()
  }

  def getKeyStorePassword: String = {
    "whisper"
  }
}
