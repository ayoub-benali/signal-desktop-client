package de.m7w3.signal.store.model

import org.whispersystems.libsignal.{IdentityKey, SignalProtocolAddress}
import slick.driver.H2Driver.api._


class TrustedKeys(tag: Tag) extends Table[(Int, Array[Byte], Int)](tag, "TRUSTED_KEYS"){
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def pubKey = column[Array[Byte]]("PUB_KEY")
  def addressId = column[Int]("ADDRESS_ID")

  def address = foreignKey("TRUSTED_KEYS_ADDRESS_FK", addressId, Addresses.addresses)(_.id)

  override def * = (id, pubKey, addressId)
}

object TrustedKeys {
  def exists(address: SignalProtocolAddress, identityKey: IdentityKey) = {
    val addressId = address.hashCode()
    val pubKeyBytes = identityKey.serialize()
    trustedKeys.filter(trustedKey => {
      trustedKey.addressId === addressId && trustedKey.pubKey === pubKeyBytes
    }).exists.result
  }

  def get(address: SignalProtocolAddress) = {
    val addressId = address.hashCode()
    val join = trustedKeys join Addresses.addresses on (_.addressId === _.id)
    join.filter(_._1.addressId === addressId).take(1).result
  }

  val trustedKeys = TableQuery[TrustedKeys]

  def upsert(address: SignalProtocolAddress, trustedKey: IdentityKey) = {
    val IGNORED = 42
    val addressId = address.hashCode()
    (trustedKeys returning trustedKeys.map(_.id)).insertOrUpdate((IGNORED, trustedKey.serialize(), addressId))
  }
}
