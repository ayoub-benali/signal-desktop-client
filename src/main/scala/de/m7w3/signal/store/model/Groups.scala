package de.m7w3.signal.store.model
import java.util

import de.m7w3.signal.store.DBActionRunner
import org.whispersystems.signalservice.api.messages.multidevice.DeviceGroup
import slick.driver.H2Driver.api._

import scala.collection.JavaConverters.collectionAsScalaIterableConverter

case class Group(pk: Int,
                 id: Array[Byte],
                 name: Option[String],
                 avatar: Option[Array[Byte]],
                 active: Boolean) {
  def members = {
    GroupMembers.groupMembers.filter(_.groupId === pk).result
  }
}

case class GroupWithMembers(group: Group,
                            members: Seq[GroupMember])

class Groups(tag: Tag) extends Table[Group](tag, "GROUPS") {
  def pk = column[Int]("pk", O.PrimaryKey)
  def id = column[Array[Byte]]("ID")
  def name = column[Option[String]]("NAME")
  def avatar = column[Option[Array[Byte]]]("AVATAR")
  def active = column[Boolean]("ACTIVE", O.Default(true))

  override def *  = (pk, id, name, avatar, active) <> (Group.tupled, Group.unapply)
}

object Groups {
  val groups = TableQuery[Groups]

  def get(id: Array[Byte]) = {
    val join = groups.filter(_.id === id) join GroupMembers.groupMembers on (_.pk === _.groupId)
    join.result
  }

  def activeGroupsByName(dbActionRunner: DBActionRunner): Seq[GroupWithMembers] = {
    val join = groups.filter(_.active === true).sortBy(_.name.asc) join GroupMembers.groupMembers on (_.pk === _.groupId)
    val groupsAndMembers = dbActionRunner.run(join.result)
    val grouped = groupsAndMembers.groupBy {
      case (group, member) => group.pk
    }
    grouped.map { case (group, groupsMembers) =>
      GroupWithMembers(groupsMembers.head._1, groupsMembers.map(_._2))
    }.toSeq.sortBy(_.group.name)
  }

  def insert(deviceGroup: DeviceGroup) = {
    val group = fromDeviceGroup(deviceGroup)
    val members = deviceGroup.getMembers.asScala
    groups.insertOrUpdate(group).andThen(
      DBIO.sequence(members.map(member => GroupMembers.groupMembers.insertOrUpdate(GroupMember(member, group.pk))))
    ).transactionally
  }

  def fromDeviceGroup(deviceGroup: DeviceGroup): Group = {
    val id = deviceGroup.getId
    Group(
      util.Arrays.hashCode(id),
      id,
      Option(deviceGroup.getName.orNull()),
      AttachmentHelpers.streamToArray(deviceGroup.getAvatar),
      deviceGroup.isActive
    )
  }
}

case class GroupMember(name: String, groupId: Int)

class GroupMembers(tag: Tag) extends Table[GroupMember](tag, "GROUP_MEMBERS") {

  def name = column[String]("NAME")
  def groupId = column[Int]("GROUP_PK")

  def pk = primaryKey("PK_GM", (name, groupId))
  def group = foreignKey("GROUP_MEMBERS_GROUP_FK", groupId, Groups.groups)(_.pk)

  override def * = (name, groupId) <> (GroupMember.tupled, GroupMember.unapply)
}

object GroupMembers {
  val groupMembers = TableQuery[GroupMembers]
}
