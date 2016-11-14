package de.m7w3.signal.controller

import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.control.{Button, ButtonBar, Menu}
import scalafx.scene.layout.AnchorPane
import scalafxml.core.macros.{nested, sfxml}

@sfxml
class ChatsListController(
    val chatsListButtonBar: ButtonBar,
    val newChatBtn: Button,
    @nested[EditMenuController] editMenuController: MenuController,
    @nested[FileMenuController] fileMenuController: MenuController,
    @nested[HelpMenuController] helpMenuController: MenuController) {

  println(s"editMenuController initialized $editMenuController")

  def onNewChat(event: ActionEvent): Unit = {
    println("new chat pressed")
    // show contacts search list

    val btn = new Button("switch")
    btn.id = "switch"
    val scene = newChatBtn.getScene
    val storeRoot = scene.getRoot
    btn.onAction = (a: ActionEvent) => {
      scene.setRoot(storeRoot)
    }
    val pane = new AnchorPane()
    pane.children.add(btn)
    scene.setRoot(pane)
  }


}
