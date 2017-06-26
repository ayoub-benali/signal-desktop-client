package de.m7w3.signal.store

import java.util

import de.m7w3.signal.store.model.SignedPreKeys
import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.state.{SignedPreKeyRecord, SignedPreKeyStore}
import slick.jdbc.H2Profile.api._

case class SignalDesktopSignedPreKeyStore(dbRunner: DBActionRunner) extends SignedPreKeyStore {

  override def storeSignedPreKey(signedPreKeyId: Int, record: SignedPreKeyRecord): Unit = {
    dbRunner.run(SignedPreKeys.upsert(signedPreKeyId, record))
    ()
  }

  override def loadSignedPreKey(signedPreKeyId: Int): SignedPreKeyRecord = {
    dbRunner.run(SignedPreKeys.get(signedPreKeyId)).getOrElse(
      throw new InvalidKeyIdException("no corresponding SignedPreKeyRecord")
    ).key
  }

  override def loadSignedPreKeys(): util.List[SignedPreKeyRecord] = {
    import scala.collection.JavaConverters.seqAsJavaListConverter
    dbRunner.run(SignedPreKeys.all).map(_.key).asJava
  }

  override def containsSignedPreKey(signedPreKeyId: Int): Boolean = {
    dbRunner.run(SignedPreKeys.exists(signedPreKeyId))
  }

  override def removeSignedPreKey(signedPreKeyId: Int): Unit = {
    dbRunner.run(SignedPreKeys.delete(signedPreKeyId))
    ()
  }

  def incrementAndGetSignedPreKeyId(): Int = {
    dbRunner.run(SignedPreKeys.idSequence.next.result)
  }

  def getSignedPreKeyId: Int = {
    dbRunner.run(SignedPreKeys.idSequence.curr.result)
  }
}
