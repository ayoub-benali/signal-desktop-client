package de.m7w3.signal.store

import de.m7w3.signal.store.model.{Contacts, Groups}
import org.whispersystems.signalservice.api.messages.multidevice.{DeviceContact, DeviceGroup}

case class SignalDesktopApplicationStore(dBActionRunner: DBActionRunner) {
  def saveContact(deviceContact: DeviceContact): Unit = {
    dBActionRunner.run(Contacts.insert(deviceContact))
    ()
  }

  def saveGroup(deviceGroup: DeviceGroup): Unit = {
    dBActionRunner.run(Groups.insert(deviceGroup))
    ()
  }
}
