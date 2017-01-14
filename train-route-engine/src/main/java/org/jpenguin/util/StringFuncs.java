package org.jpenguin.util;

import java.util.StringTokenizer;
import java.util.Vector;


/**
 * class encapsulating a buffer of bytes
 */
public class StringFuncs {
    public static StringBuffer substitute(String src, String orig_str, String new_str) {
        StringBuffer result = new StringBuffer(src.length() * 2);
        int index = src.indexOf(orig_str);
        int last_end = 0;

        while (index != -1) {
            result.append(src.substring(last_end, index));
            result.append(new_str);
            last_end = index + orig_str.length();
            index = src.indexOf(orig_str, last_end);
        }

        result.append(src.substring(last_end));

        return result;
    }

    /**
     * 桑园兽药市场  郑州饲料兽药市场(公交站牌) 园田路市场
     *     翡翠城北区 , he meant 翡翠楼北区 , 清华大学 we only have 清华园
     * @param hostString
     *            string matched against the key
     * @param matchKey
     * @return a score between 0 and 100, the larger the better match is, 0
     *         means none of char is matched
     */
    public static double getMatchScore(String hostString, String matchKey) {
        assert (matchKey != null && hostString != null);
        assert (matchKey.length() > 0);
        final int maxStringLen = 12;
        if (hostString.length() > maxStringLen) {
            hostString = hostString.substring(0, maxStringLen);
        }
        final double unmatchedPenalty = (double) maxStringLen;

        char[] keyChars = matchKey.toCharArray();
        int matchPosTtl = 0;
        for (int k = 0; k < keyChars.length; k++) {
            int pos = hostString.indexOf(keyChars[k]);
            if (pos == -1) {
                // any unmatched char calculated as twice the keyword length
                matchPosTtl += unmatchedPenalty;
            } else {
                matchPosTtl += pos;
            }
        }

        double score = 0.0;
        // the minimum possible matchPosTtl is when hostString equals matchKey
        double minPosSum = 1.0; // this is for the case when keyword is just one
                                // char and then minPosSum would have been 0
        for (int i = 0; i < keyChars.length; i++) {
            minPosSum += i;
        }

        score = (matchPosTtl == keyChars.length * unmatchedPenalty ? 0 : (100 * minPosSum) / (matchPosTtl + 1.0));
        return score;
    }


    public static boolean isObscene(String val) {
        // check comment for security hazards, no <script in it allowed
        if (val.indexOf("你妈") != -1) {
            return true;
        }

        if (val.indexOf("他妈") != -1) {
            return true;
        }

        if (val.indexOf("我操") != -1) {
            return true;
        }

        if (val.indexOf("fuck") != -1) {
            return true;
        }

        if (val.indexOf("滚蛋") != -1) {
            return true;
        }

        return false;
    }
}
