package de.m7w3.signal.store

import de.m7w3.signal.store.model._
import org.whispersystems.libsignal.state.IdentityKeyStore
import org.whispersystems.libsignal.{IdentityKey, IdentityKeyPair, SignalProtocolAddress}
import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext

case class SignalDesktopIdentityKeyStore(dbRunner: DBActionRunner) extends IdentityKeyStore {

  implicit val ec = ExecutionContext.global

  var identity: Option[Identity] = None

  // one time initialization, once the local key has been created
  def initialize(identity: Identity): Unit = {
    if (!dbRunner.run(LocalIdentity.exists(identity))) {
      dbRunner.run(LocalIdentity.insert(identity))
      this.identity = Some(identity)
    }
  }

  private def loadIdentity(): Identity = {
    identity match {
      case Some(id) => id
      case None =>
        val storedId = dbRunner.run(LocalIdentity.localIdentity)
        identity = Some(storedId)
        storedId
    }
  }

  override def getIdentityKeyPair: IdentityKeyPair = {
    loadIdentity().keyPair
  }

  override def isTrustedIdentity(address: SignalProtocolAddress, identityKey: IdentityKey): Boolean = {
    val identities = dbRunner.run(TrustedKeys.get(address))
    identities match {
      case Seq(((_, pubKey: Array[Byte], _), address: Address), xs @ _*) =>
        val dbKey = new IdentityKey(pubKey, 0)
        dbKey.equals(identityKey) // not trusted if not equal
      case Seq() =>
        true // trust on first use
    }
  }

  override def getLocalRegistrationId: Int = {
    loadIdentity().registrationId
  }

  override def saveIdentity(address: SignalProtocolAddress, identityKey: IdentityKey): Unit = {
    val saveAction = for {
        _ <- Addresses.upsert(address) // make sure it is stored
        _ <- TrustedKeys.upsert(address, identityKey)
      } yield ()

    dbRunner.run(saveAction.transactionally)
    ()
  }
}
