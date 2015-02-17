package attools

import java.nio.ByteBuffer
import java.nio.ByteOrder

object Conversion {
  def parseHexString(hex: String): Array[Byte] = {
    if(hex == null) {
    null
  }
  else {
    val bytes = new Array[Byte](hex.length() / 2)
    for(i <- 0 until bytes.length) {
      var char1 = hex.charAt(i * 2).toInt
      char1 = (if(char1 > 0x60) char1 - 0x57 else char1 - 0x30)
      var char2 = hex.charAt(i * 2 + 1).toInt
      char2 = (if(char2 > 0x60) char2 - 0x57 else char2 - 0x30)
      if(char1 < 0 || char2 < 0 || char1 > 15 || char2 > 15)
      {
        throw new NumberFormatException("Invalid hex number: " + hex)
      }
      bytes(i) = ((char1 << 4) + char2).toByte
    }
    bytes
  }
  }
  
  val hexChars = Array( '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f' )
  
  def toHexString(bytes: Array[Byte]): String = {
    if(bytes == null) {
      null
    }
    else {
      val chars = new Array[Char](bytes.length * 2)
      for(i <- 0 until bytes.length) {
        chars(i * 2) = hexChars((bytes(i) >> 4) & 0x0F)
        chars(i * 2 + 1) = hexChars(bytes(i) & 0x0F)
      }
      String.valueOf(chars)
    }
  }
  
  def valToLong(hex: String): Long = {
    val buffer = ByteBuffer.allocate(8)
    buffer.order(ByteOrder.BIG_ENDIAN)
    buffer.put(parseHexString(hex))
    buffer.flip()
    buffer.getLong()
  }
  
  def longToVal(l: Long): String = {
    val buffer = ByteBuffer.allocate(8)
    buffer.order(ByteOrder.BIG_ENDIAN)
    buffer.putLong(l)
    toHexString(buffer.array)
  }
  
  def longToBytes(l: Long) = {
    val buffer = ByteBuffer.allocate(8)
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    buffer.putLong(l)
    buffer.array()
  }
  
  def intToBytes(i: Int) = {
    val buffer = ByteBuffer.allocate(4)
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    buffer.putInt(i)
    buffer.array()
  }
  
  def shortToBytes(s: Short) = {
    val buffer = ByteBuffer.allocate(2)
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    buffer.putShort(s)
    buffer.array()
  }
}