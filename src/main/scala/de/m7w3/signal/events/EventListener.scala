package de.m7w3.signal.events

import de.m7w3.signal.Logging
import monix.execution.Ack
import monix.execution.Ack.Continue
import monix.reactive.Observer

import scala.concurrent.Future

trait EventListener extends Observer[SignalDesktopEvent] {
  def handle: PartialFunction[SignalDesktopEvent, Unit]

  override def onNext(elem: SignalDesktopEvent): Future[Ack] = {
    if (handle.isDefinedAt(elem)) {
      handle.apply(elem)
    }
    Continue
  }
}

/**
  * very simplified observer that always returns "Continue"
  * and does not do any means to ensure backpressure
  */
abstract class SimpleEventListener extends EventListener with Logging {

  override def onError(ex: Throwable): Unit = {
    logger.error(s"Error in event listener ${getClass.getSimpleName}", ex)
  }

  override def onComplete(): Unit = {
    logger.info(s"${getClass.getSimpleName} completed")
  }
}

object EventListener {
  def simple(onEvent: PartialFunction[SignalDesktopEvent, Unit]): EventListener = {
    new SimpleEventListener() {
      override def handle: PartialFunction[SignalDesktopEvent, Unit] = onEvent
    }
  }
}
