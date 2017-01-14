package org.jpenguin;

import java.io.UnsupportedEncodingException;

public class XTEA {
	private static final long DELTA = 0x9F3779B9l;
	private static final long mask = 0xffffffffl;
	private long[] k1 = new long[32];
	private long[] k2 = new long[32];

	public void setKey(byte[] bytes) {
		long[] key = new long[4];
		long[] b = new long[bytes.length];
		for (int i = 0; i < 16; i++) {
			b[i] = bytes[i];
		}

		for (int i = 0; i < 16;) {
			key[i / 4] = (b[i++] << 24) + ((b[i++] & 0xFF) << 16)
					+ ((b[i++] & 0xFF) << 8) + (b[i++] & 0xFF);
		}
		long sum = 0;
		for (int i = 0; i < 32; i++) {
			k1[i] = key[(int) (sum & 3)];
			sum = (sum + DELTA) & mask;
			k2[i] = key[(int) ((sum >>> 11) & 3)];
		}
	}

	public String encryptToHex(String text) {
		try {
			byte[] bytes = text.getBytes("utf-8");
			// add len header and pad to eight
			int totalLen = 4 + bytes.length;
			if (totalLen % 8 != 0) {
				totalLen = (totalLen / 8) * 8 + 8;
			}
			byte[] allBytes = new byte[totalLen];
			System.arraycopy(bytes, 0, 	allBytes, 4, bytes.length);
			int netLen = bytes.length;
			allBytes [0] =  (byte) ((netLen >>> 24) & 0xFF);
			allBytes [1] =  (byte) ((netLen >>> 16) & 0xFF);
			allBytes [2] =  (byte) ((netLen >>> 8) & 0xFF);
			allBytes [3] =  (byte) ((netLen) & 0xFF);
			
			int[] out = new int[allBytes.length];
			encrypt(allBytes, out, 0, allBytes.length);
			StringBuffer buf = new StringBuffer(2 * out.length);
			for (int o : out) {
				buf.append(Utils.paddedIntToHexStr(o, 2));
			}
			return new String(buf);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
	}
	

	public String decryptFromHex(String string) {
		try {
	        byte[] buf = string.getBytes();
	        int[] intV = new int[string.length() / 2];
	        for (int i = 0; i < (string.length() / 2); i++) {
	            String hex_str = new String(buf, i * 2, 2);
	            intV[i] = Integer.parseInt(hex_str, 16);
	        }
	        byte[] plainText = new byte[intV.length];
	        decrypt(intV, plainText, 0, intV.length);
	        // first 4 bytes are length
	        int len = 0x1000000 * plainText[0] + 0x10000 * plainText[1] + 0x100 * plainText[2] + plainText[3];
	        String str = new String(plainText, 4, len, "utf-8");
	        return str;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
	}	
	public void encrypt(byte[] bytes, int[] out, int off, int len) {
		if (len % 8 != 0) {
			throw new RuntimeException("unaligned len " + len);
		}
		for (int i = off; i < off + len; i += 8) {
			encryptBlock(bytes, out, i);
		}
	}

	public void decrypt(int[] input, byte[] out, int off, int len) {
		if (len % 8 != 0) {
			throw new RuntimeException("unaligned len " + len);
		}
		for (int i = off; i < off + len; i += 8) {
			decryptBlock(input, out, i);
		}
	}

	public void encryptBlock(byte[] in, int[] out, int off) {
		long y = (in[off] << 24) | ((in[off + 1] & 0xFF) << 16)
				| ((in[off + 2] & 0xFF) << 8) | (in[off + 3] & 0xFF);
		long z = (in[off + 4] << 24) | ((in[off + 5] & 0xFF) << 16)
				| ((in[off + 6] & 0xFF) << 8) | (in[off + 7] & 0xFF);
		long sum = 0;
		for (int r = 0; r < 32; r++) {
			long yinc = ((((z << 4) & mask) ^ (z >>> 5)) + z)
					^ ((sum + k1[r]) & mask);
			y = (y + yinc) & mask;
			sum = (sum + DELTA) & mask;
			long zinc = (((y >>> 5) ^ ((y << 4) & mask)) + y) ^ (sum + k2[r]);
			z = (z + zinc) & mask;
		}
		out[off] = (int) (y >> 24 & 0xFF);
		out[off + 1] = (int) (y >> 16 & 0xFF);
		out[off + 2] = (int) (y >> 8 & 0xFF);
		out[off + 3] = (int) (y & 0xFF);
		out[off + 4] = (int) (z >> 24 & 0xFF);
		out[off + 5] = (int) (z >> 16 & 0xFF);
		out[off + 6] = (int) (z >> 8 & 0xFF);
		out[off + 7] = (int) (z & 0xFF);
	}

	public void decryptBlock(int[] in, byte[] out, int off) {
		long y = ((long) in[off] << 24) | (((long) in[off + 1] & 0xFF) << 16)
				| (((long) in[off + 2] & 0xFF) << 8)
				| ((long) in[off + 3] & 0xFF);
		long z = ((long) in[off + 4] << 24)
				| (((long) in[off + 5] & 0xFF) << 16)
				| (((long) in[off + 6] & 0xFF) << 8)
				| ((long) in[off + 7] & 0xFF);
		long sum = (32 * (long) DELTA) & mask;
		for (int r = 0; r < 32; r++) {
			long zinc = (((y >>> 5) ^ ((y << 4) & mask)) + y)
					^ ((sum + k2[31 - r]) & mask);
			z = (z - zinc) & mask;
			sum = (sum - DELTA) & mask;
			long yinc = ((((z << 4) & mask) ^ (z >>> 5)) + z)
					^ ((sum + k1[31 - r]) & mask);
			y = (y - yinc) & mask;
		}
		out[off] = (byte) (y >> 24 & 0xFF);
		out[off + 1] = (byte) (y >> 16 & 0xFF);
		out[off + 2] = (byte) (y >> 8 & 0xFF);
		out[off + 3] = (byte) (y & 0xFF);
		out[off + 4] = (byte) (z >> 24 & 0xFF);
		out[off + 5] = (byte) (z >> 16 & 0xFF);
		out[off + 6] = (byte) (z >> 8 & 0xFF);
		out[off + 7] = (byte) (z & 0xFF);
	}

	public static void main(String[] args) {
		XTEA tea = new XTEA();
		String key = "0123456789012345";
		tea.setKey(key.getBytes());
		/*
		int[] enc = new int[in.length];
		tea.encrypt(in, enc, 0, 8);
		for (int b : enc) {
			System.out.println(Integer.toHexString(b));
		}
		tea.decrypt(enc, in, 0, 8);
		*/
		String encText = tea.encryptToHex("camel987");
		System.out.println(encText);
		System.out.println(tea.decryptFromHex(encText));
	}
}
