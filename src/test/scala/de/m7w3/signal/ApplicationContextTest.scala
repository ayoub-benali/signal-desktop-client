package de.m7w3.signal

import de.m7w3.signal.Config.SignalDesktopConfig
import scala.concurrent.duration._
import java.io.File

object ApplicationContextTest extends ApplicationContext(SignalDesktopConfig(false, 1.seconds, new File("foo")))