/**
 * 
 */
package com.gaocan.publictransportation;

/**
 * @author xwang
 *
 */
public class ChangedTime extends LineDiff {
    private String time;

    public ChangedTime(String time) {
        this.time = time;
    }
    
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    
    public String toString() {
        return "schedule time changed:" + time;
    }
}
