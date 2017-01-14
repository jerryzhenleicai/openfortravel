package org.jpenguin;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;

public class PinyinTable {
    static HashMap<Character, String> char2PinYin = new HashMap<Character,String>();
    static {
        try {
            InputStream ins = new FileInputStream(Utils.getProperty("resources.dir") + File.separator + "charpinyin.txt");
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(ins, "utf-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] f = line.split(":") ;
                // f[1] is like jing1, bei3
                char2PinYin.put(f[0].charAt(0), f[1].substring(0, f[1].length() - 1));
            }
        } catch  (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** for 北京, this will return "beijing" */
    public static String getFullPinYin(String str) {
        char[] chs = str.toCharArray();
        StringBuffer out = new StringBuffer();
        for (int i = 0 ; i < chs.length; i++) {
            out.append(char2PinYin.get(chs[i]));
        }
        return out.toString();
    }
    
    /** for 北京, this will return "bj" */
    public static String getPinYinInitial(String str) {
        char[] chs = str.toCharArray();
        StringBuffer out = new StringBuffer();
        for (int i = 0 ; i < chs.length; i++) {
            out.append(char2PinYin.get(chs[i]).charAt(0));
        }
        return out.toString();
    }

}
