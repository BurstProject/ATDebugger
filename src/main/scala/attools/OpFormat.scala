package attools

class OpFormatContainer(private val ops: Vector[OpFormat]) {
  
  def this() = this(Vector())
  
  def +(f: OpFormat) = new OpFormatContainer(ops :+ f)
  def +(o: Option[OpFormat]) = o match {
    case Some(f) => new OpFormatContainer(ops :+ f)
    case None => this
  }
  
  def get = this.ops
}

object OpFormatContainer {
  implicit def build(ofc: OpFormatContainer) = ofc.ops
}

sealed abstract class OpFormat {
  def +(f: OpFormat) = new OpFormatContainer(Vector(this, f))
  //def wrap = new OpFormatContainer(Vector(this))
  def size: Int
}

object OpFormat {
  //implicit def wrap(of: OpFormat) = new OpFormatContainer(Vector(of))
  implicit def wrapVect(of: OpFormat) = Vector(of)
}

case class ConstByte(b: Byte) extends OpFormat {
  override def size = 1
}
//case class ConstShort(s: Short) extends OpFormat {
//  override def size = 2
//}
case class ConstInt(i: Int) extends OpFormat {
  override def size = 4
}
case class ConstLong(l: Long) extends OpFormat {
  override def size = 8
}
case class VariableInt(v: String) extends OpFormat {
  override def size = 4
}
case class LabelInt(l: String) extends OpFormat {
  override def size = 4
}
case class LabelByte(l: String) extends OpFormat {
  override def size = 1
}
case class FunShort(f: String) extends OpFormat {
  override def size = 2
}
