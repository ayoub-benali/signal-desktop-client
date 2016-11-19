package de.m7w3.signal.store.model


import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.SessionRecord
import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext


/**
  * session without old states
  * TODO: convenient way to obtain old states
  */
case class Session(id: Option[Int] = None, recordBytes: Array[Byte], addressId: Int) {
  lazy val record = new SessionRecord(recordBytes)
}

class Sessions(tag: Tag) extends Table[Session](tag, "SESSIONS") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def addressId = column[Int]("ADDRESS_ID")
  def record = column[Array[Byte]]("RECORD")

  def address = foreignKey("SESSIONS_ADDRESS_FK", addressId, Addresses.addresses)(_.id)

  override def * = (id.?, record, addressId) <> (
    Session.tupled,
    Session.unapply
  )
}

object Sessions {
  val sessions = TableQuery[Sessions]

  def get(address: SignalProtocolAddress) = {
    val addressId = address.hashCode()
    sessions.filter(_.addressId === addressId).result.head
  }

  def exists(address: SignalProtocolAddress) = {
    val addressId = address.hashCode()
    sessions.filter(_.addressId === addressId).exists.result
  }

  def upsert(address: SignalProtocolAddress, record: SessionRecord) = {
    implicit val ec = ExecutionContext.global
    val addressId = address.hashCode()
    (
      for {
        _ <- Addresses.upsert(address) // ensure address is stored
        _ <- sessions.insertOrUpdate(Session(None, record.serialize(), addressId))
      } yield ()).transactionally
  }

  def delete(address: SignalProtocolAddress) = {
    val addressId = address.hashCode()
    sessions.filter(_.addressId === addressId).delete
  }

  def deleteByRemoteClientName(name: String) = {
    val join = sessions join Addresses.addresses on (_.addressId === _.id)
    join.filter(_._2.name === name).map(_._1).delete
  }

  def getSessionDevices(remoteClientName: String) = {
    val join = sessions join Addresses.addresses on (_.addressId === _.id)
    join.filter(_._2.name === remoteClientName).map(_._2.deviceId).result
  }
}
