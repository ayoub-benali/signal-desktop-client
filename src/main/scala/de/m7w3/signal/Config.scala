package de.m7w3.signal

import java.io.File

import scopt.OptionParser

import scala.annotation.tailrec
import scala.concurrent.duration._

object Config {

  case class SignalDesktopConfig(verbose: Boolean = false,
                                 databaseTimeout: Duration = 10.seconds,
                                 profileDir: File = Directories.defaultProfileDir)

  lazy val optionParser = new OptionParser[SignalDesktopConfig](App.NAME) {
    head(App.NAME, App.VERSION)

    opt[Unit]("verbose")
    .action( (_, c) => c.copy(verbose = true))
    .text("provide verbose output on console")

    opt[Long]("database-timeout")
    .text("timeout for database queries")
    .valueName("<database-timeout>")
    .validate(timeoutMillis =>
      if (timeoutMillis <= 0) Left(s"invalid timeout of $timeoutMillis ms")
      else Right(())
    )
    .action( (timeoutMillis, c) => c.copy(databaseTimeout = timeoutMillis.milliseconds))


    opt[File]('p', "profile-dir").valueName("<profileDir>").action( (profileDir, config) =>
      config.copy(profileDir = profileDir)
    ).validate((profileDir: File) => {
      if (!firstExistingParent(profileDir).canWrite) Left(s"profile directory $profileDir is not writable")
      else Right(())
    })
    .text("profile directory containing profile information and messages")
  }

  @tailrec
  def firstExistingParent(f: File): File = {
    if (f.exists()) {
      if (f.isDirectory) f
      else f.getParentFile
    } else {
      firstExistingParent(f.getParentFile)
    }
  }
}
