package de.m7w3.signal.store

import java.nio.file.{Path, Paths}

import de.m7w3.signal.exceptions.DatabaseDoesNotExistException
import org.h2.api.ErrorCode
import org.h2.jdbc.JdbcSQLException
import slick.driver.H2Driver.api._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Try}

object DBType {
  val FILE = "file"
  val MEM = "mem"
}

case class DatabaseLoader(profileDir: String,
                          dbType: String = DBType.FILE) {

  val DB_NAME = "signal-desktop"
  val DB_USER = "signal-desktop"

  val CONNECTION_PROPERTIES: Map[String, String] = Map(
    "CIPHER" -> "AES"
  )

  val LOAD_CONNECTION_PROPERTIES: Map[String, String] = CONNECTION_PROPERTIES + ("IFEXISTS" -> "TRUE")

  /**
    * path of the database to be used in the JDBC URL
    */
  def uriDbPath: Path = Paths.get(profileDir, DB_NAME)

  /**
    * actual path of the database
    */
  def dbFilePath: Path = Paths.get(uriDbPath.toString + ".mv.db")

  private def connectionProperties(onlyIfExists: Boolean): String = {
    val props = if (onlyIfExists) LOAD_CONNECTION_PROPERTIES else CONNECTION_PROPERTIES
    props.map { case (k, v) => s"$k=$v"} mkString ";"
  }

  private def dbUrl(onlyIfExists: Boolean): String = s"jdbc:h2:$dbType:${uriDbPath.toString};${connectionProperties(onlyIfExists)}"

  def loadDatabase(password: String, onlyIfExists: Boolean = false): Try[Database] = {
    val passwords: String = s"$password $password" // #YOLO
    val loaded = Try {
      val _db = Database.forURL(
          url = dbUrl(onlyIfExists),
          user = DB_USER,
          password = passwords,
          driver = "org.h2.Driver"
        )
      // test if database is actually available
      // in order to fail fast in case `onlyIfExists` was set
      Await.result(_db.run(sql"""select table_name from information_schema.tables""".as[(String)]), 10 seconds)
      _db
    }.recoverWith {
      case e:JdbcSQLException =>
        if (e.getErrorCode == ErrorCode.DATABASE_NOT_FOUND_1) {
          Failure(new DatabaseDoesNotExistException(e.getMessage, e.getOriginalCause))
        } else {
          Failure(e)
        }
    }
    loaded
  }
}
