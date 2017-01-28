package de.m7w3.signal.controller

import de.m7w3.signal.ApplicationContext

import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.control.Button
import scalafxml.core.macros.sfxml


@sfxml
class ChatViewController(sendMsgBtn: Button,
                         applicationContext: ApplicationContext) {

  sendMsgBtn.onAction = (a: ActionEvent) => {
    // store message
    // display message in listview
  }
}
