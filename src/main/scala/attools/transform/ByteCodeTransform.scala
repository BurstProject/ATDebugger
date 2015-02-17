package attools.transform

import attools._

object ByteCodeTransform {
  def apply(ops: Vector[OpCode], labels: Map[String, Int], vars: Map[String, Int]): Vector[Byte] = {
    var pos = 0
    
    ops flatMap { op=>
      val bytes = apply(op, pos, labels, vars)
      pos += bytes.size
      bytes
    }
  }
  
  def apply(op: OpCode, pos: Int, labels: Map[String, Int], vars: Map[String, Int]): Vector[Byte] = {
    op.format flatMap {
      case ConstByte(b) => Array(b)
      case ConstInt(i) => Conversion.intToBytes(i)
      case ConstLong(l) => Conversion.longToBytes(l)
      case VariableInt(vi) => vars.get(vi) match {
        case Some(i) => Conversion.intToBytes(i)
        case None => throw VariableNotFoundException(vi)
      }
      case LabelInt(li) => labels.get(li) match {
        case Some(i) => Conversion.intToBytes(i)
        case None => throw LabelNotFoundException(li)
      }
      case LabelByte(lb) => labels get lb match {
        case Some(b) => Array((b - pos).asInstanceOf[Byte])
        case None => throw LabelNotFoundException(lb)
      }
      case FunShort(fs) => APIFunctions.functions get fs match {
        case Some(s) => Conversion.shortToBytes(s)
        case None => throw FunctionNotFoundException(fs)
      }
    }
  }
  
  def mapLabels(ops: Vector[OpCode]) = {
    var curPos = 0
    var labelPos = Map[String, Int]()
    
    ops foreach { op => op match {
        case Label(name) => {
          if(labelPos contains name) {
            throw DuplicateLabelException(name)
          }
          labelPos = labelPos + (name -> curPos)
        }
        case _ =>
      }
      curPos += op.size
    }
    
    labelPos
  }
  
  def mapVars(ops: Vector[OpCode]) = {
    var varPos = Map[String, Int]()
    
    ops foreach { _ match {
        case Declare(name) => if(!varPos.contains(name)) {varPos = varPos + (name -> varPos.size)}
        case op => op.format foreach { _ match {
            case VariableInt(v) => if(!varPos.contains(v)) {varPos = varPos + (v -> varPos.size)}
            case _ =>
          }
        }
      }
    }
    
    varPos
  }
}