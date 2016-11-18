package de.m7w3.signal.store

import de.m7w3.signal.store.model.PreKeys
import org.whispersystems.libsignal.state.{PreKeyRecord, PreKeyStore}
import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

case class SignalDesktopPreKeyStore(dbRunner: DBActionRunner) extends PreKeyStore {

  implicit val ec = ExecutionContext.global
  val defaultTimeout = 10.seconds

  override def containsPreKey(preKeyId: Int): Boolean = {
    dbRunner.run(PreKeys.exists(preKeyId))
  }

  override def loadPreKey(preKeyId: Int): PreKeyRecord = {
    dbRunner.run(PreKeys.get(preKeyId))
  }

  override def removePreKey(preKeyId: Int): Unit = {
    dbRunner.run(PreKeys.delete(preKeyId))
    ()
  }

  override def storePreKey(preKeyId: Int, record: PreKeyRecord): Unit = {
    val saveAction = PreKeys.preKeys.insertOrUpdate(record)
    dbRunner.run(saveAction)
    ()
  }
}
