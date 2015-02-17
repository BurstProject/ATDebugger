package attools

abstract sealed class OpCode {
  def format: Vector[OpFormat]
  def size = format.foldLeft[Int](0)(_ + _.size)
  def maxSize = size
  def cost: Long
}

sealed protected class GenericDstSrcVars(name: String, opcode: Byte, dst: String, src: String) extends OpCode {
  def this(opcode: Byte, dst: String, src: String) = this("", opcode, dst, src)
  override def format = ConstByte(opcode) + VariableInt(dst) + VariableInt(src)
  override def toString = name + " @" + dst + " $" + src
  override def cost = 10000000L
}

sealed protected class GenericLocVar(name: String, opcode: Byte, loc: String) extends OpCode {
  override def format = ConstByte(opcode) + VariableInt(loc)
  override def toString = name + " @" + loc
  override def cost = 10000000L
}

sealed protected class GenericThreeVar(opcode: Byte, var1: String, var2: String, var3: String) extends OpCode {
  override def format = ConstByte(opcode) + VariableInt(var1) + VariableInt(var2) + VariableInt(var3)
  override def cost = 10000000L
}

sealed protected class GenericLocLabel(name: String, opcode: Byte, loc: String) extends OpCode {
  override def format = ConstByte(opcode) + LabelInt(loc)
  override def toString = name + " :" + loc
  override def cost = 10000000L
}

sealed protected class GenericNoParam(name: String, opcode: Byte) extends OpCode {
  override def format = ConstByte(opcode)
  override def toString = name
  override def cost = 10000000L
}

sealed abstract protected class GenericBranch(val dest: String) extends OpCode {
  override def cost = 10000000L
}

sealed abstract protected class GenericBranchOneVar(name: String, opcode: Byte, var1: String, dst: String) extends GenericBranch(dst) {
  override def format = ConstByte(opcode) + VariableInt(var1) + LabelByte(dst)
  override def toString = name + " $" + var1 + " :" + dst
  override def maxSize = size + JMP_ADR(dst).size
}

sealed abstract protected class GenericBranchTwoVars(name: String, opcode: Byte, var1: String, var2: String, dst: String) extends GenericBranch(dst) {
  override def format = ConstByte(opcode) + VariableInt(var1) + VariableInt(var2) + LabelByte(dst)
  override def toString = name + " $" + var1 + " $" + var2 + " :" + dst
  override def maxSize = size + JMP_ADR(dst).size
}

sealed protected class GenericFunCall(opcode: Byte, retVar: Option[String], func: String, var1: Option[String], var2: Option[String]) extends OpCode {
  override def format = ConstByte(opcode) + FunShort(func) + retVar.map(VariableInt(_)) + var1.map(VariableInt(_)) + var2.map(VariableInt(_))
  override def toString = "FUN " + retVar.map("@" + _ + " ").getOrElse("") + func + var1.map(" $" + _).getOrElse("") + var2.map(" $" + _).getOrElse("")
  override def cost = 100000000L
}

sealed protected class GenericNoOp() extends OpCode {
  override def format = new OpFormatContainer
  override def size = 0
  override def cost = 0L
}

case class SET_VAL(dst: String, src: Long) extends OpCode {
  override def format = ConstByte(0x01.asInstanceOf[Byte]) + VariableInt(dst) + ConstLong(src)
  override def toString = "SET @" + dst + " #" + Conversion.longToVal(src)
  override def cost = 10000000L
}

case class SET_DAT(dst: String, src: String) extends GenericDstSrcVars("SET", 0x02.asInstanceOf[Byte], dst, src)

case class CLR_DAT(loc: String) extends GenericLocVar("CLR", 0x03.asInstanceOf[Byte], loc)

case class INC_DAT(loc: String) extends GenericLocVar("INC", 0x04.asInstanceOf[Byte], loc)

case class DEC_DAT(loc: String) extends GenericLocVar("DEC", 0x05.asInstanceOf[Byte], loc)

case class ADD_DAT(dst: String, src: String) extends GenericDstSrcVars("ADD", 0x06.asInstanceOf[Byte], dst, src)

case class SUB_DAT(dst: String, src: String) extends GenericDstSrcVars("SUB", 0x07.asInstanceOf[Byte], dst, src)

case class MUL_DAT(dst: String, src: String) extends GenericDstSrcVars("MUL", 0x08.asInstanceOf[Byte], dst, src)

case class DIV_DAT(dst: String, src: String) extends GenericDstSrcVars("DIV", 0x09.asInstanceOf[Byte], dst, src)

case class BOR_DAT(dst: String, src: String) extends GenericDstSrcVars("BOR", 0x0A.asInstanceOf[Byte], dst, src)

case class AND_DAT(dst: String, src: String) extends GenericDstSrcVars("AND", 0x0B.asInstanceOf[Byte], dst, src)

case class XOR_DAT(dst: String, src: String) extends GenericDstSrcVars("XOR", 0x0C.asInstanceOf[Byte], dst, src)

case class NOT_DAT(loc: String) extends GenericLocVar("NOT", 0x0D.asInstanceOf[Byte], loc)

case class SET_IND(dst: String, src: String) extends GenericDstSrcVars(0x0E.asInstanceOf[Byte], dst, src) {
  override def toString = "SET @" + dst + " $($" + src + ")"
}

case class SET_IDX(dst: String, src1: String, src2: String) extends GenericThreeVar(0x0F.asInstanceOf[Byte], dst, src1, src2) {
  override def toString = "SET @" + dst + " $($" + src1 + " + $" + src2 + ")"
}

