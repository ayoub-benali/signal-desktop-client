package de.m7w3.signal

import org.whispersystems.signalservice.internal.push.SignalServiceUrl

object Constants {
  val URL = "https://textsecure-service.whispersystems.org"
  val USER_AGENT = "signal-desktop-client"
  val SERVICE_URLS = Array(
    new SignalServiceUrl(URL, LocalKeyStore)
  )
}