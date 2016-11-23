package de.m7w3.signal.store


import org.scalatest.{BeforeAndAfterEach, Suite}
import org.whispersystems.libsignal.SignalProtocolAddress

trait TestStore extends BeforeAndAfterEach with TestDatabase { suite: Suite =>

  val localIdentity = TestUtils.generateIdentity
  val remoteIdentity = TestUtils.generateIdentity

  val localAddress: SignalProtocolAddress = new SignalProtocolAddress("+49123456789", 1)
  val remoteAddress: SignalProtocolAddress = new SignalProtocolAddress("+49987654321", 2)

  var protocolStore: SignalDesktopProtocolStore = _

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    protocolStore = SignalDesktopProtocolStore(dBActionRunner)
  }
}
