package de.m7w3.signal.store

import java.util

import de.m7w3.signal.Logging
import de.m7w3.signal.store.model.Sessions
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.{SessionRecord, SessionStore}

case class SignalDesktopSessionStore(dbRunner: DBActionRunner) extends SessionStore with Logging {

  override def containsSession(address: SignalProtocolAddress): Boolean = {
    val sessionExists = dbRunner.run(Sessions.exists(address))
    sessionExists && loadSession(address).getSessionState.hasSenderChain
  }

  override def loadSession(address: SignalProtocolAddress): SessionRecord = {
    logger.debug(s"load session for $address.")
    dbRunner.run(Sessions.get(address)).map(_.record).getOrElse(
      new SessionRecord()
    )
  }

  override def storeSession(address: SignalProtocolAddress, record: SessionRecord): Unit = {
    logger.debug(s"store session for $address.")
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
    logger.debug(s"deleting session for $address.")
    dbRunner.run(Sessions.delete(address))
    ()
  }
}
