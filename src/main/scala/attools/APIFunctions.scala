package attools

import scala.collection.mutable.HashMap

object APIFunctions {
  
  val functions = new HashMap[String, Short]
  functions += "get_A1" -> 0x0100
  functions += "get_A2" -> 0x0101
  functions += "get_A3" -> 0x0102
  functions += "get_A4" -> 0x0103
  functions += "get_B1" -> 0x0104
  functions += "get_B2" -> 0x0105
  functions += "get_B3" -> 0x0106
  functions += "get_B4" -> 0x0107
  functions += "set_A1" -> 0x0110
  functions += "set_A2" -> 0x0111
  functions += "set_A3" -> 0x0112
  functions += "set_A4" -> 0x0113
  functions += "set_A1_A2" -> 0x0114
  functions += "set_A3_A4" -> 0x0115
  functions += "set_B1" -> 0x0116
  functions += "set_B2" -> 0x0117
  functions += "set_B3" -> 0x0118
  functions += "set_B4" -> 0x0119
  functions += "set_B1_B2" -> 0x011a
  functions += "set_B3_B4" -> 0x011b
  functions += "clear_A" -> 0x0120
  functions += "clear_B" -> 0x0121
  functions += "clear_A_B" -> 0x0122
  functions += "copy_A_From_B" -> 0x0123
  functions += "copy_B_From_A" -> 0x0124
  functions += "check_A_Is_Zero" -> 0x0125
  functions += "check_B_Is_Zero" -> 0x0126
  functions += "check_A_equals_B" -> 0x0127
  functions += "swap_A_and_B" -> 0x0128
  functions += "OR_A_with_B" -> 0x0129
  functions += "OR_B_with_A" -> 0x012a
  functions += "AND_A_with_B" -> 0x012b
  functions += "AND_B_with_A" -> 0x012c
  functions += "XOR_A_with_B" -> 0x012d
  functions += "XOR_B_with_A" -> 0x012e
  functions += "add_A_to_B" -> 0x0140
  functions += "add_B_to_A" -> 0x0141
  functions += "sub_A_from_B" -> 0x0142
  functions += "sub_B_from_A" -> 0x0143
  functions += "mul_A_by_B" -> 0x0144
  functions += "mul_B_by_A" -> 0x0145
  functions += "div_A_by_B" -> 0x0146
  functions += "div_B_by_A" -> 0x0147
  functions += "MD5_A_to_B" -> 0x0200
  functions += "check_MD5_A_with_B" -> 0x0201
  functions += "HASH160_A_to_B" -> 0x0202
  functions += "check_HASH160_A_with_B" -> 0x0203
  functions += "SHA256_A_to_B" -> 0x0204
  functions += "check_SHA256_A_with_B" -> 0x0205
  functions += "get_Block_Timestamp" -> 0x0300
  functions += "get_Creation_Timestamp" -> 0x0301
  functions += "get_Last_Block_Timestamp" -> 0x0302
  functions += "put_Last_Block_Hash_In_A" -> 0x0303
  functions += "A_to_Tx_after_Timestamp" -> 0x0304
  functions += "get_Type_for_Tx_in_A" -> 0x0305
  functions += "get_Amount_for_Tx_in_A" -> 0x0306
  functions += "get_Timestamp_for_Tx_in_A" -> 0x0307
  functions += "get_Ticket_Id_for_Tx_in_A" -> 0x0308
  functions += "message_from_Tx_in_A_to_B" -> 0x0309
  functions += "B_to_Address_of_Tx_in_A" -> 0x030a
  functions += "B_to_Address_of_Creator" -> 0x030b
  functions += "get_Current_Balance" -> 0x0400
  functions += "get_Previous_Balance" -> 0x0401
  functions += "send_to_Address_in_B" -> 0x0402
  functions += "send_All_to_Address_in_B" -> 0x0403
  functions += "send_Old_to_Address_in_B" -> 0x0404
  functions += "send_A_to_Address_in_B" -> 0x0405
  functions += "add_Minutes_to_Timestamp" -> 0x0406
}