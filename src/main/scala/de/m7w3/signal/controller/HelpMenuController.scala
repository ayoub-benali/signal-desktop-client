package de.m7w3.signal.controller

import scalafx.event.ActionEvent
import scalafxml.core.macros.sfxml

@sfxml
class HelpMenuController extends MenuController {
  def onHelpOnline(event: ActionEvent): Unit = {
    println("go to https://github.com/abenali/signal-desktop-client/wiki")
  }

  def onHelpAbout(event: ActionEvent): Unit = {
    println("help->about")
  }

  override def onMenu(): Unit = println("helpmenu")
}
