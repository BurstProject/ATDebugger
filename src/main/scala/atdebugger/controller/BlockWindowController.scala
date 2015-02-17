package atdebugger.controller

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.stage.Stage
import scalafx.application.Platform
import scalafx.scene.control._
import scalafx.beans.property._
import scalafx.event._
import scalafxml.core.macros.sfxml
import scalafxml.core._
import atdebugger.ATDebugger
import atdebugger.processor._

@sfxml
class BlockWindowController(private val addAT: Button,
    private val advanceBlock: Button,
    private val undoBlock: Button,
    private val blockLabel: Label) {
  
  def addAT(event: ActionEvent) = {
    val newATWindow = new Stage {
      scene = new Scene(FXMLView(getClass.getResource("atwindow.fxml"), new DependenciesByType(Map())))
    }
    newATWindow show;
  }
  
  def advanceBlock(event: ActionEvent) = {
    ATDebugger.blockProcessor ! AdvanceBlock()
  }
  
  def undoBlock(event: ActionEvent) = {
    ATDebugger.blockProcessor ! UndoBlock()
  }
  
  def setLabel(msg: String) {
    Platform.runLater {
      blockLabel.text() = msg
    }
  }
  
  def disableButtons = {
    Platform.runLater {
      addAT.disable() = true
      advanceBlock.disable() = true
      undoBlock.disable() = true
    }
  }
  
  def enableButtons = {
    Platform.runLater {
      addAT.disable() = false
      advanceBlock.disable() = false
      undoBlock.disable() = false
    }
  }
  
  def setWindChange: Unit = {
    blockLabel.scene().window.onChange {
      setCloseFunc
    }
  }
  
  ATDebugger.blockProcessor ! SetBlockWindowController(BlockWindowController.this)
  
  blockLabel.scene.onChange {
    setWindChange
  }
  
  def setCloseFunc = {
    blockLabel.scene().window().onHidden = handle {onClose}
  }
  
  def onClose = {
    ATDebugger.system.shutdown
    System.exit(0)
  }
  
}