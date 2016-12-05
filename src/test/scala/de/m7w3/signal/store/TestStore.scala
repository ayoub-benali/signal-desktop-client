package de.m7w3.signal.store

import de.m7w3.signal.resources.StoreResource
import org.scalatest.{BeforeAndAfterEach, Suite}

trait TestStore extends BeforeAndAfterEach with StoreResource with Identities with Addresses { suite: Suite =>

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    setupResource()
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    tearDownResource()
  }
}
