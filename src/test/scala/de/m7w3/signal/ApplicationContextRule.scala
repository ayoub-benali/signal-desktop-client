package de.m7w3.signal

import java.io.File
import java.util.concurrent.atomic.AtomicReference

import account.{AccountHelper, AccountInitializationHelper}
import de.m7w3.signal.Config.SignalDesktopConfig
import de.m7w3.signal.exceptions.DatabaseDoesNotExistException
import de.m7w3.signal.resources.StoreResource
import de.m7w3.signal.store.Addresses
import de.m7w3.signal.store.model.Registration
import monix.execution.atomic.Atomic
import org.junit.rules.ExternalResource

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class ApplicationContextRule extends ExternalResource with StoreResource with Addresses {

  val defaultPassword = "foo"
  val signalingKey = Util.getSecret(52)

  private val builder = new AtomicReference[ContextBuilder]()


  override def before(): Unit = {
    super.before()
    setupResource()
    val context = ApplicationContext(
      TestMessageSender,
      AccountHelper(Registration(localAddress.getName, localAddress.getDeviceId, defaultPassword, signalingKey), TestMessageSender),
      dbActionRunner,
      protocolStore,
      applicationStore
    )
    val contextRef = Atomic(Some(context).asInstanceOf[Option[ApplicationContext]])
    builder.set(
      new ContextBuilder(SignalDesktopConfig(verbose=false, 1.seconds, new File("foo"))) {
        override def profileDirExists: Boolean = true
        override def profileIsInitialized: Boolean = true
        override def buildWithExistingStore(password: String): Try[ApplicationContext] = {
          if (!password.equals(defaultPassword)) {
            Failure(new DatabaseDoesNotExistException("nope", null))
          } else {
            Success(context)
          }
        }
        override def buildWithNewStore(accountInitHelper: AccountInitializationHelper,
                                       deviceName: String,
                                       password: String): Try[ApplicationContext] = Success(context)
      })
  }

  override def after(): Unit = {
    tearDownResource()
    super.after()
  }

  def get(): ContextBuilder = {
    val ctx = builder.get()
    require(ctx != null, "ApplicationContext not yet initialized")
    ctx
  }
}
