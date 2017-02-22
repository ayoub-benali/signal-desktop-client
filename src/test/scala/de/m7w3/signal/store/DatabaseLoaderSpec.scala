package de.m7w3.signal.store

import java.nio.file.Files

import de.m7w3.signal.exceptions.DatabaseDoesNotExistException
import org.scalatest.{FlatSpec, Matchers}
import slick.driver.H2Driver.api._

import scala.concurrent.Await
import scala.concurrent.duration._

class DatabaseLoaderSpec extends FlatSpec with Matchers {

  val pw = "123456"

  "loadDatabase" should "load a new one if no db exist yet" in {
    val loader = DatabaseLoader("foo", dbType = DBType.MEM)
    val db = loader.loadDatabase(pw, onlyIfExists = false)
    db shouldBe 'isSuccess
    db.foreach {
      d =>
        // check if db is working
        Await.result(d.run(sqlu"""CREATE TABLE XYZ (i int primary key)"""), 2 seconds) shouldBe 0
        Await.result(d.run(sqlu"""DROP TABLE IF EXISTS XYZ"""), 2 seconds) shouldBe 0
    }

  }

  it should "return the same db if one exists already" in {
    val loader = DatabaseLoader("foo", dbType = DBType.MEM)
    val db = loader.loadDatabase(pw, onlyIfExists = false)
    db shouldBe 'isSuccess

    val newDB = loader.loadDatabase(pw, onlyIfExists = false)
    newDB shouldBe 'isSuccess
    for {
      db1 <- db
      db2 <- newDB
    } yield {
      db1 shouldBe theSameInstanceAs (db2)
    }
  }

  it should "fail if non exists yet and 'onlyIfExists' is set" in {
    val pDir = Files.createTempDirectory(this.getClass.getName)
    try {
      val dbCtx = DatabaseLoader(pDir.toString, dbType = DBType.FILE)
      val db = dbCtx.loadDatabase(pw, onlyIfExists = true)
      db shouldBe 'isFailure
      a [DatabaseDoesNotExistException] should be thrownBy db.get
    } finally {
      Files.deleteIfExists(pDir)
      ()
    }
  }
}


