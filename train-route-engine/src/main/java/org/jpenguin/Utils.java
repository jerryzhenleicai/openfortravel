package org.jpenguin;

import org.jpenguin.util.Base64;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

public class Utils {


    static final String encKey = "AE7045D098673121";
    public static final String EMAIL_SEND = "com.gaocan.emailsend";
    public static final String EMAIL_SEND_REAL = "real";

    public static final String RUN_MODE_KEY = "app.run.mode";
    public static final String RUN_MODE_DEV = "development";
    public static final String RUN_MODE_PROD = "production";

    /** How many milisecs the Chinese time is ahead of US time */
    static int time_diff_ms;
    static boolean locale_is_china = true;
    public static java.util.TimeZone chinese_time_zone;
    public static int MILISECONDS_IN_A_DAY = 86400000;

    private static Properties configProperties = new Properties();

    static {
        if (Utils.isRunningInProductionMode()) {
            // properties that are different based on running mode etc.
            Utils.loadProperties("config-prod.properties");
        } else {
            Utils.loadProperties("config-dev.properties");
        }
        chinese_time_zone = java.util.TimeZone.getTimeZone("Asia/Shanghai");
        java.util.TimeZone tz = java.util.TimeZone.getDefault();
        int local_offset = tz.getRawOffset();
        int chinese_offset = chinese_time_zone.getRawOffset();
        time_diff_ms = chinese_offset - local_offset;
        Date dt = new Date(System.currentTimeMillis());
        if (chinese_time_zone.useDaylightTime() && !tz.useDaylightTime() && chinese_time_zone.inDaylightTime(dt)) {
            time_diff_ms += 3600000;
        } else if (!chinese_time_zone.useDaylightTime() && tz.useDaylightTime() && tz.inDaylightTime(dt)) {
            time_diff_ms -= 3600000;
        }
    }

    /** return how many miliseconds China time is ahead of GMT (London time) */
    static public int getOffsetBetweenChinaTimeAndUTC() {
        return chinese_time_zone.getRawOffset();
    }

    static public String getURLString(String inp) {
        return Utils.replaceCharInStr(inp, ' ', "%20");
    }

    /**
     * given a HTML form string, convert it to something suitable for SQL query
     * or insert
     */
    public static String getSQLStr(String val) {
        if (val == null) {
            return "null";
        }

        val = replaceCharInStr(val, '\'', "\\\'");
        return ((val.length() == 0) ? "null" : ("'" + val + "'"));
    }

    public static String replaceCharInStr(String val, char oldch, String new_str) {
        if (Utils.isNull(val)) {
            return "";
        }

        // make sure there's no ' in the str
        int qp = val.indexOf(oldch);
        while (qp != -1) {
            val = val.substring(0, qp) + new_str + val.substring(qp + 1);
            qp = val.indexOf(oldch, qp + 2);
        }
        return val;
    }

    public static boolean isNull(String s) {
        if (s == null) {
            return true;
        } else {
            return s.equals("");
        }
    }

    public static boolean isFieldString(String val, String fname) {
        if (isNull(val)) {
            return false;
        }

        return (!Character.isDigit(val.charAt(0)) || fname.equalsIgnoreCase("phone") || fname.equalsIgnoreCase("fax"));
    }

    public static String getResultString(ResultSet rs, String field_name) throws SQLException {
        String val;
        val = rs.getString(field_name);
        if (val == null) {
            return null;
        }

        val = val.trim();
        return val;
    }

    public static String getCurrDateStr() {
        return "'" + new java.sql.Date(time_diff_ms + System.currentTimeMillis()) + "'";
    }

    public static int getChineseUSTimeDiffMilisec() {
        return time_diff_ms;
    }

    public static int getCurrYear() {
        return java.util.Calendar.getInstance(chinese_time_zone).get(java.util.Calendar.YEAR);
    }

    public static int getCurrMonth() {
        return java.util.Calendar.getInstance(chinese_time_zone).get(java.util.Calendar.MONTH) + 1; // 1
                                                                                                    // means
                                                                                                    // Jan.
    }

    public static int getCurrDayInMonth() {
        return java.util.Calendar.getInstance(chinese_time_zone).get(java.util.Calendar.DAY_OF_MONTH);
    }

    public static int getCurrHourInDay() {
        return java.util.Calendar.getInstance(chinese_time_zone).get(java.util.Calendar.HOUR_OF_DAY);
    }

