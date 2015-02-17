package atdebugger.controller

import scalafx.Includes._
import scalafxml.core.macros.sfxml
import scalafxml.core._
import scalafx.scene.control.TextField
import scalafx.event.ActionEvent
import atdebugger.ATDebugger
import atdebugger.processor.AddTx
import atdebugger.processor.Transaction
import attools.Conversion

trait TransactionWindowInterface {
  def setRecipient(recip: Long)
}

@sfxml
class TransactionWindowController(private val txSender: TextField,
    private val txRecipient: TextField,
    private val txAmount: TextField,
    private val txMessage: TextField)
    extends TransactionWindowInterface {
  
  def sendTx(event: ActionEvent) = {
    try {
      var message = Conversion.parseHexString(txMessage.text())
      if(message != null && message.size == 0) message = null
      ATDebugger.blockProcessor ! AddTx(Transaction(0, 0, java.lang.Long.parseLong(txSender.text()),
          java.lang.Long.parseLong(txRecipient.text()),
          java.lang.Long.parseLong(txAmount.text()),
          message))
      
      txAmount.text() = ""
      txMessage.text() = ""
    }
    catch {
      case e: Exception =>
    }
  }
  
  def setRecipient(recip: Long) = {
    txRecipient.text() = recip.toString
  }
}