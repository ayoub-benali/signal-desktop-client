package de.m7w3.signal.messages

import java.time.Clock

import de.m7w3.signal.account.{AccountHelper, PreKeyRefreshResult}
import de.m7w3.signal.events.PreKeyEvent
import de.m7w3.signal.store.SignalDesktopProtocolStore
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import org.whispersystems.libsignal.state.SignedPreKeyRecord
import org.whispersystems.signalservice.api.messages.{SignalServiceContent, SignalServiceEnvelope}

import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.concurrent.duration._

class PreKeyRefreshListenerSpec extends FlatSpec with Matchers with MockitoSugar {

  behavior of classOf[PreKeyRefreshListener].getSimpleName

  val clock = Clock.fixed(Clock.systemUTC().instant(), Clock.systemUTC().getZone)

  it should "should refresh PreKeys if less than 10 are available" in {

    val accountHelper = mock[AccountHelper]
    when(accountHelper.countAvailablePreKeys()).thenReturn(9)
    val result = mock[PreKeyRefreshResult]
    val signedPreKey = mock[SignedPreKeyRecord]
    when(signedPreKey.getId).thenReturn(42)
    when(result.signedPreKeyRecord).thenReturn(signedPreKey)
    when(accountHelper.refreshPreKeys(ArgumentMatchers.any())).thenReturn(result)

    val store = mock[SignalDesktopProtocolStore]
    val listener = new PreKeyRefreshListener(accountHelper, store, clock)
    listener.onNext(PreKeyEvent(mock[SignalServiceEnvelope], new SignalServiceContent()))

    verify(accountHelper, times(1)).refreshPreKeys(store)
  }

  it should "should not refresh PreKeys if at least 10 are available" in {

    val accountHelper = mock[AccountHelper]
    when(accountHelper.countAvailablePreKeys()).thenReturn(10)
    val result = mock[PreKeyRefreshResult]
    val signedPreKey = mock[SignedPreKeyRecord]
    when(signedPreKey.getId).thenReturn(42)
    when(result.signedPreKeyRecord).thenReturn(signedPreKey)
    when(accountHelper.refreshPreKeys(ArgumentMatchers.any())).thenReturn(result)

    val store = mock[SignalDesktopProtocolStore]
    val listener = new PreKeyRefreshListener(accountHelper, store, clock)
    listener.onNext(PreKeyEvent(mock[SignalServiceEnvelope], new SignalServiceContent()))

    verify(accountHelper, never).refreshPreKeys(store)
  }

  it should "should not refresh PreKeys if many keys are available" in {

    val accountHelper = mock[AccountHelper]
    when(accountHelper.countAvailablePreKeys()).thenReturn(100)
    val result = mock[PreKeyRefreshResult]
    val signedPreKey = mock[SignedPreKeyRecord]
    when(signedPreKey.getId).thenReturn(42)
    when(result.signedPreKeyRecord).thenReturn(signedPreKey)
    when(accountHelper.refreshPreKeys(ArgumentMatchers.any())).thenReturn(result)

    val store = mock[SignalDesktopProtocolStore]
    val listener = new PreKeyRefreshListener(accountHelper, store, clock)
    listener.onNext(PreKeyEvent(mock[SignalServiceEnvelope], new SignalServiceContent()))

    verify(accountHelper, never).refreshPreKeys(store)
  }

  it should "only delete prekeys that are older than 7 days" in {

    val activeKey = mock[SignedPreKeyRecord]
    when(activeKey.getId).thenReturn(0)
    when(activeKey.getTimestamp).thenReturn(clock.millis())

    val freshKey = mock[SignedPreKeyRecord]
    when(freshKey.getId).thenReturn(1)
    when(freshKey.getTimestamp).thenReturn((clock.millis().millis - (1 day)).toMillis)

    val oldKey = mock[SignedPreKeyRecord]
    when(oldKey.getId).thenReturn(2)
    when(oldKey.getTimestamp).thenReturn((clock.millis().millis - (8 days)).toMillis)

    val accountHelper = mock[AccountHelper]
    when(accountHelper.countAvailablePreKeys()).thenReturn(7)
    val result = mock[PreKeyRefreshResult]
    when(result.signedPreKeyRecord).thenReturn(activeKey)
    when(accountHelper.refreshPreKeys(ArgumentMatchers.any())).thenReturn(result)

    val store = mock[SignalDesktopProtocolStore]
    when(store.loadSignedPreKeys()).thenReturn(List(freshKey, oldKey, activeKey).asJava)
    val listener = new PreKeyRefreshListener(accountHelper, store, clock)
    listener.onNext(PreKeyEvent(mock[SignalServiceEnvelope], new SignalServiceContent()))

    verify(accountHelper, times(1)).refreshPreKeys(store)
    verify(store, never).removeSignedPreKey(0)
    verify(store, never).removeSignedPreKey(1)
    verify(store, times(1)).removeSignedPreKey(2)
  }
}
