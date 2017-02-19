package de.m7w3.signal.events

import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global
import monix.execution.atomic.Atomic
import monix.reactive.Observable
import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import org.scalatest.{FlatSpec, Matchers}


class SignalDesktopEventDispatcherSpec extends FlatSpec
  with Matchers
  with Eventually
  with IntegrationPatience {

  behavior of classOf[SignalDesktopEventDispatcher].getSimpleName


  it should "deliver events to registered listener" in {
    val dispatcher = new SignalDesktopEventDispatcher
    val listener = CountingEventListener()
    val registration = dispatcher.register(listener)
    val event = TestEvent("foo")

    publish(Observable.repeat(event).take(10), dispatcher)
    eventually {
      listener.count shouldBe 10L
    }
  }

  it should "only dispatch requested events" in {
    val dispatcher = new SignalDesktopEventDispatcher
    val fooListener = CountingEventListener(Some("foo"))
    val barListener = CountingEventListener(Some("bar"))
    val fooRegistration = dispatcher.register(fooListener)
    val barRegistration = dispatcher.register(barListener)

    val fooEvent = TestEvent("foo")
    val barEvent = TestEvent("bar")
    val bazEvent = TestEvent("baz")

    val events = Observable.concat(
      Observable.repeat(fooEvent).take(11),
      Observable.repeat(barEvent).take(12),
      Observable.repeat(bazEvent).take(13)
    )
    publish(events, dispatcher)
    eventually {
      fooListener.count shouldBe 11L
      barListener.count shouldBe 12L
    }
  }

  it should "not deliver events after close" in {
    val dispatcher = new SignalDesktopEventDispatcher
    val listener = CountingEventListener()
    val registration = dispatcher.register(listener)
    val event = TestEvent("foo")

    listener.count shouldBe 0L

    publish(Observable.repeat(event).take(5), dispatcher)
    eventually {
      listener.count shouldBe 5L
    }

    dispatcher.close()

    publish(Observable.repeat(event).take(5), dispatcher)

    // this might not catch all cases, due to race conditions
    // but if the dispatcher is correct this should never fail
    Thread.sleep(10)
    listener.count shouldBe 5L
  }

  def publish(events: Observable[SignalDesktopEvent],
              dispatcher: SignalDesktopEventDispatcher): CancelableFuture[Unit] = {
    events.foreach((event) => {
      dispatcher.publishEvent(event)
      ()
    })
  }

  case class TestEvent(eventType: String) extends SignalDesktopEvent

  case class CountingEventListener(consider: Option[String] = None) extends SimpleEventListener {
    private val counter = Atomic(0L)

    override def handle: PartialFunction[SignalDesktopEvent, Unit] = {
      case TestEvent(eventType) =>
        if (consider.forall(_ == eventType)) {
          counter.increment()
        }
      }

    def count: Long = counter.get
  }
}
