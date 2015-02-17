package atdebugger.controller

import scala.collection.JavaConverters._
import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.stage.Stage
import scalafx.application.Platform
import scalafx.scene.control._
import scalafx.beans.property._
import scalafx.event._
import scalafx.scene.input._
import scalafx.scene.paint._
import scalafx.scene.shape._
import scalafxml.core.macros.sfxml
import scalafxml.core._
import atdebugger.ATDebugger
import atdebugger.processor._
import akka.actor.ActorRef
import akka.pattern.ask
import ATDebugger.system.dispatcher
import scala.util._
import scala.concurrent.duration._
import attools.Conversion
import scalafx.scene.control.cell.TextFieldTableCell
import scalafx.util.converter.LongStringConverter
import scalafx.util.StringConverter
import scalafx.util.converter.NumberStringConverter
import scalafx.scene.control.TableColumn.CellEditEvent
import atdebugger.util.ErrorWindow

class CodeLine(bp_ : Boolean,
    ip_ : Boolean,
    location_ : String,
    command_ : String) {
  val bp = new ObjectProperty(this, "bp", bp_ )
  val ip = new ObjectProperty(this, "ip", ip_ )
  val location = new StringProperty(this, "location", location_ )
  val command = new StringProperty(this, "command", command_ )
}

class VarLine(varLocation_ : Int,
    varName_ : String,
    varValue_ : Long) {
  val varLocation = new IntegerProperty(this, "varLocation", varLocation_ )
  val varName = new StringProperty(this, "varName", varName_ )
  val varValue = new LongProperty(this, "varValue", varValue_ )
}

class TxLine(txHeight_ : Int,
    txId_ : Long,
    txSender_ : Long,
    txRecipient_ : Long,
    txAmount_ : Long,
    txMessage_ : String) {
  val txHeight = new IntegerProperty(this, "txHeight", txHeight_ )
  val txId = new LongProperty(this, "txId", txId_ )
  val txSender = new LongProperty(this, "txSender", txSender_ )
  val txRecipient = new LongProperty(this, "txRecipient", txRecipient_ )
  val txAmount = new LongProperty(this, "txAmount", txAmount_ )
  val txMessage = new StringProperty(this, "txMessage", txMessage_ )
}

