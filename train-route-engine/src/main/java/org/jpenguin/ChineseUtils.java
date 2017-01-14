package org.jpenguin;

import java.io.OutputStreamWriter;


public class ChineseUtils {
    static OutputStreamWriter utf8Decoder;

    static {
        try {
            utf8Decoder = new OutputStreamWriter(System.out, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printlnUnicode(String s) {
        printUnicode(s + '\n');
    }

    public static void printlnUnicode(String s, OutputStreamWriter ow) {
        printUnicode(s + '\n', ow);
    }

    public static void printUnicode(String s) {
        printUnicode(s, utf8Decoder);
    }

    public static void printUnicode(String s, OutputStreamWriter ow) {
        try {
            ow.write(s);
            ow.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isValidCJKChar(char ch) {
        return ch >= '\u4e00' && ch <= '\u9faf';
    }
    
    public static String quanJiao2BanJiao(String s) {
        return s;
    }
}
