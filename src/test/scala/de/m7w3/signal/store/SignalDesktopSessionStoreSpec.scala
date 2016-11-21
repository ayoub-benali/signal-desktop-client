package de.m7w3.signal.store

import java.util.Objects

import org.scalatest.{FlatSpec, Matchers}
import org.whispersystems.libsignal.state.{SessionRecord, SessionState}

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

  it should "create a new session when loading non existent ones" in {
    val loadedSession = protocolStore.loadSession(remoteAddress)
    loadedSession shouldBe 'isFresh

    val secondLoadedSession = protocolStore.loadSession(remoteAddress)
    secondLoadedSession shouldBe 'isFresh

    loadedSession shouldNot equal(secondLoadedSession)

    secondLoadedSession.archiveCurrentState()
    secondLoadedSession.getSessionState.setLocalIdentityKey(localIdentity.keyPair.getPublicKey)
    secondLoadedSession.archiveCurrentState()
    protocolStore.storeSession(remoteAddress, secondLoadedSession)

    val anotherLoadedSession = protocolStore.loadSession(remoteAddress)
    anotherLoadedSession.serialize() shouldEqual secondLoadedSession.serialize()
  }
}
