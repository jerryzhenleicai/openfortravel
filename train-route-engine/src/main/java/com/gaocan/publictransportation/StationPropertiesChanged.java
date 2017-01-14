package com.gaocan.publictransportation;

public class StationPropertiesChanged extends LineDiff {
	private String oldProps;
	private String newProps;
	private String stationName;
	
	public String getNewProps() {
		return newProps;
	}
	public void setNewProps(String newProps) {
		this.newProps = newProps;
	}
	public String getOldProps() {
		return oldProps;
	}
	public void setOldProps(String oldProps) {
		this.oldProps = oldProps;
	}
	
	public StationPropertiesChanged(String sName, String o, String n) {
		oldProps = o;
		newProps = n;
		stationName = sName;
	}
	public String toString() {
		return  stationName + ":(" + oldProps + " --> " + newProps + ")";
	}
	public String getStationName() {
		return stationName;
	}
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}
}
