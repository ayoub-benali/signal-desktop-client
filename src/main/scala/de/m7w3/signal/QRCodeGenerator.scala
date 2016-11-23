package de.m7w3.signal

import net.glxn.qrgen.javase.QRCode
import java.io.ByteArrayOutputStream

object QRCodeGenerator {
  def generate(getUrl: () => String): ByteArrayOutputStream = {
    QRCode.from(getUrl()).stream
  }
}
