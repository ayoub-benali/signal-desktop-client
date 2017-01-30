package de.m7w3.signal.store.model

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import org.h2.util.IOUtils
import org.whispersystems.libsignal.util.guava.Optional
import org.whispersystems.signalservice.api.messages.{SignalServiceAttachment, SignalServiceAttachmentStream}

object AttachmentHelpers {

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
