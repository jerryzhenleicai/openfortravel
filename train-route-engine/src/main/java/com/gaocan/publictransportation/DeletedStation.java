package com.gaocan.publictransportation;

public class DeletedStation extends LineDiff {
	private String stationName;
	public DeletedStation(String sName) {
		stationName = sName;
	}
	public String getStationName() {
		return stationName;
	}
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}
	public String toString() {
		return "Deleted " + stationName;
	}

}

