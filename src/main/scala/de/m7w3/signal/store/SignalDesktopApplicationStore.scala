package de.m7w3.signal.store

import de.m7w3.signal.store.model.{Contacts, GroupWithMembers, Groups}
import org.whispersystems.signalservice.api.messages.multidevice.{DeviceContact, DeviceGroup}


case class SignalDesktopApplicationStore(dBActionRunner: DBActionRunner) {
  def getContacts: Seq[DeviceContact] = {
    dBActionRunner.run(Contacts.contactsByName)
  }

  def saveContact(deviceContact: DeviceContact): Unit = {
    dBActionRunner.run(Contacts.insert(deviceContact))
    ()
  }

  def saveGroup(deviceGroup: DeviceGroup): Unit = {
    dBActionRunner.run(Groups.insert(deviceGroup))
    ()
  }

  def getGroups: Seq[GroupWithMembers] = {
    Groups.activeGroupsByName(dBActionRunner)
  }
}
