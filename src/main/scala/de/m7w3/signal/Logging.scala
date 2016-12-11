package de.m7w3.signal

import org.slf4j.LoggerFactory

trait Logging {
  lazy val logger = LoggerFactory.getLogger(getClass)

  def debug(format: String, args: Any*): Unit = {
    logger.debug(format, args)
  }

  def info(format: String, args: Any*): Unit = {
    logger.info(format, args)
  }

  def warn(format: String, args: Any*): Unit = {
    logger.warn(format, args)
  }

  def error(format: String, exception: Exception): Unit = {
    logger.error(format, exception)
  }

  def error(format: String): Unit = {
    logger.error(format)
  }
}
