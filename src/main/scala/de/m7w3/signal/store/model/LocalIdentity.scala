package de.m7w3.signal.store.model

import org.whispersystems.libsignal.IdentityKeyPair
import slick.driver.H2Driver.api._

case class Identity(registrationId: Int, keyBytes: Array[Byte]) {
  lazy val keyPair = new IdentityKeyPair(keyBytes)
}

class LocalIdentity(tag: Tag) extends Table[Identity](tag, "LOCAL_IDENTITY") {

  def id = column[Int]("ID", O.PrimaryKey) // TODO: maybe verify that number is between 1 and 16380
  def key = column[Array[Byte]]("KEY")


  override def * = (id, key) <> (Identity.tupled, Identity.unapply)
}

object LocalIdentity {
  val query = TableQuery[LocalIdentity]

  def localIdentity = {
    query.take(1).result.head
  }

  def exists(identity: Identity) = {
    query.filter(row => row.id === identity.registrationId && row.key === identity.keyBytes)
      .exists.result
  }

  def insert(identity: Identity) = {
    query += identity
  }
}
