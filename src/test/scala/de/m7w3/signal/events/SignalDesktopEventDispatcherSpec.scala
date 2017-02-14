package de.m7w3.signal.events

import monix.execution.Scheduler.Implicits.global
import monix.execution.atomic.Atomic
import monix.reactive.Observable
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._


class SignalDesktopEventDispatcherSpec extends FlatSpec with Matchers {

  behavior of classOf[SignalDesktopEventDispatcher].getSimpleName

  it should "deliver events to registered listener" in {
    val dispatcher = new SignalDesktopEventDispatcher()
    val listener = new CountingEventListener
    val registration = dispatcher.register(listener)
    val event = new SignalDesktopEvent {
      override def eventType = "foo"
    }

    val events = Observable.repeat(event).take(10)
    val cancelable = events.foreach(
      dispatcher.publishEvent(_)
    )
    Await.ready(cancelable, 10 seconds)
    listener.count shouldBe 10L
  }

  it should "only dispatch requested events" in {
    val dispatcher = new SignalDesktopEventDispatcher()
    val fooListener = new CountingEventListener
    val barListener = new CountingEventListener
    val fooRegistration = dispatcher.register(fooListener, "foo")
    val barRegistration = dispatcher.register(barListener, "bar")

    val fooEvent = new SignalDesktopEvent {
      override def eventType = "foo"
    }
    val barEvent = new SignalDesktopEvent {
      override def eventType = "bar"
    }
    val bazEvent = new SignalDesktopEvent {
      override def eventType = "baz"
    }

    val events = Observable.concat(
      Observable.repeat(fooEvent).take(11),
      Observable.repeat(barEvent).take(12),
      Observable.repeat(bazEvent).take(13)
    )
    val cancelable = events.foreach((event) => {
      dispatcher.publishEvent(event)
    })
    Await.ready(cancelable, 10 seconds)
    fooListener.count shouldBe 11L
    barListener.count shouldBe 12L
  }

  class CountingEventListener extends SimpleEventListener {
    private val counter = Atomic(0L)
    override def onEvent(event: SignalDesktopEvent): Unit = {
      counter.increment()
    }
    def count: Long = counter.get
  }
}
