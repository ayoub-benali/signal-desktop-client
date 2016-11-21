package de.m7w3.signal.store

import java.util

import de.m7w3.signal.store.model.Sessions
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.{SessionRecord, SessionStore}

case class SignalDesktopSessionStore(dbRunner: DBActionRunner) extends SessionStore {

  override def containsSession(address: SignalProtocolAddress): Boolean = {
    dbRunner.run(Sessions.exists(address))
  }

  override def loadSession(address: SignalProtocolAddress): SessionRecord = {
    dbRunner.run(Sessions.get(address)).map(_.record).getOrElse(
      new SessionRecord()
    )
  }

  override def storeSession(address: SignalProtocolAddress, record: SessionRecord): Unit = {
    dbRunner.run(Sessions.upsert(address, record))
    ()
  }

  override def deleteAllSessions(name: String): Unit = {
    dbRunner.run(Sessions.deleteByRemoteClientName(name))
    ()
  }

  override def getSubDeviceSessions(name: String): util.List[Integer] = {
    import scala.collection.JavaConverters.seqAsJavaListConverter
    dbRunner.run(Sessions.getSessionDevices(name)).map(Integer.valueOf).asJava
  }

  override def deleteSession(address: SignalProtocolAddress): Unit = {
    dbRunner.run(Sessions.delete(address))
    ()
  }
}
