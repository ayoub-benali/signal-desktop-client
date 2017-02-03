package de.m7w3.signal.store

import de.m7w3.signal.store.model.{Contacts, Schema}
import org.scalatest.{FlatSpec, Matchers}
import slick.driver.H2Driver.api._

import scala.concurrent.duration._

class SchemaSpec extends FlatSpec with Matchers {

  "Schema" should "load fine to a fresh database" in {
    val loader = DatabaseLoader(getClass.getName, dbType = DBType.MEM)
    val loaded = loader.loadDatabase("bar")
    loaded shouldBe 'isSuccess
    loaded.foreach(
      db => {
        val runner = DBActionRunner(db, 10 seconds)
        runner.run(
          Schema.schema.create.andThen(
            Contacts.contacts.result
          )
        ) shouldBe empty
        db.close()
      }
    )
  }
}
