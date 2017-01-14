package org.jpenguin.util;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c)
 * Company:
 * @author
 * @version 1.0
 */
public class HexConverter {

    /**
     * All possible chars for HEX
     */
    final static char[] hex_digits = {
	'0' , '1' , '2' , '3' , '4' , '5' ,
	'6' , '7' , '8' , '9' , 'A' , 'B' ,
	'C' , 'D' , 'E' , 'F'
    };

   /**
    * Given a byte buffer, convert it to ASCII string where the 2 chars in string is HEX of the byte
    */
    static public String  BytesToHexStr (byte[] bytes, int count)
    {
     // System.out.print("Byes len" +  bytes.length + " count " + count);
      char []  char_buf = new char [count * 2];
      // scan input
      int radix = 16;
      int mask = radix - 1;
      int b_int;
      int   i;
      for (i = 0 ; i < count ; i++)
      {
          b_int = bytes [i] ;
          char_buf [2 * i + 1] = hex_digits [b_int & mask];
          b_int >>>= 4;
          char_buf [2 * i ] = hex_digits [b_int & mask];
      }
      return new String (char_buf, 0, 2 * count);
    }


   /**
    * Given a string, convert it to a HEX string (e.g. "\u4ef5" -> "4EF5")
    * where the 4 chars in HEX string is HEX of the original char
    */
    static public String  UnicodeToHexStr (String str)
    {
      int count = str.length();
      char[]  chars = str.toCharArray();
      char [] char_buf = new char [count * 4];
      // scan input
      int radix = 16;
      int mask = radix - 1;
      int b_int;
      int   i;
      for (i = 0 ; i < count ; i++)
      {
          b_int = (int) chars [i];
          char_buf [4 * i + 3] = hex_digits [b_int & mask];
          b_int >>>= 4;
          char_buf [4 * i + 2] = hex_digits [b_int & mask];
          b_int >>>= 4;
          char_buf [4 * i + 1] = hex_digits [b_int & mask];
          b_int >>>= 4;
          char_buf [4 * i] = hex_digits [b_int & mask];
      }
      return new String (char_buf, 0, 4 * count);
    }

  /**
   * Given HEX str, get byte array
   */
    static public org.jpenguin.util.ByteBuffer HexStrToBytes (String str)
    {
      char []   char_buf = str.toCharArray();
      int       count = char_buf.length;
      byte[]    buf = new byte [count / 2];
      byte      result;
      int       digit;

      // scan input
      for (int i = 0 ; i < count / 2; i++)
      {
        digit = char_buf [2 * i];
        if (digit >= 'A')
          digit = digit - 'A' + 10;
        else
          digit = digit - '0';
        digit <<= 4;
	result = (byte) digit;
        digit = char_buf [2 * i + 1];
        if (digit >= 'A')
          digit = digit - 'A' + 10;
        else
          digit = digit - '0';
	result += digit;
        buf [i] = result;
      }

      return new org.jpenguin.util.ByteBuffer (buf, count / 2);
    }

}

