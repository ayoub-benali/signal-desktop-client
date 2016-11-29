package de.m7w3.signal.controller

import de.m7w3.signal.ApplicationContext

import scala.reflect.runtime.universe.{Type, typeOf}
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.Parent
import scalafx.scene.control.{Button, ButtonBar}
import scalafx.scene.layout.AnchorPane
import scalafxml.core.macros.{nested, sfxml}
import scalafx.scene.Parent
import de.m7w3.signal.{ApplicationContext, InitiatedContext}
import scala.reflect.runtime.universe.{Type, typeOf}
import scalafxml.core.{DependenciesByType, FXMLView}


@sfxml
class ChatsListController(
    val chatsListButtonBar: ButtonBar,
    val newChatBtn: Button,
    @nested[EditMenuController] editMenuController: MenuController,
    @nested[FileMenuController] fileMenuController: MenuController,
    @nested[HelpMenuController] helpMenuController: MenuController,
    applicationContext: ApplicationContext) {

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

object ChatsList{
  def load(context: InitiatedContext): Parent = {
    val dependencies = Map[Type, Any](
      typeOf[ApplicationContext] -> context
    )
    val resourceUri = "/de/m7w3/signal/recent_chats_list.fxml"
    val fxmlUri = getClass.getResource(resourceUri)
    require(fxmlUri != null, s"$resourceUri not found")
    FXMLView(fxmlUri, new DependenciesByType(dependencies))
  }
}
