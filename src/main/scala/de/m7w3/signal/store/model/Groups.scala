package de.m7w3.signal.store.model
import java.util

import org.whispersystems.signalservice.api.messages.multidevice.DeviceGroup
import slick.driver.H2Driver.api._

import scala.collection.JavaConverters.collectionAsScalaIterableConverter

class Groups(tag: Tag) extends Table[(Int, Array[Byte], Option[String], Option[Array[Byte]], Boolean)](tag, "GROUPS") {
  def pk = column[Int]("pk", O.PrimaryKey, O.AutoInc)
  def id = column[Array[Byte]]("ID")
  def name = column[Option[String]]("NAME")
  def avatar = column[Option[Array[Byte]]]("AVATAR")
  def active = column[Boolean]("ACTIVE", O.Default(true))

  override def *  = (pk, id, name, avatar, active)
}

object Groups {
  val groups = TableQuery[Groups]

  def get(id: Array[Byte]) = {
    val join = groups join GroupMembers.groupMembers on (_.pk === _.groupId)
    join.filter(joined => joined._1.id === id).result
  }

  def insert(deviceGroup: DeviceGroup) = {
    val id = deviceGroup.getId
    val pk = util.Arrays.hashCode(id)
    DBIO.seq(
      groups.+=((
        pk,
        id,
        Option(deviceGroup.getName.orNull()),
        AttachmentHelpers.streamToArray(deviceGroup.getAvatar),
        deviceGroup.isActive)),
      deviceGroup.getMembers.asScala.foreach(member => {
        GroupMembers.groupMembers.+=((member, pk))
      }).result
    ).transactionally
  }
}

class GroupMembers(tag: Tag) extends Table[(String, Int)](tag, "GROUP_MEMBERS") {

  def name = column[String]("NAME")
  def groupId = column[Int]("GROUP_PK")

  def group = foreignKey("GROUP_MEMBERS_GROUP_FK", groupId, Groups.groups)(_.pk)

  override def * = (name, groupId)
}

object GroupMembers {
  val groupMembers = TableQuery[GroupMembers]
}