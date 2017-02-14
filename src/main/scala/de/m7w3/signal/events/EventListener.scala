package de.m7w3.signal.events

import de.m7w3.signal.Logging
import monix.execution.Ack
import monix.execution.Ack.Continue
import monix.reactive.Observer

import scala.concurrent.Future

trait EventListener extends Observer[SignalDesktopEvent]

/**
  * very simplified observer that always returns "Continue"
  * and does not do any means to ensure backpressure
  */
abstract class SimpleEventListener(val name: String = getClass.getSimpleName) extends EventListener with Logging {

  def onEvent(event: SignalDesktopEvent): Unit

  override def onNext(elem: SignalDesktopEvent): Future[Ack] = {
    onEvent(elem)
    Continue
  }

  override def onError(ex: Throwable): Unit = {
    logger.error(s"Error in event listener $name", ex)
  }

  override def onComplete(): Unit = {
    logger.info(s"$name completed")
  }
}

object EventListener {
  def simple(onEvent: (SignalDesktopEvent) => Unit): EventListener = {
    new SimpleEventListener() {
      override def onEvent(event: SignalDesktopEvent): Unit = onEvent _
    }
  }
}
