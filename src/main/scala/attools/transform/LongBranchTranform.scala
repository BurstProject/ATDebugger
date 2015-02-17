package attools.transform

import attools._

object LongBranchTranform {
  def apply(ops: Vector[OpCode], opPos: Vector[Int], labelPos: Map[String, Int]) = {
    var out = Vector[OpCode]()
    var labelNum = 0
    
    ops zip opPos foreach { _ match {
        case (b: GenericBranch, pos) if tooFar(pos, b.dest, labelPos) => {
          val label = "autogen_longbranch_" + {labelNum += 1; labelNum - 1}
          out = out ++ Vector(convertedOp(b, label), JMP_ADR(b.dest), Label(label))
        }
        case (op, _) => out = out :+ op
      }
    }
    
    out
  }
  
  def apply(op: OpCode, pos: Int, labelPos: Map[String, Int]) = op match {
    case b: GenericBranch if tooFar(pos, b.dest, labelPos) => {
      val label = "autogen_longbranch_" + pos
      Vector(convertedOp(b, label), JMP_ADR(b.dest), Label(label))
    }
    case o => Vector(o)
  }
  
  def findPos(ops: Vector[OpCode]) = {
    var curPos = 0
    var labelPos = Map[String, Int]()
    var opPos = Vector[Int]()
    
    ops foreach { op => op match {
        case Label(name) => {
          if(labelPos contains name) {
            throw DuplicateLabelException(name)
          }
          labelPos = labelPos + (name -> curPos)
        }
        case _ =>
      }
      opPos = opPos :+ curPos
      curPos += op.maxSize
    }
    
    (opPos, labelPos)
  }
  
  def tooFar(pos: Int, dst: String, labelPos: Map[String, Int]): Boolean = {
    val dstPos = labelPos.get(dst) match {
      case Some(d) => d
      case None => throw LabelNotFoundException(dst)
    }
    dstPos - pos match {
      case d if d > 127 => true
      case d if d < -128 => true
      case _ => false
    }
  }
  
  def convertedOp(b: GenericBranch, label: String) = b match {
    case BZR_DAT(loc, _) => BNZ_DAT(loc, label)
    case BNZ_DAT(loc, _) => BZR_DAT(loc, label)
    case BGT_DAT(var1, var2, _) => BLE_DAT(var1, var2, label)
    case BLT_DAT(var1, var2, _) => BGE_DAT(var1, var2, label)
    case BGE_DAT(var1, var2, _) => BLT_DAT(var1, var2, label)
    case BLE_DAT(var1, var2, _) => BGT_DAT(var1, var2, label)
    case BEQ_DAT(var1, var2, _) => BNE_DAT(var1, var2, label)
    case BNE_DAT(var1, var2, _) => BEQ_DAT(var1, var2, label)
  }
}