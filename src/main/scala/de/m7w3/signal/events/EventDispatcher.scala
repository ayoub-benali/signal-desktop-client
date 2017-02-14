package de.m7w3.signal.events

import monix.execution.Scheduler.Implicits.global
import monix.execution.{Ack, Cancelable}
import monix.reactive.OverflowStrategy.DropOld
import monix.reactive.subjects.ConcurrentSubject

import scala.concurrent.Future


trait EventPublisher {
  /**
    * returns a value that signals when this event has been handled
    * and if it is safe to issue more calls
    */
  def publishEvent(event: SignalDesktopEvent): Future[Ack]
}

trait EventDispatcher {
  def register(listener: EventListener, forTypes: String*): Cancelable
}

class SignalDesktopEventDispatcher extends EventPublisher with EventDispatcher {

  private val bufferSize: Int = 1024
  private val overflowStrategy = DropOld(bufferSize)
  // ensures concurrency,safety
  // and backpressure by maintaining a buffer
  // in this subject that sits before any subscriber
  // but has the drawback that we might execute stuff asynchronously
  private val subject: ConcurrentSubject[SignalDesktopEvent, SignalDesktopEvent] =
    ConcurrentSubject.publish[SignalDesktopEvent](overflowStrategy)

  override def publishEvent(event: SignalDesktopEvent): Future[Ack] = {
    subject.onNext(event)
  }

  override def register(listener: EventListener, forTypes: String*): Cancelable = {
    val observable = if (forTypes.isEmpty) {
      subject // no filtering if no types were given
    } else {
      subject.filter((event) => forTypes.contains(event.eventType))
    }
    observable.subscribe(listener)
  }

  def finish(): Unit = subject.onComplete()
}
