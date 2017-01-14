package org.jpenguin.util;

/**
 * class encapsulating a buffer of bytes
 */
public class ByteBuffer {
   public byte[] buf;
   public int    len;

    public ByteBuffer (byte[] buffer, int len1) {
      buf = buffer;
      len = len1;
    }
}

