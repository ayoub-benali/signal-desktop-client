package de.m7w3.signal.store

import com.google.common.collect.ImmutableList
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.whispersystems.libsignal.util.guava.Optional
import org.whispersystems.signalservice.api.messages.multidevice.DeviceGroup


class ApplicationStoreSpec extends FlatSpec with Matchers with BeforeAndAfterAll with TestStore {
  behavior of "SignalDesktopApplicationStore"

  val groups = Seq(
    new DeviceGroup(Array[Byte](1, 2, 3), Optional.of("group1"),
      ImmutableList.of("member1", "member2"), Optional.absent(), true),
    new DeviceGroup(Array[Byte](1, 2, 3, 4), Optional.of("group2"),
      ImmutableList.of("member3"), Optional.absent(), true),
    new DeviceGroup(Array[Byte](1, 2, 3, 4, 5), Optional.of("group3"),
      ImmutableList.of("member3", "member1"), Optional.absent(), true),
    new DeviceGroup(Array[Byte](1, 2, 3, 4, 5, 6), Optional.of("inactive_group4"),
      ImmutableList.of("member1", "member2", "member3"), Optional.absent(), false)
  )

  it should "load all groups sorted by name with their members" in {
    groups.foreach { group =>
      applicationStore.saveGroup(group)
      ()
    }
    val groupsWithMembers = applicationStore.getGroups
    groupsWithMembers should have length 3
    groupsWithMembers.map(_.group.name) shouldBe Seq(Some("group1"), Some("group2"), Some("group3"))
    groupsWithMembers.head.members.map(_.name) should contain allOf("member1", "member2")
  }

  it should "update an existing group if inserted twice" in {
    val group1 = groups.head
    val group2 = groups.head
    applicationStore.saveGroup(group1)
    applicationStore.saveGroup(group2)

    val groupsWithMembers = applicationStore.getGroups
    groupsWithMembers should have length 1

  }
}
