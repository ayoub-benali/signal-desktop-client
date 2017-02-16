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
  def register(listener: EventListener): Cancelable
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

  override def publishEvent(event: SignalDesktopEvent): Future[Ack] = subject.onNext(event)

  override def register(listener: EventListener): Cancelable = subject.subscribe(listener)

  def close(): Unit = subject.onComplete()
}
