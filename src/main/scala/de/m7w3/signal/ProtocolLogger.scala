package de.m7w3.signal

import org.apache.logging.log4j.{Level, LogManager}
import org.whispersystems.libsignal.logging.SignalProtocolLogger

class ProtocolLogger extends SignalProtocolLogger {

  private val logger = LogManager.getFormatterLogger(getClass)

  override def log(priority: Int, tag: String, message: String): Unit = {
    val level = priority match {
      case SignalProtocolLogger.ERROR => Level.ERROR
      case SignalProtocolLogger.WARN => Level.WARN
      case SignalProtocolLogger.INFO => Level.INFO
      case SignalProtocolLogger.DEBUG => Level.DEBUG
      case SignalProtocolLogger.VERBOSE => Level.TRACE
      case SignalProtocolLogger.ASSERT => Level.ALL
    }
    logger.log(level, s"[$tag] $message")
  }
}