@sfxml
class ATWindowController(
    private val atCode: TableView[CodeLine],
    private val bp: TableColumn[CodeLine, Boolean],
    private val ip: TableColumn[CodeLine, Boolean],
    private val codeLocation: TableColumn[CodeLine, String],
    private val codeCommand: TableColumn[CodeLine, String],
    private val atVars: TableView[VarLine],
    private val varLocation: TableColumn[VarLine, Number],
    private val varName: TableColumn[VarLine, String],
    private val varValue: TableColumn[VarLine, Number],
    private val atTxs: TableView[TxLine],
    private val txHeight: TableColumn[TxLine, Number],
    private val txId: TableColumn[TxLine, Number],
    private val txSender: TableColumn[TxLine, Number],
    private val txRecipient: TableColumn[TxLine, Number],
    private val txAmount: TableColumn[TxLine, Number],
    private val txMessage: TableColumn[TxLine, String],
    private val runAT: Button,
    private val stepInto: Button,
    private val stepOver: Button,
    private val atBalance: TextField,
    private val sendTx: Button) {
  
  bp.cellFactory = { _ =>
    new TableCell[CodeLine, Boolean] {
      item.onChange { (_, _, newVal) =>
        graphic = if(newVal == true){new Circle {fill = Color.Red; radius = 8}} else {null}
      }
      onMouseClicked = handle {
        try {
          val loc = java.lang.Long.parseLong(atCode.items().get(index()).location(), 16)
          atProcessor ! ToggleBP(loc)
        }
        catch {
          case _: Exception =>
        }
      }
    }
  }
  ip.cellFactory = { _ =>
    new TableCell[CodeLine, Boolean] {
      item.onChange { (_, _, newVal) =>
        graphic = if(newVal == true){new Circle {fill = Color.Yellow; radius = 8}} else {null}
      }
      onMouseClicked = { e: MouseEvent =>
        try {
          if(e.clickCount == 2) {
            val newIp = java.lang.Long.parseLong(atCode.items().get(index()).location(), 16)
            atProcessor ! SetIP(newIp)
          }
        }
        catch {
          case _: Exception =>
        }
      }
    }
  }
  bp.cellValueFactory = { _.value.bp }
  ip.cellValueFactory = { _.value.ip }
  codeLocation.cellValueFactory = { _.value.location }
  codeCommand.cellValueFactory = { _.value.command }
  
  varValue.cellFactory =  _ => new TextFieldTableCell[VarLine, Number](new NumberStringConverter(): StringConverter[Number])
  varValue.onEditCommit = { e: CellEditEvent[VarLine, Number] =>
    atProcessor ! SetVar(e.rowValue.varName(), e.newValue.asInstanceOf[Long])
  }
  varLocation.cellValueFactory = { _.value.varLocation.delegate  }
  varName.cellValueFactory = { _.value.varName }
  varValue.cellValueFactory = { _.value.varValue.delegate }
  
  txHeight.cellValueFactory = { _.value.txHeight.delegate }
  txId.cellValueFactory = { _.value.txId.delegate }
  txSender.cellValueFactory = { _.value.txSender.delegate }
  txRecipient.cellValueFactory = { _.value.txRecipient.delegate}
  txAmount.cellValueFactory = { _.value.txAmount.delegate }
  txMessage.cellValueFactory = { _.value.txMessage }
  
  //atCode.items.get().add(new CodeLine(true, true, "1", "b"))
  //atVars.items().add(new VarLine(0.toString, "asdf", 1.toString))
  
  def runAT(event: ActionEvent) = {
    atProcessor ! RunAT()
  }
  
  def stepInto(event: ActionEvent) = {
    atProcessor ! StepInto()
  }
  
  def stepOver(event: ActionEvent) = {
    atProcessor ! StepOver()
  }
  
  def setBalance(event: ActionEvent) = {
    atProcessor ! SetBalance(atBalance.text().toLong)
  }
  
  def sendTx(event: ActionEvent) = {
    val fxml = getClass.getResource("sendWindow.fxml")
    val loader = new FXMLLoader(fxml, new DependenciesByType(Map()))
    loader.load()
    val root = loader.getRoot[javafx.scene.Parent]()
    val controller = loader.getController[TransactionWindowInterface]
    val stage = new Stage {
      scene = new Scene(root)
    }
    controller.setRecipient(id)
    stage show;
  }
  
  def dragOver(event: DragEvent) = {
    if(event.dragboard hasFiles) {
      event acceptTransferModes TransferMode.COPY
    }
  }
  
  def dragDropped(event: DragEvent) = {
    if(event.dragboard.hasFiles) {
      val in = io.Source.fromFile(event.dragboard.Files head).getLines toVector;
      var hex = "([\\da-f]+)".r
      try {
        in.toSeq head match {
          case hex(bytes) => atProcessor ! SetATBytes(bytes)
          case _ => atProcessor ! SetATCode(in)
        }
      }
      catch {
        case _: Throwable =>
      }
      
    }
    
  }
  
  def setBP(loc: Long, enabled: Boolean) = {
    val locText = loc.toHexString
    Platform.runLater {
      atCode.items() foreach { l =>
        if(l.location() == locText) {
          l.bp() = enabled
        }
      }
    }
  }
  
  def setIP(loc: Int) = {
    val locText = Integer.toHexString(loc)
    Platform.runLater {
      atCode.items() foreach { l =>
        if(l.location() == locText) {
          l.ip() = true
        }
        else {
          l.ip() = false
        }
      }
    }
  }
  
  def setCode(code: Vector[(Int, String)]) = {
    Platform.runLater {
      atCode.items().clear()
      val lines = for((l, c) <- code) yield new CodeLine(false, false, Integer.toHexString(l), c)
      atCode.items().addAll(lines.asJava)
    }
  }
  
  def setVars(vars: Vector[(Int, String, Long)]) = {
    Platform.runLater {
      atVars.items().clear()
      val lines = for((l, n, v) <- vars) yield new VarLine(l, n, v)
      atVars.items().addAll(lines.asJava)
    }
  }
  
  def updateVars(vars: Map[String, Long]) = {
    Platform.runLater {
      atVars.items() foreach { l =>
        vars get l.varName() match {
          case Some(v) => l.varValue() = v
          case None =>
        }
      }
    }
  }
  
  def updateBalance(newBal: Long) {
    Platform.runLater {
      atBalance.text() = newBal.toString
    }
  }
  
  def disableButtons() {
    Platform.runLater {
      runAT.disable() = true
      stepInto.disable() = true
      stepOver.disable() = true
      sendTx.disable() = false
    }
  }
  
  def enableButtons() {
    Platform.runLater {
      runAT.disable() = false
      stepInto.disable() = false
      stepOver.disable() = false
      sendTx.disable() = true
    }
  }
  
  def setTxs(txs: Vector[Transaction]) {
    Platform.runLater {
      atTxs.items().clear()
      atTxs.items().addAll((txs map {t => new TxLine(t.block, t.id, t.sender, t.recipient, t.amount, if(t.message != null){Conversion.toHexString(t.message)}else{null})}).asJava)
    }
  }
  
  def addTx(tx: Transaction) {
    Platform.runLater {
      atTxs.items().add(0, new TxLine(tx.block, tx.id, tx.sender, tx.recipient, tx.amount, if(tx.message != null){Conversion.toHexString(tx.message)}else{null}))
    }
  }
  
  def showAlert(msg: String) = {
    Platform.runLater {
      new ErrorWindow(msg)
    }
  }
  
  var atProcessor: ActorRef = null
  var id = 0L
  disableButtons
  
  def setId(newId: Long) = {
    id = newId
  }
  
  val atFuture = ATDebugger.blockProcessor.ask(CreateAT())(5 seconds)
  atFuture onComplete {
    case Success(newAt: ActorRef) => {
      atProcessor = newAt
      atProcessor ! SetController(ATWindowController.this)
    }
    case _ => System.out.println("Failed")
  }
  
  atCode.scene.onChange{
    setCloseFunc
  }
  
  def setCloseFunc = {
    atCode.scene().window().onHidden = handle {onClose}
    atCode.scene().onKeyPressed() = { e: KeyEvent =>
      e.code match {
        case _ if runAT.disable() =>
        case KeyCode.F7 => atProcessor ! StepInto()
        case KeyCode.F8 => atProcessor ! StepOver()
        case KeyCode.F9 => atProcessor ! RunAT()
        case _ =>
      }
    }
  }
  
  def onClose = {
    atProcessor ! CloseAT()
    System.out.println("closing")
  }
  
}