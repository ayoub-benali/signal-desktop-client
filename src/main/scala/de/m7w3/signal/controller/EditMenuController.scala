package de.m7w3.signal.controller

import scalafx.event.ActionEvent
import scalafx.scene.control.MenuItem
import scalafxml.core.macros.sfxml

/**
  * Created by mwahl on 11/14/16.
  */
@sfxml
class EditMenuController(editMenuItemSettings: MenuItem) extends MenuController {

  def onEditSettings(event: ActionEvent): Unit = {
    println("edit->settings")
    println(s"${editMenuItemSettings.id}")
  }

  override def onMenu(): Unit = {
    println("menu")
  }
}
