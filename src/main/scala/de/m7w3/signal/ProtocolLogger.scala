package de.m7w3.signal

import org.whispersystems.libsignal.logging.SignalProtocolLogger

import org.slf4j.LoggerFactory
class ProtocolLogger extends SignalProtocolLogger {

  private val logger = LoggerFactory.getLogger(getClass)

  override def log(priority: Int, tag: String, message: String): Unit = {
    val msg = s"[$tag] $message"
    val level = priority match {
      case SignalProtocolLogger.ERROR => logger.error(msg)
      case SignalProtocolLogger.WARN => logger.warn(msg)
      case SignalProtocolLogger.INFO => logger.info(msg)
      case SignalProtocolLogger.DEBUG => logger.debug(msg)
      case SignalProtocolLogger.VERBOSE|SignalProtocolLogger.ASSERT => logger.trace(msg)
    }
  }
}
