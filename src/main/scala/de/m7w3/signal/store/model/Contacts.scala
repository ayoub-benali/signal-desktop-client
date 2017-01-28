package de.m7w3.signal.store.model

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import org.h2.util.IOUtils
import org.whispersystems.libsignal.util.guava.Optional
import org.whispersystems.signalservice.api.messages.{SignalServiceAttachment, SignalServiceAttachmentStream}
import org.whispersystems.signalservice.api.messages.multidevice.DeviceContact
import slick.driver.H2Driver.api._
import slick.lifted.ProvenShape

class Contacts(tag: Tag) extends Table[DeviceContact](tag, "ADDRESSES") {
  def number = column[String]("NUMBER", O.PrimaryKey)
  def name = column[Option[String]]("NAME")
  def avatar = column[Option[Array[Byte]]]("AVATAR")
  def color = column[Option[String]]("COLOR")

  override def * : ProvenShape[DeviceContact] = (number, name, avatar, color) <> (
    (tuple: (String, Option[String], Option[Array[Byte]], Option[String])) =>
      new DeviceContact(
        tuple._1,
        Optional.fromNullable(tuple._2.orNull),
        Optional.fromNullable(tuple._3.map(Contacts.arrayToStream).orNull),
        Optional.fromNullable(tuple._4.orNull)),
    (deviceContact: DeviceContact) => Some((
        deviceContact.getNumber,
        Option(deviceContact.getName.orNull()),
        Contacts.streamToArray(deviceContact.getAvatar),
        Option(deviceContact.getColor.orNull())
      ))
  )
}

object Contacts {
  val contacts = TableQuery[Contacts]

  def insert(deviceContact: DeviceContact) = {
    contacts.insertOrUpdate(deviceContact)
  }

  def streamToArray(ssaStream: Optional[SignalServiceAttachmentStream]): Option[Array[Byte]] =
    if (ssaStream.isPresent) {
      val stream = ssaStream.get()
      val bos = new ByteArrayOutputStream()
      val in = stream.getInputStream
      in.reset()
      IOUtils.copy(in, bos)
      Some(bos.toByteArray)
    } else {
      None
    }

  def arrayToStream(array: Array[Byte]): SignalServiceAttachmentStream = {
    SignalServiceAttachment.newStreamBuilder()
      .withStream(new ByteArrayInputStream(array))
      .withLength(array.length)
      .withContentType("application/octet-stream")
      .build()
  }
}
