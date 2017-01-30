package de.m7w3.signal

import java.io.File
import java.util.concurrent.atomic.AtomicReference

import de.m7w3.signal.Config.SignalDesktopConfig
import de.m7w3.signal.resources.StoreResource
import de.m7w3.signal.store.SignalDesktopProtocolStore
import org.junit.rules.ExternalResource

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class ApplicationContextRule extends ExternalResource with StoreResource {

  private val context = new AtomicReference[InitialContext]()
  val defaultPassword = "foo"
  override def before(): Unit = {
    super.before()
    setupResource()
    context.set(
      new InitialContext(SignalDesktopConfig(verbose=false, 1.seconds, new File("foo"))){
        override def profileDirExists: Boolean = true
        override def profileIsInitialized: Boolean = true
        override def createNewProtocolStore(password: String): SignalDesktopProtocolStore = protocolStore
        override def tryLoadExistingStore(password: String, skipCache: Boolean): Try[SignalDesktopProtocolStore] = {
          if (password == defaultPassword) Success(protocolStore)
          else Failure(new Throwable("wrong password"))
        }
    })
  }
  override def after(): Unit = {
    tearDownResource()
    super.after()
  }

  def get(): InitialContext = {
    val ctx = context.get()
    require(ctx != null, "ApplicationCOntext not yet initialized")
    ctx
  }
}
