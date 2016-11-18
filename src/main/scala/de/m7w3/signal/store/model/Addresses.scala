package de.m7w3.signal.store.model


import org.whispersystems.libsignal.SignalProtocolAddress
import slick.driver.H2Driver.api._

case class Address(id: Int, signalAddress: SignalProtocolAddress)

object Address {
  def apply(address: SignalProtocolAddress): Address = {
    new Address(address.hashCode(), address)
  }
}

class Addresses(tag: Tag) extends Table[Address](tag, "ADDRESSES") {
  def id = column[Int]("ID", O.PrimaryKey)
  def name = column[String]("NAME")
  def deviceId = column[Int]("DEVICE_ID")

  override def * = (id, name, deviceId) <> (
    (tuple: (Int, String, Int)) => Address(tuple._1, new SignalProtocolAddress(tuple._2, tuple._3)),
    (address: Address) => Some((address.id, address.signalAddress.getName, address.signalAddress.getDeviceId))
  )
}

object Addresses {
  val addresses = TableQuery[Addresses]

  def upsert(address: SignalProtocolAddress) = {
    (addresses returning addresses).insertOrUpdate(Address(address))
  }
}
