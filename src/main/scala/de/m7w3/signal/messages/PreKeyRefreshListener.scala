package de.m7w3.signal.messages

import java.time.Clock

import de.m7w3.signal.account.{AccountHelper, PreKeyRefreshResult}
import de.m7w3.signal.Logging
import de.m7w3.signal.events.{PreKeyEvent, SignalDesktopEvent, SimpleEventListener}
import de.m7w3.signal.store.SignalDesktopProtocolStore
import org.whispersystems.libsignal.state.SignedPreKeyRecord

import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._

/**
  * listens on PreKeyEvent(...)
  *
  * refresh prekeys
  * if less than 10 prekeys available - refresh:
  *   create new prekeys, new signed prekey and publish using accountmanager.setPreKeys
  *   clean prekeys:
  *     - delete signedprekeys that are older than 7 days and not active anymore
  */
class PreKeyRefreshListener(accountHelper: AccountHelper,
                            protocolStore: SignalDesktopProtocolStore,
                            clock: Clock = Clock.systemUTC()) extends SimpleEventListener with Logging {

  override def handle: PartialFunction[SignalDesktopEvent, Unit] = {
    case PreKeyEvent(envelope, content) =>
      if (accountHelper.countAvailablePreKeys() < PreKeyRefreshListener.MIN_KEYS) {
        logger.debug("refreshing prekeys...")
        val tryRefresh = Try {
          accountHelper.refreshPreKeys(protocolStore)
        }
        val result = for {
          PreKeyRefreshResult(_, _, signedPreKey) <- tryRefresh
        } yield {
          cleanSignedPreKeys(signedPreKey)
        }
        result match {
          case Success(_) =>
            logger.debug("prekeys refreshed.")
          case Failure(t) =>
            logger.error("could not refresh prekeys", t)
        }

      } else {
        logger.debug("enough prekeys left, no need to refresh.")
      }
  }

  def cleanSignedPreKeys(activeSignedPreKey: SignedPreKeyRecord): Unit = {
    logger.debug("cleaning out old signed prekeys...")
    val signedPreKeys = protocolStore.loadSignedPreKeys().asScala

    val cleanMePredicate = (spk: SignedPreKeyRecord) => {
      val age = clock.millis() - spk.getTimestamp
      val isOld = age > PreKeyRefreshListener.SIGNED_PREKEY_DELETE_AGE
      val isActive = spk.getId == activeSignedPreKey.getId
      isOld && !isActive
    }

    signedPreKeys
      .filter(cleanMePredicate)
      .foreach(key => {
        logger.debug(s"cleaning out signedPreKey: ${key.getId}")
        protocolStore.removeSignedPreKey(key.getId)
      })
    logger.debug("cleaned out old signed prekeys.")

  }
}

object PreKeyRefreshListener {
  val MIN_KEYS: Int = 10
  val SIGNED_PREKEY_DELETE_AGE: Long = (7 days).toMillis
}
