package com.gaocan.train.route;

import com.gaocan.publictransportation.SearchRecord;

public class TrainSearchRequest extends SearchRecord implements Cloneable {
    /**
     * 
     */
    private static final long serialVersionUID = 8843877562761665952L;

    private transient boolean srcTimeContraint = false;

    private transient boolean destTimeContraint = false;

    private transient int earliestArriveHour;

    private transient int latestArriveHour;

    private transient int earliestDepartHour;

    private transient int latestDepartHour;

    public static final byte SORT_BY_TRIP_TIME = 0;
    public static final byte SORT_BY_PRICE = 1;
    public static final byte SORT_BY_DEPART_TIME = 2;
    public static final byte SORT_BY_ARRIVE_TIME = 3;
    
    private byte sortOrder = SORT_BY_TRIP_TIME;
    
    public boolean isDestTimeContraint() {
        return destTimeContraint;
    }

    public void setDestTimeContraint(boolean destTimeContraint) {
        this.destTimeContraint = destTimeContraint;
    }

    public boolean isSrcTimeContraint() {
        return srcTimeContraint;
    }

    public void setSrcTimeContraint(boolean srcTimeContraint) {
        this.srcTimeContraint = srcTimeContraint;
    }

    public int getEarliestArriveHour() {
        return earliestArriveHour;
    }

    public void setEarliestArriveHour(int earliestArriveHour) {
        this.earliestArriveHour = earliestArriveHour;
    }

    public int getEarliestDepartHour() {
        return earliestDepartHour;
    }

    public void setEarliestDepartHour(int earliestDepartHour) {
        this.earliestDepartHour = earliestDepartHour;
    }

    public int getLatestArriveHour() {
        return latestArriveHour;
    }

    public void setLatestArriveHour(int latestArriveHour) {
        this.latestArriveHour = latestArriveHour;
    }

    public int getLatestDepartHour() {
        return latestDepartHour;
    }

    public void setLatestDepartHour(int latestDepartHour) {
        this.latestDepartHour = latestDepartHour;
    }

    public byte getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(byte sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String toString() {
        return (sortOrder != SORT_BY_TRIP_TIME ? "S<" + sortOrder + ">" : "") +  super.toString();
    }
    
    public TrainSearchRequest getClone() {
        try {
            return  (TrainSearchRequest) this.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
