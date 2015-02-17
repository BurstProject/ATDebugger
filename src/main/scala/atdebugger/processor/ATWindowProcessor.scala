package atdebugger.processor

import scala.collection.JavaConverters._
import scala.collection.immutable.HashSet
import akka.actor.Actor
import atdebugger.ATDebugger
import atdebugger.controller._
import attools._
import attools.transform._
import nxt.at.AT_API_Helper
import nxt.at.AT_Machine_State
import nxt.at.AT_Controller
import nxt.at.AT_Constants
import nxt.at.AT_Machine_Processor
import nxt.at.AT_Exception

case class SetController(controller: ATWindowController)
case class SetATCode(code: Vector[String])
case class SetATBytes(bytes: String)
case class ToggleBP(location: Long)
case class SetIP(ip: Long)
case class SetVar(name: String, value: Long)
case class RunAT()
case class StepInto()
case class StepOver()
case class SetBalance(bal: Long)

class ATWindowProcessor extends Actor {
  
  var controller: ATWindowController = null
  var bps = HashSet[Long]()
  var id = 0L
  var balance = 0L
  var creationHeight = 0
  var currentHeight = 0
  
  var curState: Option[ATState] = None
  var inProgState: Option[AT_Machine_State] = None
  var machineProc: Option[AT_Machine_Processor] = None
  var pastBlockStates = List[ATState]()
  var pastBalances = List[Long]()
  
  val r = new scala.util.Random
  
  def receive = {
    case SetController(cont) => {controller = cont; ATDebugger.blockProcessor ! RegisterAT()}
    case AssignId(id, height) => {
      this.id = id
      controller.setId(id)
      this.creationHeight = height
    }
    case SetATCode(code) => setCode(code)
    case ToggleBP(loc) => {
      if(bps contains loc) {
        bps = bps - loc
        controller.setBP(loc, false)
      }
      else {
        bps = bps + loc
        controller.setBP(loc, true)
      }
    }
    case SetIP(ip) => inProgState match {
      case Some(s) => {
        s.getMachineState().pc = ip.toInt
        controller.setIP(ip.toInt)
      }
      case None =>
    }
    case SetVar(name, value) => inProgState match {
      case Some(s) => curState.get.varMap.get(name) match {
        case Some(pos) => s.getAp_data().putLong(pos * 8, value)
        case None =>
      }
      case None =>
    }
    case StartBlock(height: Int) => {
      startBlock(height)
      runCode(false)
    }
    case UpdateTxs(finishedBlock) => {
      controller.setTxs(ATDebugger.txProc.getTxsForId(id) reverse)
      if(finishedBlock) {
        balance = balance + ATDebugger.txProc.getTxsTo(id, currentHeight).foldLeft(0L)(_ + _.amount)
        controller updateBalance(balance)
      }
    }
    case AddBalance(bal) => {
      balance += bal
      controller updateBalance balance
    }
    case UndoBlock() => {
      if(pastBlockStates.size > 0 && pastBalances.size > 0) {
        currentHeight -= 1
        curState = Some(pastBlockStates head)
        pastBlockStates = pastBlockStates tail;
        balance = pastBalances head;
        pastBalances = pastBalances tail;
        loadFromState(curState.get)
        updateDisplay
        inProgState = None
        machineProc = None
      }
    }
    case RunAT() => runCode(true)
    case StepInto() => stepInto()
    case StepOver() => stepOver()
    case SetBalance(bal) => {
      balance = bal
      System.out.println("Set balance: " + balance)
    }
    case CloseAT() => ATDebugger.blockProcessor ! CloseAT()
    case _ =>
  }
  
