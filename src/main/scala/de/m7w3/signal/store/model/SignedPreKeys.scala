package de.m7w3.signal.store.model


import org.whispersystems.libsignal.state.SignedPreKeyRecord
import org.whispersystems.libsignal.util.Medium
import slick.dbio.Effect
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._
import slick.lifted.ProvenShape
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction, SqlAction}

case class SignedPreKey(id: Int, keyBytes: Array[Byte]) {
  lazy val key = new SignedPreKeyRecord(keyBytes)
}

class SignedPreKeys(tag: Tag) extends Table[SignedPreKey](tag, "SIGNED_PRE_KEYS") {
  def id: Rep[Int] = column[Int]("ID", O.PrimaryKey)
  def key: Rep[Array[Byte]] = column[Array[Byte]]("KEY")

  override def * : ProvenShape[SignedPreKey] = (id, key) <> (SignedPreKey.tupled, SignedPreKey.unapply)
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

  def get(keyId: Int): SqlAction[Option[SignedPreKey], NoStream, Effect.Read] = {
    signedPreKeys.filter(_.id === keyId).result.headOption
  }

  def exists(keyId: Int): FixedSqlAction[Boolean, H2Profile.api.NoStream, Effect.Read] = {
    signedPreKeys.filter(_.id === keyId).exists.result
  }

  def delete(keyId: Int): FixedSqlAction[Int, NoStream, Effect.Write] = {
    signedPreKeys.filter(_.id === keyId).delete
  }

  def all: FixedSqlStreamingAction[Seq[SignedPreKey], SignedPreKey, Effect.Read] = signedPreKeys.result

  def upsert(keyId: Int, record: SignedPreKeyRecord): FixedSqlAction[Int, NoStream, Effect.Write] = {
    signedPreKeys.insertOrUpdate(SignedPreKey(keyId, record.serialize()))
  }
}
