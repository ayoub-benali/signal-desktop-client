package de.m7w3.signal.controller

import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.scene.control.Menu
import scalafxml.core.macros.sfxml

/**
  * Created by mwahl on 11/14/16.
  */
@sfxml
class FileMenuController(private val fileMenu: Menu) extends MenuController {
  def onFileClose(event: ActionEvent): Unit = {
    Platform.exit()
  }

  override def onMenu(): Unit = {
    println("menu")
  }
}
