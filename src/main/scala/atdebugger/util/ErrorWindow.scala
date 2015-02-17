package atdebugger.util

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.stage.Stage
import scalafx.application.Platform
import scalafx.scene.control.Label
import scalafx.scene.control.Button
import scalafx.event.ActionEvent
import scalafx.scene.layout.VBox

class ErrorWindow(msg: String) {
  val stage = new Stage {
    scene = new Scene {
      content = new VBox {
        val label = new Label(msg)
        val button = new Button("Ok")
        button.onAction = handle {this.button.scene().window().hide()}
        content = Seq(label, button)
      }
    }
  }
  stage show;
}