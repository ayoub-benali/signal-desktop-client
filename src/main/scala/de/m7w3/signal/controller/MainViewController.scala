package de.m7w3.signal.controller

import de.m7w3.signal.ApplicationContext
import scala.reflect.runtime.universe.{Type, typeOf}

import scalafx.Includes._
import scalafx.scene.Parent
import scalafxml.core.{DependenciesByType, FXMLView}
import scalafxml.core.macros.{nested, sfxml}

@sfxml
class MainViewController(@nested[EditMenuController] editMenuController: MenuController,
                          @nested[FileMenuController] fileMenuController: MenuController,
                          @nested[HelpMenuController] helpMenuController: MenuController,
                          applicationContext: ApplicationContext) {

}

object MainView {
  def load(context: ApplicationContext): Parent = {
    val dependencies = Map[Type, Any](
      typeOf[ApplicationContext] -> context
    )
    val resourceUri = "/de/m7w3/signal/main_view.fxml"
    val fxmlUri = getClass.getResource(resourceUri)
    require(fxmlUri != null, s"$resourceUri not found")
    FXMLView(fxmlUri, new DependenciesByType(dependencies))
  }
}