    public static String getDateChooserHTML(String year_input_name, String month_input_name, String day_input_name) {
        int year = Utils.getCurrYear();
        StringBuffer str = new StringBuffer(1024);
        str.append("<SELECT name=" + year_input_name + " value=" + year + ">");
        for (int i = -2; i < 5; i++) {
            str.append("<OPTION");
            if (i == 0) {
                str.append(" selected");
            }
            str.append(">" + (i + year));
        }
        str.append("</SELECT>年 <SELECT name=" + month_input_name + ">");
        int mon = Utils.getCurrMonth();
        for (int i = 1; i <= 12; i++) {
            str.append("<OPTION");

            if (i == mon) {
                str.append(" selected");
            }
            str.append(">" + i);
        }

        str.append(" </SELECT> 月 <SELECT name=" + day_input_name + " value=" + Utils.getCurrDayInMonth() + ">");
        int day = Utils.getCurrDayInMonth();
        for (int i = 1; i <= 31; i++) {
            str.append("<OPTION");

            if (i == day) {
                str.append(" selected");
            }
            str.append(">" + i);
        }
        str.append("</SELECT> 日 ");

        return new String(str);
    }

    public static String getFutureDateStr(int days_into_future) {
        long curr_ms;
        long diff;

        curr_ms = System.currentTimeMillis();
        diff = days_into_future;
        diff = diff + (MILISECONDS_IN_A_DAY * diff);
        return "'" + new java.sql.Date(time_diff_ms + curr_ms + diff) + "'";
    }

    public static String getDateStrForCalendarDate(Calendar date) {
        long mili = date.getTime().getTime();
        return Utils.getDateStrForMilisec(mili);
    }

    public static String getDateStrForFutureCalendarDate(Calendar date, int days_into_future) {
        long mili = date.getTime().getTime();
        long end_mili = mili + (MILISECONDS_IN_A_DAY * (long) days_into_future);
        return Utils.getDateStrForMilisec(end_mili);
    }

    public static String getDateStrForMilisec(long milisec) {
        return "'" + new java.sql.Date(time_diff_ms + milisec) + "'";
    }

    public static String getTimeStrForMilisec(long milisec) {
        return "'" + new java.sql.Time(time_diff_ms + milisec) + "'";
    }

    public static String getCurrTimeStr() {
        return "'" + new java.sql.Time(time_diff_ms + System.currentTimeMillis()) + "'";
    }


    public static String GB2Unicode(String val) {
        if (val == null) {
            return null;
        }
        try {
            return new String(val.getBytes("ISO8859_1"), "GB2312");
        } catch (java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String Unicode2GB(String val) {
        try {
            if (val == null) {
                return null;
            }
            return new String(val.getBytes("GB2312"), "ISO8859_1");
        } catch (java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getStringForCents(int cents) {
        float y = (float) cents;
        return (y / 100.) + "元";
    }

    public static boolean isIPLocal(String ip_addr) {
        return ip_addr.startsWith("192.168.1.");
    }

    public static java.util.Locale getLocale() {
        if (locale_is_china) {
            return java.util.Locale.PRC;
        } else {
            return java.util.Locale.US;
        }
    }

    public static TimeZone getChinaTimeZone() {
        return chinese_time_zone;
    }

    /**
     * 
     */
    public static String convertStringToUnicode(String str, String encoding) {
        try {
            InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(str.getBytes()), encoding);
            char[] buf = new char[1024];
            int read = in.read(buf);
            return new String(buf, 0, read);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }



    static public String paddedIntToHexStr(int i) {
        return paddedIntToHexStr(i, 4);
    }

    static public String paddedIntToHexStr(int i, int width) {
        String str = Integer.toHexString(i);

        // pad 0
        if (str.length() != width) {
            int zeros = width - str.length();
            for (int j = 0; j < zeros; j++) {
                str = "0" + str;
            }
        }
        return str;
    }


    public static boolean isRunningInProductionMode() {
        return RUN_MODE_PROD.equals(getProperty(RUN_MODE_KEY));
    }

    public static void loadProperties(String file) {
        InputStream is = null;
        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
            if (is == null) {
                System.err.println(file + " not found in class path, assume no custom properties");
            } else {
                System.err.println("Loading properties from " + file);
                configProperties.load(is);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {
                    ;
                }
            }
        }
    }

    public static void setProperty(String key, String val) {
        configProperties.put(key, val);
    }

    public static String getProperty(String key) {
        String sysVal = System.getProperty(key);
        if (sysVal != null)
            return sysVal;
        else
            return configProperties.getProperty(key);
    }

    /**
     * BASE64 of SHA
     * 
     * @param in
     * @return
     */
    public static String getShaHash(String in) {
        try {
            // get password hash
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(in.getBytes("utf8"));
            byte[] hash = md.digest();
            String base64 = Base64.encodeBytes(hash);
            return base64;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException ence) {
            throw new RuntimeException(ence);
        }
    }




}
