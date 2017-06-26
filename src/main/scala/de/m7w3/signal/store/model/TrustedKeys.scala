package de.m7w3.signal.store.model

import org.whispersystems.libsignal.{IdentityKey, SignalProtocolAddress}
import slick.dbio.Effect
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}


class TrustedKeys(tag: Tag) extends Table[(Int, Array[Byte], Int)](tag, "TRUSTED_KEYS"){
  def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def pubKey: Rep[Array[Byte]] = column[Array[Byte]]("PUB_KEY")
  def addressId: Rep[Int] = column[Int]("ADDRESS_ID")

  def address: ForeignKeyQuery[Addresses, Address] = foreignKey("TRUSTED_KEYS_ADDRESS_FK", addressId, Addresses.addresses)(_.id)

  override def * : ProvenShape[(Int, Array[Byte], Int)] = (id, pubKey, addressId)
}

object TrustedKeys {
  def exists(address: SignalProtocolAddress, identityKey: IdentityKey): FixedSqlAction[Boolean, H2Profile.api.NoStream, Effect.Read] = {
    val addressId = address.hashCode()
    val pubKeyBytes = identityKey.serialize()
    trustedKeys.filter(trustedKey => {
      trustedKey.addressId === addressId && trustedKey.pubKey === pubKeyBytes
    }).exists.result
  }

  def get(address: SignalProtocolAddress): FixedSqlStreamingAction[Seq[((Int, Array[Byte], Int), Address)], ((Int, Array[Byte], Int), Address), Effect.Read] = {
    val addressId = address.hashCode()
    val join = trustedKeys join Addresses.addresses on (_.addressId === _.id)
    join.filter(_._1.addressId === addressId).take(1).result
  }

  val trustedKeys: TableQuery[TrustedKeys] = TableQuery[TrustedKeys]

  def upsert(address: SignalProtocolAddress, trustedKey: IdentityKey): FixedSqlAction[Option[Int], NoStream, Effect.Write] = {
    val IGNORED = 42
    val addressId = address.hashCode()
    (trustedKeys returning trustedKeys.map(_.id)).insertOrUpdate((IGNORED, trustedKey.serialize(), addressId))
  }
}
