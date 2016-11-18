package de.m7w3.signal.store

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec, Matchers}

class SignalDesktopIdentityKeyStoreSpec extends FlatSpec with Matchers with BeforeAndAfterEach {
  behavior of "SignalDesktopIdentityKeyStore"

  var keyStore: SignalDesktopIdentityKeyStore = _


  it should "fail if not initialized" in {
    fail()
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    val dbRunner = DBActionRunner()
    keyStore = SignalDesktopSignedPreKeyStore()
  }
}
