package attools.transform

import attools._

object AllocateTransform {
  def apply(ops: Vector[OpCode]) = {
    var out = Vector[OpCode]()
    
    ops foreach { _ match {
        case Allocate(name, num) => {
          out = out ++ (Declare(name) +: {for(i <- 0 until num) yield Declare(name + "[" + i + "]")}.toVector :+ SET_VAL_ALLOCATE(name, name + "[0]"))
        }
        case op => out = out :+ op
      }
    }
    
    out
  }
  
  def apply(op: OpCode) = op match {
    case Allocate(name, num) => {
      Declare(name) +: {for(i <- 0 until num) yield Declare(name + "[" + i + "]")}.toVector :+ SET_VAL_ALLOCATE(name, name + "[0]") 
    }
    case o => Vector(o)
  }
}