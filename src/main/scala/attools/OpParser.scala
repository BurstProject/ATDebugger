package attools

import scala.collection.immutable.HashSet

object OpParser {
  
  val whitespace = "^\\s*$".r
  val label = "(\\w+):".r
  val comment = "\\^comment\\s+(.*)".r
  val declare = "\\^declare\\s+(\\w+)".r
  val allocate = "\\^allocate\\s+(\\w+)\\s+(\\d+)".r
  //val at_macro = "\\.(.*)".r
  val set_val = "SET\\s+@(\\w+)\\s+#([\\da-f]+)".r
  val set_dat = "SET\\s+@(\\w+)\\s+\\$(\\w+)".r
  val clr_dat = "CLR\\s+@(\\w+)".r
  val inc_dat = "INC\\s+@(\\w+)".r
  val dec_dat = "DEC\\s+@(\\w+)".r
  val add_dat = "ADD\\s+@(\\w+)\\s+\\$(\\w+)".r
  val sub_dat = "SUB\\s+@(\\w+)\\s+\\$(\\w+)".r
  val mul_dat = "MUL\\s+@(\\w+)\\s+\\$(\\w+)".r
  val div_dat = "DIV\\s+@(\\w+)\\s+\\$(\\w+)".r
  val bor_dat = "BOR\\s+@(\\w+)\\s+\\$(\\w+)".r
  val and_dat = "AND\\s+@(\\w+)\\s+\\$(\\w+)".r
  val xor_dat = "XOR\\s+@(\\w+)\\s+\\$(\\w+)".r
  val not_dat = "NOT\\s+@(\\w+)".r
  val set_ind = "SET\\s+@(\\w+)\\s+\\$\\(\\$(\\w+)\\)".r
  val set_idx = "SET\\s+@(\\w+)\\s+\\$\\(\\$(\\w+)\\s*\\+\\s*\\$(\\w+)\\)".r
  val psh_dat = "PSH\\s+\\$(\\w+)".r
  val pop_dat = "POP\\s+@(\\w+)".r
  val jmp_sub = "JSR\\s+:(\\w+)".r
  val ret_sub = "RET".r
  val ind_dat = "SET\\s+@\\(\\$(\\w+)\\)\\s+\\$(\\w+)".r
  val idx_dat = "SET\\s+@\\(\\$(\\w+)\\s*\\+\\s*\\$(\\w+)\\)\\s+\\$(\\w+)".r
  val mod_dat = "MOD\\s+@(\\w+)\\s+\\$(\\w+)".r
  val shl_dat = "SHL\\s+@(\\w+)\\s+\\$(\\w+)".r
  val shr_dat = "SHR\\s+@(\\w+)\\s+\\$(\\w+)".r
  val jmp_adr = "JMP\\s+:(\\w+)".r
  val bzr_dat = "BZR\\s+\\$(\\w+)\\s+:(\\w+)".r
  val bnz_dat = "BNZ\\s+\\$(\\w+)\\s+:(\\w+)".r
  val bgt_dat = "BGT\\s+\\$(\\w+)\\s+\\$(\\w+)\\s+:(\\w+)".r
  val blt_dat = "BLT\\s+\\$(\\w+)\\s+\\$(\\w+)\\s+:(\\w+)".r
  val bge_dat = "BGE\\s+\\$(\\w+)\\s+\\$(\\w+)\\s+:(\\w+)".r
  val ble_dat = "BLE\\s+\\$(\\w+)\\s+\\$(\\w+)\\s+:(\\w+)".r
  val beq_dat = "BEQ\\s+\\$(\\w+)\\s+\\$(\\w+)\\s+:(\\w+)".r
  val bne_dat = "BNE\\s+\\$(\\w+)\\s+\\$(\\w+)\\s+:(\\w+)".r
  val fiz_dat = "FIZ\\s+\\$(\\w+)".r
  val slp_dat = "SLP\\s+\\$(\\w+)".r
  val stz_dat = "STZ\\s+\\$(\\w+)".r
  val fin_imd = "FIN".r
  val stp_imd = "STP".r
  val err_adr = "ERR\\s+:(\\w+)".r
  val set_pcs = "PCS".r
  val ext_fun = "FUN\\s+(\\w+)".r
  val ext_fun_dat = "FUN\\s+(\\w+)\\s+\\$(\\w+)".r
  val ext_fun_dat_2 = "FUN\\s+(\\w+)\\s+\\$(\\w+)\\s+\\$(\\w+)".r
  val ext_fun_ret = "FUN\\s+@(\\w+)\\s+(\\w+)".r
  val ext_fun_ret_dat = "FUN\\s+@(\\w+)\\s+(\\w+)\\s+\\$(\\w+)".r
  val ext_fun_ret_dat_2 = "FUN\\s+@(\\w+)\\s+(\\w+)\\s+\\$(\\w+)\\s+\\$(\\w+)".r
  val nop = "NOP".r
}

