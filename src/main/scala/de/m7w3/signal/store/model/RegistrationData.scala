package de.m7w3.signal.store.model

import slick.jdbc.H2Profile.api._

case class Registration(userName: String, deviceId: Int, password: String, signalingKey: String)

class RegistrationData(tag: Tag) extends Table[Registration](tag, "REGISTRATION") {
  def userName = column[String]("USER_NAME")
  def deviceId = column[Int]("DEVICE_ID")
  def password = column[String]("PASSWORD") // password for basic auth to textsecure server
  def signalingKey = column[String]("SIGNALING_KEY")

  override def * = (userName, deviceId, password, signalingKey) <> (Registration.tupled, Registration.unapply)
}

object RegistrationData {
  val registrationData = TableQuery[RegistrationData]

  def insert(registration: Registration) = registrationData += registration

  def get() = registrationData.take(1).result.head
}


