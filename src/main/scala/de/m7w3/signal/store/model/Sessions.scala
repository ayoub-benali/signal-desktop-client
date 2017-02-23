package de.m7w3.signal.store.model


import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.SessionRecord
import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext


/**
  * session without old states
  * TODO: convenient way to obtain old states
  */
case class Session(addressId: Int, recordBytes: Array[Byte]) {
  lazy val record = new SessionRecord(recordBytes)
}

class Sessions(tag: Tag) extends Table[Session](tag, "SESSIONS") {
  def id = column[Int]("ID", O.PrimaryKey)
  def record = column[Array[Byte]]("RECORD")

  def address = foreignKey("SESSIONS_ADDRESS_FK", id, Addresses.addresses)(_.id)

  override def * = (id, record) <> (
    Session.tupled,
    Session.unapply
  )
}

object Sessions {
  val sessions: TableQuery[Sessions] = TableQuery[Sessions]

  def get(address: SignalProtocolAddress) = {
    val addressId = address.hashCode()
    sessions.filter(_.id === addressId).result.headOption
  }

  def exists(address: SignalProtocolAddress) = {
    val addressId = address.hashCode()
    sessions.filter(_.id === addressId).exists.result
  }

  def upsert(address: SignalProtocolAddress, record: SessionRecord) = {
    implicit val ec = ExecutionContext.global
    val addressId = address.hashCode()

    val query = for {
      _ <- Addresses.upsert(address) // ensure address is stored
      _ <- sessions.insertOrUpdate(Session(addressId, record.serialize()))
    } yield ()

    query.transactionally
  }

  def delete(address: SignalProtocolAddress) = {
    val addressId = address.hashCode()
    sessions.filter(_.id === addressId).delete
  }

  def deleteByRemoteClientName(name: String) = {
    sessions.filter(session => { session.id in (
        session.address filter { _.name === name } map { _.id }
      )
    }).delete
  }

  def getSessionDevices(remoteClientName: String) = {
    val join = sessions join Addresses.addresses on (_.id === _.id)
    join.filter(_._2.name === remoteClientName).map(_._2.deviceId)
      .distinct
      .sortBy(identity)
      .result
  }
}
