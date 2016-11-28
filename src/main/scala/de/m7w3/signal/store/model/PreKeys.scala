package de.m7w3.signal.store.model

import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.util.Medium
import slick.driver.H2Driver.api._

class PreKeys(tag: Tag) extends Table[PreKeyRecord](tag, "PRE_KEYS") {

  def id = column[Int]("ID", O.PrimaryKey)
  def preKey = column[Array[Byte]]("PRE_KEY")

  override def * = (id, preKey) <> (
    (tuple: (Int, Array[Byte])) => new PreKeyRecord(tuple._2),
    (preKeyRecord: PreKeyRecord) => Some((preKeyRecord.getId, preKeyRecord.serialize()))
    )
}

object PreKeys {
  val SEQUENCE_NAME = "PRE_KEYS_ID_SEQUENCE"
  val MAX_VALUE = Medium.MAX_VALUE
  val idSequence = Sequence[Int](SEQUENCE_NAME)
    .start(0)
    .inc(1)
    .max(MAX_VALUE)
    .cycle

  val preKeys = TableQuery[PreKeys]

  def exists(keyId: Int) = {
    preKeys.filter(_.id === keyId).exists.result
  }

  def get(keyId: Int) = {
    PreKeys.preKeys.filter(_.id === keyId).result.headOption
  }

  def delete(keyId: Int) = {
    PreKeys.preKeys.filter(_.id === keyId).delete
  }
}