case class PSH_DAT(loc: String) extends GenericLocVar("PSH", 0x10.asInstanceOf, loc)

case class POP_DAT(loc: String) extends GenericLocVar("POP", 0x11.asInstanceOf, loc)

case class JMP_SUB(loc: String) extends GenericLocLabel("JSR", 0x12.asInstanceOf[Byte], loc)

case class RET_SUB() extends GenericNoParam("RET", 0x13.asInstanceOf[Byte])

case class IND_DAT(dst: String, src: String) extends GenericDstSrcVars(0x14.asInstanceOf[Byte], dst, src) {
  override def toString = "SET @($" + dst + ") $" + src
}

case class IDX_DAT(dst1: String, dst2: String, src: String) extends GenericThreeVar(0x15.asInstanceOf[Byte], dst1, dst2, src) {
  override def toString = "SET @($" + dst1 + " + $" + dst2 + ") $" + src
}

case class MOD_DAT(dst: String, src: String) extends GenericDstSrcVars("MOD", 0x16.asInstanceOf[Byte], dst, src)

case class SHL_DAT(dst: String, src: String) extends GenericDstSrcVars("SHL", 0x17.asInstanceOf[Byte], dst, src)

case class SHR_DAT(dst: String, src: String) extends GenericDstSrcVars("SRH", 0x18.asInstanceOf[Byte], dst, src)

case class JMP_ADR(loc: String) extends GenericLocLabel("JMP", 0x1A.asInstanceOf[Byte], loc)

case class BZR_DAT(loc: String, dst: String) extends GenericBranchOneVar("BZR", 0x1B.asInstanceOf[Byte], loc, dst)

case class BNZ_DAT(loc: String, dst: String) extends GenericBranchOneVar("BNZ", 0x1E.asInstanceOf[Byte], loc, dst)

case class BGT_DAT(var1: String, var2: String, dst: String) extends GenericBranchTwoVars("BGT", 0x1F.asInstanceOf[Byte], var1, var2, dst)

case class BLT_DAT(var1: String, var2: String, dst: String) extends GenericBranchTwoVars("BLT", 0x20.asInstanceOf[Byte], var1, var2, dst)

case class BGE_DAT(var1: String, var2: String, dst: String) extends GenericBranchTwoVars("BGE", 0x21.asInstanceOf[Byte], var1, var2, dst)

case class BLE_DAT(var1: String, var2: String, dst: String) extends GenericBranchTwoVars("BLE", 0x22.asInstanceOf[Byte], var1, var2, dst)

case class BEQ_DAT(var1: String, var2: String, dst: String) extends GenericBranchTwoVars("BEQ", 0x23.asInstanceOf[Byte], var1, var2, dst)

case class BNE_DAT(var1: String, var2: String, dst: String) extends GenericBranchTwoVars("BNE", 0x24.asInstanceOf[Byte], var1, var2, dst)

case class SLP_DAT(loc: String) extends GenericLocVar("SLP", 0x25.asInstanceOf[Byte], loc)

case class FIZ_DAT(loc: String) extends GenericLocVar("FIZ", 0x26.asInstanceOf[Byte], loc)

case class STZ_DAT(loc: String) extends GenericLocVar("STZ", 0x27.asInstanceOf[Byte], loc)

case class FIN_IMD() extends GenericNoParam("FIN", 0x28.asInstanceOf[Byte])

case class STP_IMD() extends GenericNoParam("STP", 0x29.asInstanceOf[Byte])

case class ERR_ADR(loc: String) extends GenericLocLabel("ERR", 0x2B.asInstanceOf[Byte], loc)

case class SET_PCS() extends GenericNoParam("PCS", 0x30.asInstanceOf[Byte])

case class EXT_FUN(fun: String) extends GenericFunCall(0x32.asInstanceOf[Byte], None, fun, None, None)

case class EXT_FUN_DAT(fun: String, arg: String) extends GenericFunCall(0x33.asInstanceOf[Byte], None, fun, Some(arg), None)

case class EXT_FUN_DAT_2(fun: String, arg1: String, arg2: String) extends GenericFunCall(0x34.asInstanceOf[Byte], None, fun, Some(arg1), Some(arg2))

case class EXT_FUN_RET(dst: String, fun: String) extends GenericFunCall(0x35.asInstanceOf[Byte], Some(dst), fun, None, None)

case class EXT_FUN_RET_DAT(dst: String, fun: String, arg: String) extends GenericFunCall(0x36.asInstanceOf[Byte], Some(dst), fun, Some(arg), None)

case class EXT_FUN_RET_DAT_2(dst: String, fun: String, arg1: String, arg2: String) extends GenericFunCall(0x37.asInstanceOf[Byte], Some(dst), fun, Some(arg1), Some(arg2))

case class NOP() extends GenericNoParam("NOP", 0x7F.asInstanceOf[Byte])

// not ops:

case class Label(name: String) extends GenericNoOp {
  override def toString = name + ":"
}

case class Comment(comment: String) extends GenericNoOp {
  override def toString = "^comment " + comment
}

case class Declare(name: String) extends GenericNoOp {
  override def toString = "^declare " + name
}

case class Allocate(name: String, num: Int) extends GenericNoOp {
  override def toString = "^allocate " + name + " " + num
}

// special cases:

case class SET_VAL_ALLOCATE(dst: String, src: String) extends OpCode {
  override def format = ConstByte(0x01.asInstanceOf[Byte]) + VariableInt(dst) + VariableInt(src) + ConstInt(0)
  override def toString = "SET @" + dst + " #" + src
  override def cost = 10000000L
}
