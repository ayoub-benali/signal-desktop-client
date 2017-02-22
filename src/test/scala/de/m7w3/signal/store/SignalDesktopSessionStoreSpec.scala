package de.m7w3.signal.store

import de.m7w3.signal.store.model.Sessions
import org.scalatest.{FlatSpec, Matchers}
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.{SessionRecord, SessionState}
import slick.driver.H2Driver.api._

class SignalDesktopSessionStoreSpec extends FlatSpec with Matchers with TestStore {

  behavior of SignalDesktopSessionStore.getClass.getSimpleName

  it should "contain stored sessions" in {
    val session = new SessionRecord()
    val state = new SessionState()
    session.setState(state)
    session.archiveCurrentState()

    protocolStore.storeSession(remoteAddress, session)
    protocolStore.containsSession(remoteAddress) shouldBe true
  }

  it should "not contain session that were not stored" in {
    protocolStore.containsSession(remoteAddress) shouldBe false
  }

  it should "load copies of stored sessions" in {
    val session = new SessionRecord()
    val state = new SessionState()
    session.setState(state)
    session.archiveCurrentState()

    protocolStore.storeSession(remoteAddress, session)
    val loadedSession = protocolStore.loadSession(remoteAddress)
    session shouldBe 'isFresh
    loadedSession shouldNot be('isFresh)
    // serialize does not serialize freshness state, so comparing serialized form
    // yields correct equality for this use case
    session.serialize() shouldEqual loadedSession.serialize()
    loadedSession == session shouldBe false
  }

  it should "mutate the same session record given the same address" in {
    val session = new SessionRecord()
    val state = new SessionState()
    session.setState(state)
    session.archiveCurrentState()

    protocolStore.storeSession(remoteAddress, session)
    val loadedSession = protocolStore.loadSession(remoteAddress)
    session shouldBe 'isFresh
    loadedSession shouldNot be('isFresh)

    val newState = new SessionState()
    newState.setLocalRegistrationId(1)
    newState.setRemoteRegistrationId(42)
    loadedSession.setState(newState)
    protocolStore.storeSession(remoteAddress, loadedSession)

    val numSessions = dbActionRunner.run(Sessions.sessions.length.result)
    numSessions shouldBe 1

    val loadedTwiceSession = protocolStore.loadSession(remoteAddress)
    loadedTwiceSession.serialize() shouldEqual loadedSession.serialize()
  }

  it should "create a new session when loading non existent ones" in {
    val loadedSession = protocolStore.loadSession(remoteAddress)
    loadedSession shouldBe 'isFresh

    val secondLoadedSession = protocolStore.loadSession(remoteAddress)
    secondLoadedSession shouldBe 'isFresh

    protocolStore.containsSession(remoteAddress) shouldBe false

    loadedSession shouldNot equal(secondLoadedSession)

    secondLoadedSession.archiveCurrentState()
    secondLoadedSession.getSessionState.setLocalIdentityKey(localIdentity.keyPair.getPublicKey)
    secondLoadedSession.archiveCurrentState()
    protocolStore.storeSession(remoteAddress, secondLoadedSession)

    val anotherLoadedSession = protocolStore.loadSession(remoteAddress)
    anotherLoadedSession.serialize() shouldEqual secondLoadedSession.serialize()
  }

  it should "delete sessions properly" in {

    protocolStore.containsSession(remoteAddress) shouldBe false

    val session = new SessionRecord()
    val state = new SessionState()
    session.setState(state)

    protocolStore.storeSession(remoteAddress, session)
    protocolStore.containsSession(remoteAddress) shouldBe true

    protocolStore.deleteSession(remoteAddress)
    protocolStore.containsSession(remoteAddress) shouldBe false
  }

  it should "delete all sessions for a given sender name properly" in {

    val names = Seq("+4912345", "+4983341")
    val ids = Seq(1, 2)
    val addresses = (names zip ids) map { case (name, id) =>
      new SignalProtocolAddress(name, id)
    }
    val (name1Addresses, name2Addresses) = addresses.partition {
      address => {
        val name = address.getName
        name.equals(names.head)
      }
    }
    addresses.foreach { address =>
      protocolStore.storeSession(address, protocolStore.loadSession(address))
    }

    protocolStore.deleteAllSessions(names.head)
    name1Addresses.foreach {
      protocolStore.containsSession(_) shouldBe false
    }
    name2Addresses.foreach {
      protocolStore.containsSession(_) shouldBe true
    }
  }

  it should "return all known devices with active sessions for a recipient" in {

    import scala.collection.JavaConverters.iterableAsScalaIterableConverter

    val name = "+491234567890"
    val deviceIds = 1 to 10
    val addresses = deviceIds map { new SignalProtocolAddress(name, _) }

    val sessionAddresses = addresses.take(2)
    sessionAddresses.foreach { address =>
      protocolStore.storeSession(address, protocolStore.loadSession(address))
    }
    val sessionIds = sessionAddresses map { _.getDeviceId }
    val queriedSessionDeviceIds = protocolStore.getSubDeviceSessions(name).asScala.toSeq
    queriedSessionDeviceIds should contain theSameElementsAs sessionIds
  }
}
