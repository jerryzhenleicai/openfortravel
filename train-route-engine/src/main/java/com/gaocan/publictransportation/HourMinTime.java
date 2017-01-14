package com.gaocan.publictransportation;

import java.io.Serializable;


public class HourMinTime implements Comparable, Serializable {
    private static final long serialVersionUID = 6741261077221668479L;
    
    static public final HourMinTime MIDNIGHT = new HourMinTime(0);
    static final int mins_per_day = 24 * 60;
    private int hour;
    private int minute;

    public HourMinTime() {
    }
    
    public HourMinTime(int _minutes) {
        setHour(_minutes / 60);
        setMinute(_minutes - (getHour() * 60));
        setHour(getHour() % 24);
    }

    public HourMinTime(int h, int m) throws InvalidHMTimeFormatException {
        setHour(h);
        setMinute(m);

        if ((getHour() >= 24) || (getHour() < 0) || (getMinute() >= 60) ||
                (getMinute() < 0)) {
            throw new InvalidHMTimeFormatException("" + getHour() + ":" +
                getMinute());
        }
    }

    // 6:20

    /**
     * @param fullStr 6:20, -1D:6:20 (prev day), 15.34000000 (like BanRuo) or 15.3 (means 15:30)
     * @see
     */
    public HourMinTime(String fullStr) throws InvalidHMTimeFormatException {
        try {
	        // see if day is present
	        int colon1Pos = fullStr.indexOf(':');
	        String str = fullStr;
	
	        int sepPos = str.indexOf(':');
	
	        if (sepPos == -1) {
	            sepPos = str.indexOf('.');
	
	            // 10 means 10:00 
	            if (sepPos == -1) {
                    setHour(Integer.parseInt(str));
                    setMinute(0);
                } 
            }
	
	        // not 10, 10.3 or 10.03
	        if (sepPos != -1) {
	            setHour(Integer.parseInt(str.substring(0, sepPos)));
	
	            if (str.length() >= (sepPos + 3)) {
	                setMinute(Integer.parseInt(str.substring(sepPos + 1, sepPos +
	                            3)));
	            }
	            // 15.3 
	            else {
	                setMinute(Integer.parseInt(str.substring(sepPos + 1)) * 10);
	            }
	        }
	
	        if ((getHour() >= 24) || (getHour() < 0) || (getMinute() > 59) ||
	                (getMinute() < 0)) {
	            throw new InvalidHMTimeFormatException("" + getHour() + ":" +
	                getMinute());
	        }
	        
        } catch (Exception e) {
                throw new InvalidHMTimeFormatException(fullStr);
           }	        
    }


    
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        
        HourMinTime t = (HourMinTime) o;

        return (getHour() == t.getHour()) && (getMinute() == t.getMinute());
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append(getStringWithoutDay());

        return buf.toString();
    }

    public String getStringWithoutDay() {
        StringBuffer buf = new StringBuffer();
        buf.append(getHour());
        buf.append(':');

        if (getMinute() < 10) {
            buf.append('0');
        }

        buf.append(getMinute());

        return buf.toString();
    }

    /**
     *   difference is always less than 12 hours, unit in minutes
     */
    public int minutesEarlierThan(HourMinTime time) {
        int this_min;
        int that_min;
        int res ;
        this_min = (this.getHour() * 60) + this.getMinute();
        that_min = (time.getHour() * 60) + time.getMinute();

        // hour is in 24 format, This is the 22:10 -> 1:20  case
        if (that_min < this_min) {
        	res = (mins_per_day + that_min);
        	res = res - this_min;
            if (res < 0) {
            	System.err.println("Negative time diff " + res + ":" + this_min + ", " + that_min);
            }
        } else {
            res = (that_min - this_min);
        }
        return res;
    }

    /**
     * determine if a time is earlier than another, 23:30 could be eariler than 4:00 because it's the day before
     */
    public boolean isEarlierThan(HourMinTime time) {
        if (getHour() == time.getHour()) {
            return (getMinute() < time.getMinute());
        }
        int diff = minutesEarlierThan(time);
        return diff < 21 * 60;
    }

    public boolean isValid() {
        if ((getHour() > 24) || (getHour() < 0) || (getMinute() > 59) ||
                (getMinute() < 0)) {
            return false;
        }

        // catch 0:00 from GZrail
        if ((getHour() == 0) && (getMinute() == 0)) {
            return false;
        }

        return true;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getHour() {
        return hour;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getMinute() {
        return minute;
    }

    /**
     *  compara hour and minute
     */
    public int compareTo(Object other) {
        HourMinTime time = (HourMinTime) other;
        int thisTime = getHour() * 60 + getMinute();
        int otherTime = time.getHour() * 60 + time.getMinute();
        
        if (thisTime > otherTime) {
            return 1;
        }
        if (thisTime == otherTime) {
            return 0;
        }
        return -1;
    }

}