  def setCode(code: Vector[String]) = {
    try {
      val parser = new OpParser
      val loadedOps = parser parseLines code

      val allocTrans = loadedOps map (transform.AllocateTransform(_))
      val (opPos, labelPos) = transform.LongBranchTranform.findPos(allocTrans flatten)
      val allocTransWithInd = allocTrans.zipWithIndex flatMap { case(v, i) => v map {(i, _)}}
      val lbTransWithInd = allocTransWithInd zip opPos flatMap { case((i, o), p) =>
        transform.LongBranchTranform(o, p, labelPos) map {(i, _)}
      }
      var outPos = 0
      val labelMap = transform.ByteCodeTransform.mapLabels(lbTransWithInd map { case(i, o) => o})
      val varMap = transform.ByteCodeTransform.mapVars(lbTransWithInd map { case(i, o) => o})
      val bytesWithIndPos = lbTransWithInd map { case(i, o) =>
        val bytes = transform.ByteCodeTransform(o, outPos, labelMap, varMap)
        val out = (i, outPos, bytes)
        outPos += bytes.size
        out
      }
      val bytesWithIndPosCondensed = (bytesWithIndPos.groupBy(_._1) mapValues { v =>
        val p = v.minBy(_._2)._2 
        val b = v flatMap{case(_, _, b) => b}
        (p, b)
      }).toVector.sortWith(_._1 < _._1)
      val bytes = bytesWithIndPosCondensed flatMap { case(_, (_, b)) => b}
      val viewPos = bytesWithIndPosCondensed map { case (_, (p, _)) => p}
      bytesWithIndPosCondensed foreach { case(i, (pos, bytes)) =>
        System.out.println(i + " " + pos + " " + bytes)
      }
      
      System.out.println(Conversion.toHexString(bytes.toArray))
      
      val creationBytes = Array[Byte](1, 0, 0, 0, 10, 0, 10, 0, 10, 0, 10, 0, 1, 0, 0, 0, 0, 0, 0, 0, (bytes.length & 0xFF).toByte, ((bytes.length >>> 8) & 0xFF).toByte) ++ bytes ++ Array[Byte](0, 0)
      
      try {
        AT_Controller.checkCreationBytes(creationBytes, 1)
        System.out.println("passed")
      }
      catch {
        case e: AT_Exception => System.out.println(e.getMessage())
      }
      
      val mState = new AT_Machine_State(AT_API_Helper.getByteArray(id), AT_API_Helper.getByteArray(12345), creationBytes,creationHeight)
      AT_Controller.resetMachine(mState)
      curState = Some(new ATState(loadedOps, bytes.toArray, viewPos, varMap, mState.getState(), 0, 0, mState.getG_balance(), mState.getP_balance(), mState.freezeOnSameBalance()))
      
      controller.setCode(for((op, p) <- loadedOps zip viewPos) yield (p, op.toString()))
      controller.setVars((for((n, i) <- varMap) yield (i, n, 0L)).toVector)
      controller.setIP(0)
    }
    catch {
      case e: DuplicateLabelException => controller.showAlert("Duplicate label: " + e.label)
      case e: UnknownLineException => controller.showAlert("Unknown line: " + e.line)
      case e: LabelNotFoundException => controller.showAlert("Unknown label: " + e.label)
      case e: VariableNotFoundException => controller.showAlert("Assembler error: no mapping for var: " + e.variable )
      case e: NumberFormatException => controller.showAlert(e.getMessage())
      case e: Exception => e.printStackTrace()
    }
  }
  
  def startBlock(height: Int) = {
    currentHeight = height
    curState match {
      case Some(s) => {
        pastBlockStates = s +: pastBlockStates
        pastBalances = balance +: pastBalances
      }
      case None =>
    }
    curState match {
      case Some(s) if height < s.nextHeight => endBlock
      case Some(s) if balance <= s.balance && s.freeze => endBlock
      case Some(s) if balance < 100000000 => endBlock
      case Some(s) => {
        loadFromState(s)
        controller.enableButtons
      }
      case None => endBlock
    }
  }
  
  def loadFromState(s: ATState) = {
    val mState = new AT_Machine_State(AT_API_Helper.getByteArray(id), AT_API_Helper.getByteArray(12345), 1.toShort,
      s.atState.clone, s.byteCode.size, 2560, 2560, 2560, creationHeight, 0,
      s.freeze, 1, s.byteCode)
    mState.clearTransactions
    mState.setHeight(currentHeight)
    mState.setWaitForNumberOfBlocks(0)
    mState.setG_balance(balance)
    AT_Controller.listCode(mState, true, true)
    mState.getMachineState().running = true
    mState.getMachineState().stopped = false
    mState.getMachineState().finished = false
    mState.getMachineState().dead = false
    mState.getMachineState().steps = 0
    mState.setFreeze(false)
    machineProc = Some(new AT_Machine_Processor(mState))
    inProgState = Some(mState)
  }
  
