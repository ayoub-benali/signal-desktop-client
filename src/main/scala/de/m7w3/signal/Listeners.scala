package de.m7w3.signal

import de.m7w3.signal.events.EventListener
import de.m7w3.signal.messages.PreKeyRefreshListener


/**
  * A place for generic event listeners that are being initialized once the application context is available
  *
  * Placed them here to keep the application context clean
  */
object Listeners {
  def initialize(context: ApplicationContext): Unit = {
    getListeners(context).foreach(context.register)
  }

  def getListeners(context: ApplicationContext): Iterable[EventListener] = {
    Seq(
      new PreKeyRefreshListener(context.account, context.protocolStore)
    )
  }
}
