package atdebugger.processor

case class Transaction(block: Int, id: Long, sender: Long, recipient: Long, amount: Long, message: Array[Byte])

class TransactionProcessor {
  
  var transactions = Vector[Transaction]()
  
  def clear() = transactions = Vector[Transaction]()
  
  def addBlockTxs(txs: Vector[Transaction]) = transactions = transactions ++ (txs.sortWith(_.id < _.id))
  
  def addTx(tx: Transaction) = transactions = transactions.filter(_.block != tx.block) ++ ((transactions.filter(_.block == tx.block) :+ tx).sortWith(_.id < _.id))
  
  def setBlock(n: Int) = transactions = transactions filter (_.block <= n)
  
  def getTxsTo(id: Long, height: Int) = transactions filter (tx => tx.block == height && tx.recipient == id)
  
  def getTxsForId(id: Long) = transactions filter (tx => tx.sender == id || tx.recipient == id)
  
  def getTx(id: Long) = transactions find (_.id == id) match {
    case Some(tx) => tx
    case None => null
  }
  
  def getTxBlockPos(id: Long, block: Int) = (transactions filter (_.block == block) indexWhere (_.id == id)) + 1
  
  def getTxIdAfter(recipient: Long, block: Int, pos: Int) = transactions slice ((transactions indexWhere (_.block >= block)) + pos, transactions size) find (_.recipient == recipient) match {
    case Some(tx) => tx.id
    case None => 0L
  }
}