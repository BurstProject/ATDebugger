package atdebugger

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.stage.Stage
import scalafx.application.Platform

import scalafxml.core.macros.sfxml
import scalafxml.core._

import akka.actor.ActorSystem
import akka.actor.Props

import atdebugger.controller._
import atdebugger.processor._

object ATDebugger extends JFXApp {
  
  val system = ActorSystem()
  val txProc = new TransactionProcessor
  val blockProcessor = system.actorOf(Props[BlockWindowProcessor])
  
  stage = new JFXApp.PrimaryStage {
    scene = new Scene(FXMLView(getClass.getResource("blockwindow.fxml"), new DependenciesByType(Map())))
  }

}