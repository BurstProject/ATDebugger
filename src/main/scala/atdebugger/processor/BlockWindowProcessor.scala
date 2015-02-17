package atdebugger.processor

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props

import atdebugger.ATDebugger
import atdebugger.controller.BlockWindowController

case class SetBlockWindowController(c: BlockWindowController)
case class CreateAT()
case class RegisterAT()
case class AssignId(id: Long, height: Int)
case class CloseAT()
case class AdvanceBlock()
case class UndoBlock()
case class StartBlock(height: Int)
case class AddPendingTxs(txs: Vector[Transaction])
case class AddTx(tx: Transaction)
case class AddBalance(bal: Long)
case class FinishedBlock(id: Long)
case class UpdateTxs(blockFinished: Boolean)

class BlockWindowProcessor extends Actor {
  
  var ats = Map[Long, ActorRef]()
  var finishedATs = Set[Long]()
  var pendingTxs = Vector[Transaction]()
  val r = new scala.util.Random()
  
  var height = 1
  
  var controller: BlockWindowController = null
  
  def receive = {
    case SetBlockWindowController(c) => controller = c
    case CreateAT() => sender() ! (context.actorOf(Props[ATWindowProcessor]))
    case RegisterAT() => {
      val newAT = sender
      var id = 0L
      do {
        id = r.nextInt()
      }while(ats contains id)
      ats = ats + (id -> newAT)
      finishedATs = finishedATs + id
      sender ! AssignId(id, height)
    }
    case CloseAT() => {
      ats find (_._2 == sender) match {
        case Some(at) => {
          ats = ats - at._1
        }
        case None =>
      }
      context.stop(sender)
    }
    case AdvanceBlock() => {
      controller.disableButtons
      height += 1
      controller.setLabel("Block: " + height + " (in progress)")
      finishedATs = Set()
      pendingTxs = Vector()
      ats foreach (_._2 ! StartBlock(height))
    }
    case UndoBlock() => {
      if(height > 1) {
        height -= 1
        controller.setLabel("Block: " + height)
        ATDebugger.txProc setBlock height
        ats foreach (_._2 ! UndoBlock())
      }
    }
    case AddPendingTxs(txs) => {
      pendingTxs = pendingTxs ++ txs
    }
    case AddTx(tx) => {
      ATDebugger.txProc.addTx(Transaction(height, r.nextLong, tx.sender, tx.recipient, tx.amount, tx.message))
      ats foreach (_._2 ! UpdateTxs(false))
      ats.get(tx.recipient) match {
        case Some(at) => at ! AddBalance(tx.amount)
        case None =>
      }
    }
    case FinishedBlock(id) => {
      if(!finishedATs.contains(id)) finishedATs = finishedATs + id
      if(ats.size == finishedATs.size) {
        ATDebugger.txProc.addBlockTxs(pendingTxs)
        ats foreach (_._2 ! UpdateTxs(true))
        controller.enableButtons
        controller.setLabel("Block: " + height)
      }
    }
    case _ => System.out.println("unknown message")
  }
}