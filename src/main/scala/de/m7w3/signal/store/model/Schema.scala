package de.m7w3.signal.store.model

import slick.driver.H2Driver.api._

object Schema {
  val schema = Addresses.addresses.schema ++
               LocalIdentity.query.schema ++
               PreKeys.preKeys.schema ++
               Sessions.sessions.schema ++
               SignedPreKeys.signedPreKeys.schema ++
               TrustedKeys.trustedKeys.schema
}
