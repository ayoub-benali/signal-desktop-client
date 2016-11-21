package de.m7w3.signal.store

import de.m7w3.signal.store.model.{Addresses, Identity, LocalIdentity, TrustedKeys}
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
    if (identity.isEmpty) {
      val loadedId = dbRunner.run(LocalIdentity.localIdentity)
      identity = Some(loadedId)
    }
    identity.get
  }

  override def getIdentityKeyPair: IdentityKeyPair = {
    loadIdentity().keyPair
  }

  override def isTrustedIdentity(address: SignalProtocolAddress, identityKey: IdentityKey): Boolean = {
    val existsAction = TrustedKeys.exists(address, identityKey)
    dbRunner.run(existsAction)
  }

  override def getLocalRegistrationId: Int = {
    loadIdentity().registrationId
  }

  override def saveIdentity(address: SignalProtocolAddress, identityKey: IdentityKey): Unit = {
    val saveAction = ( for {
      _ <- Addresses.upsert(address) // make sure it is stored
      _ <- TrustedKeys.upsert(address, identityKey)
    } yield () ).transactionally
    dbRunner.run(saveAction)
    ()
  }
}
