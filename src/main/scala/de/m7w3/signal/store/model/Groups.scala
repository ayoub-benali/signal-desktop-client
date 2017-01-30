package de.m7w3.signal.store.model
import org.whispersystems.signalservice.api.messages.multidevice.DeviceGroup
import slick.driver.H2Driver.api._
import scala.collection.JavaConverters.collectionAsScalaIterableConverter

class Groups(tag: Tag) extends Table[(Array[Byte], Option[String], Option[Array[Byte]], Boolean)](tag, "GROUPS") {
  def id = column[Array[Byte]]("ID", O.PrimaryKey)
  def name = column[Option[String]]("NAME")
  def avatar = column[Option[Array[Byte]]]("AVATAR")
  def active = column[Boolean]("ACTIVE", O.Default(true))

  override def *  = (id, name, avatar, active)
}

object Groups {
  val groups = TableQuery[Groups]

  def get(id: Array[Byte]) = {
    val join = groups join GroupMembers.groupMembers on (_.id === _.groupId)
    join.filter(joined => joined._1.id === id).result
  }

  def insert(deviceGroup: DeviceGroup) = {
    groups.+=((deviceGroup.getId,
      Option(deviceGroup.getName.orNull()),
      AttachmentHelpers.streamToArray(deviceGroup.getAvatar),
      deviceGroup.isActive)).andThen(
    deviceGroup.getMembers.asScala.foreach(member => {
      GroupMembers.groupMembers.+=((member, deviceGroup.getId))
    }).result)
  }
}

class GroupMembers(tag: Tag) extends Table[(String, Array[Byte])](tag, "GROUP_MEMBERS") {

  def name = column[String]("NAME")
  def groupId = column[Array[Byte]]("GROUP_ID")

  def group = foreignKey("GROUP_MEMBERS_GROUP_FK", groupId, Groups.groups)(_.id)

  override def * = (name, groupId)
}

object GroupMembers {
  val groupMembers = TableQuery[GroupMembers]
}