  def canRun(s: AT_Machine_State) = {
    if(s.getMachineState().isDead() || s.getMachineState().isStopped() || s.getMachineState().isFinished()) {
      false
    }
    else {
      val steps = AT_Controller.getNumSteps(s.getAp_code().get(s.getMachineState().pc), s.getHeight())
      if(s.getMachineState().steps + steps > AT_Constants.getInstance().MAX_STEPS( s.getHeight() )) {
        false
      }
      else if(s.getG_balance() < (10000000L * steps)) {
        false
      }
      else {
        true
      }
    }
  }
  
  def doStep(s: AT_Machine_State) = {
    val steps = AT_Controller.getNumSteps(s.getAp_code().get(s.getMachineState().pc), s.getHeight())
    val fee = 10000000L * steps
    s.getMachineState().steps += steps
    s.setG_balance(s.getG_balance() - fee)
    val rc = machineProc.get.processOp(false,false)
    if(rc < 0) {
      if(s.getMachineState().jumps.contains(s.getMachineState().err)) {
        s.getMachineState().pc = s.getMachineState().err
      }
      else {
        s.getMachineState().dead = true
      }
    }
  }
  
  def runCode(user: Boolean) = inProgState match {
    case Some(s) => {
      if(canRun(s) && (user || !(bps contains s.getMachineState().pc))) {
        do {
          doStep(s)
        } while(canRun(s) && !(bps.contains(s.getMachineState().pc)));
        updateDisplay
        if(!canRun(s)) {
          endBlock
        }
      }
    }
    case None => endBlock
  }
  
  def stepInto() = inProgState match {
    case Some(s) => {
      if(canRun(s)) {
        doStep(s)
      }
      updateDisplay
      if(!canRun(s)) {
        endBlock
      }
    }
    case None => endBlock
  }
  
  def stepOver() = inProgState match {
    case Some(s) => {
      if(canRun(s)) {
        do {
          doStep(s)
        } while(!(curState.get.viewPos.contains(s.getMachineState().pc)) && canRun(s));
        updateDisplay
        if(!canRun(s)) {
          endBlock
        }
      }
    }
    case None => endBlock
  }
  
  def updateDisplay() = (curState, inProgState) match {
    case (Some(as), Some(ms)) => {
      controller.setIP(ms.getMachineState().pc)
      balance = ms.getG_balance()
      controller.updateBalance(balance)
      val varUpdate = as.varMap map { case(name, pos) =>
        (name, ms.getAp_data().getLong(pos * 8))
      }
      controller.updateVars(varUpdate)
    }
    case _ =>
  }
  
  def endBlock {
    (curState, inProgState) match {
      case (Some(s), Some(ips)) => {
        balance = ips.getG_balance()
        curState = Some(new ATState(s.loadedOps, s.byteCode, s.viewPos, s.varMap, ips.getState(), ips.getMachineState().pc, currentHeight + ips.getWaitForNumberOfBlocks(), ips.getG_balance(), ips.getP_balance(), ips.freezeOnSameBalance()))
        val txs = ips.getTransactions().asScala map { t => Transaction(currentHeight, r.nextLong, AT_API_Helper.getLong(t.getSenderId()), AT_API_Helper.getLong(t.getRecipientId()), t.getAmount(), t.getMessage())} toVector;
        ATDebugger.blockProcessor ! AddPendingTxs(txs)
        inProgState = None
      }
      case _ =>
    }
    machineProc = None
    controller.disableButtons
    ATDebugger.blockProcessor ! FinishedBlock(id)
  }
}

class ATState(val loadedOps: Vector[OpCode],
    val byteCode: Array[Byte],
    val viewPos: Vector[Int],
    val varMap: Map[String, Int],
    val atState: Array[Byte],
    val pc: Int,
    val nextHeight: Int,
    val balance: Long,
    val pBalance: Long,
    val freeze: Boolean) {
}