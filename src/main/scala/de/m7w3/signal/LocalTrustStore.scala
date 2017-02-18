package de.m7w3.signal

import org.whispersystems.signalservice.api.push.TrustStore
import java.io.InputStream

object LocalTrustStore extends TrustStore {
  override def getKeyStoreInputStream: InputStream = {
    getClass.getResource("/de/m7w3/signal/whisper.store").openStream()
  }

  override def getKeyStorePassword: String = {
    "whisper"
  }
}
