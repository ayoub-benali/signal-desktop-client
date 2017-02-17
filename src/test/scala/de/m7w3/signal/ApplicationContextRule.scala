package de.m7w3.signal

import java.io.File
import java.util.concurrent.atomic.AtomicReference

import account.AccountHelper
import de.m7w3.signal.Config.SignalDesktopConfig
import de.m7w3.signal.exceptions.DatabaseDoesNotExistException
import de.m7w3.signal.resources.StoreResource
import de.m7w3.signal.store.Addresses
import monix.execution.atomic.Atomic
import org.junit.rules.ExternalResource

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class ApplicationContextRule extends ExternalResource with StoreResource with Addresses {

  val defaultPassword = "foo"

  private val builder = new AtomicReference[ContextBuilder]()


  override def before(): Unit = {
    super.before()
    setupResource()
    val context = InitiatedContext(
      AccountHelper(localAddress.getName, defaultPassword),
      dbActionRunner,
      protocolStore,
      applicationStore
    )
    val contextRef = Atomic(Some(context).asInstanceOf[Option[ApplicationContext]])
    builder.set(
      new ContextBuilder(SignalDesktopConfig(verbose=false, 1.seconds, new File("foo"))) {
        override def profileDirExists: Boolean = true
        override def profileIsInitialized: Boolean = true
        override def buildWithExistingStore(password: String): Try[InitiatedContext] = {
          if (!password.equals(defaultPassword)) {
            Failure(new DatabaseDoesNotExistException("nope", null))
          } else {
            Success(context)
          }
        }
        override def buildWithNewStore(accountHelper: AccountHelper,
                                       password: String): Try[InitiatedContext] = Success(context)
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
