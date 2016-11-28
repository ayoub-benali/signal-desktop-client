package de.m7w3.signal.store.model


import org.whispersystems.libsignal.state.SignedPreKeyRecord
import org.whispersystems.libsignal.util.Medium
import slick.driver.H2Driver.api._

case class SignedPreKey(id: Int, keyBytes: Array[Byte]) {
  lazy val key = new SignedPreKeyRecord(keyBytes)
}

class SignedPreKeys(tag: Tag) extends Table[SignedPreKey](tag, "SIGNED_PRE_KEYS") {
  def id = column[Int]("ID", O.PrimaryKey)
  def key = column[Array[Byte]]("KEY")

  override def * = (id, key) <> (SignedPreKey.tupled, SignedPreKey.unapply)
}

object SignedPreKeys {

  val SEQUENCE_NAME = "SIGNED_PRE_KEYS_ID_SEQUENCE"
  val MAX_VALUE = Medium.MAX_VALUE
  val idSequence = Sequence[Int](SEQUENCE_NAME)
                        .start(0)
                        .inc(1)
                        .max(MAX_VALUE)
                        .cycle

  val signedPreKeys = TableQuery[SignedPreKeys]

  def get(keyId: Int) = {
    signedPreKeys.filter(_.id === keyId).result.headOption
  }

  def exists(keyId: Int) = {
    signedPreKeys.filter(_.id === keyId).exists.result
  }

  def delete(keyId: Int) = {
    signedPreKeys.filter(_.id === keyId).delete
  }

  def all = signedPreKeys.result

  def upsert(keyId: Int, record: SignedPreKeyRecord) = {
    signedPreKeys.insertOrUpdate(SignedPreKey(keyId, record.serialize()))
  }
}
