package de.m7w3.signal.messages

import de.m7w3.signal.account.AccountHelper
import monix.eval.Task

case class DeviceSynchronization(accountHelper: AccountHelper) {

  def requestSynchronization(): Task[List[Unit]] = {
    Task.gatherUnordered {
      Seq(
        Task(accountHelper.requestSyncContacts()),
        Task(accountHelper.requestSyncGroups())
      )
    }
  }
}
