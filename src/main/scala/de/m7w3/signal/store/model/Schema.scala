package de.m7w3.signal.store.model

import slick.driver.H2Driver.api._

object Schema {
  val schema = Addresses.addresses.schema ++
               LocalIdentity.query.schema ++
               PreKeys.preKeys.schema ++
               PreKeys.idSequence.schema ++
               Sessions.sessions.schema ++
               SignedPreKeys.signedPreKeys.schema ++
               SignedPreKeys.idSequence.schema ++
               TrustedKeys.trustedKeys.schema ++
               RegistrationData.registrationData.schema ++
               Contacts.contacts.schema ++
               Groups.groups.schema ++
               GroupMembers.groupMembers.schema
}
