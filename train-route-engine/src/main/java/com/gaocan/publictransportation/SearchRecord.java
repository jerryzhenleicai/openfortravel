package com.gaocan.publictransportation;

import java.io.Serializable;
import java.util.Date;

public abstract class SearchRecord implements Serializable {
    private String srcStation;

    private String destStation;

    private transient Date date;

    private String ip;

    private transient String agent;

    public String getDestStation() {
        return destStation;
    }

    public void setDestStation(String destStation) {
        this.destStation = destStation;
    }

    public String getSrcStation() {
        return srcStation;
    }

    public void setSrcStation(String srcStation) {
        this.srcStation = srcStation;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(srcStation == null ? "null" : srcStation);
        sb.append("->");
        sb.append(destStation == null ? "null" : destStation);
        sb.append(", IP:");
        sb.append(ip == null ? "null" : ip);
        sb.append((date == null ? "***" : date.toString()) + ',');
        sb.append(agent == null ? "null" : agent);
        return sb.toString();
    }
}