class OpParser {
  
  import OpParser._
  import Conversion._
  
  var currentLabels: Option[Vector[String]] = None
  var usedLabels = HashSet[String]()
  
  def parseLines(lines: Iterable[String]) = {
    for(line <- lines) yield {
      val op: Option[OpCode] = line match {
        case label(name) => Label(name)
        case comment(comment) => Comment(comment)
        case declare(name) => Declare(name)
        case allocate(name, num) => Allocate(name, num.toInt)
        case set_val(dst, src) => SET_VAL(dst, valToLong(src))
        case set_dat(dst, src) => SET_DAT(dst, src)
        case clr_dat(loc) => CLR_DAT(loc)
        case inc_dat(loc) => INC_DAT(loc)
        case dec_dat(loc) => DEC_DAT(loc)
        case add_dat(dst, src) => ADD_DAT(dst, src)
        case sub_dat(dst, src) => SUB_DAT(dst, src)
        case mul_dat(dst, src) => MUL_DAT(dst, src)
        case div_dat(dst, src) => DIV_DAT(dst, src)
        case bor_dat(dst, src) => BOR_DAT(dst, src)
        case and_dat(dst, src) => AND_DAT(dst, src)
        case xor_dat(dst, src) => XOR_DAT(dst, src)
        case not_dat(loc) => NOT_DAT(loc)
        case set_ind(dst, src) => SET_IND(dst, src)
        case set_idx(dst, src1, src2) => SET_IDX(dst, src1, src2)
        case psh_dat(loc) => PSH_DAT(loc)
        case pop_dat(loc) => POP_DAT(loc)
        case jmp_sub(loc) => JMP_SUB(loc)
        case ret_sub() => RET_SUB()
        case ind_dat(dst, src) => IND_DAT(dst, src)
        case idx_dat(dst1, dst2, src) => IDX_DAT(dst1, dst2, src)
        case mod_dat(dst, src) => MOD_DAT(dst, src)
        case shl_dat(dst, src) => SHL_DAT(dst, src)
        case shr_dat(dst, src) => SHR_DAT(dst, src)
        case jmp_adr(loc) => JMP_ADR(loc)
        case bzr_dat(loc, dst) => BZR_DAT(loc, dst)
        case bnz_dat(loc, dst) => BNZ_DAT(loc, dst)
        case bgt_dat(var1, var2, dst) => BGT_DAT(var1, var2, dst)
        case blt_dat(var1, var2, dst) => BLT_DAT(var1, var2, dst)
        case bge_dat(var1, var2, dst) => BGE_DAT(var1, var2, dst)
        case ble_dat(var1, var2, dst) => BLE_DAT(var1, var2, dst)
        case beq_dat(var1, var2, dst) => BEQ_DAT(var1, var2, dst)
        case bne_dat(var1, var2, dst) => BNE_DAT(var1, var2, dst)
        case slp_dat(loc) => SLP_DAT(loc)
        case fiz_dat(loc) => FIZ_DAT(loc)
        case stz_dat(loc) => STZ_DAT(loc)
        case fin_imd() => FIN_IMD()
        case stp_imd() => STP_IMD()
        case err_adr(loc) => ERR_ADR(loc)
        case set_pcs() => SET_PCS()
        case ext_fun(fun) => EXT_FUN(fun)
        case ext_fun_dat(fun, arg) => EXT_FUN_DAT(fun, arg)
        case ext_fun_dat_2(fun, arg1, arg2) => EXT_FUN_DAT_2(fun, arg1, arg2)
        case ext_fun_ret(dst, fun) => EXT_FUN_RET(dst, fun)
        case ext_fun_ret_dat(dst, fun, arg) => EXT_FUN_RET_DAT(dst, fun, arg)
        case ext_fun_ret_dat_2(dst, fun, arg1, arg2) => EXT_FUN_RET_DAT_2(dst, fun, arg1, arg2)
        case nop() => NOP()
        case whitespace() => None
        case s: String => throw UnknownLineException(s)
        case _ => None
      }
	  op
    }
  }.flatten toVector;
  
  implicit def wrapOps(op: OpCode): Option[OpCode] = Some(op)
}