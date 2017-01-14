package com.gaocan.publictransportation;


public class InsertedStation extends LineDiff {
	private String stationName;
	public InsertedStation(String sName) {
		stationName = sName;
	}
	public String getStationName() {
		return stationName;
	}
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}
	public String toString() {
		return "Inserted " + stationName;
	}
}
