package com.gaocan.publictransportation;



/**
 * Copyright (c) 2003 Gaocan, Inc., All Rights Reserved
 * Represent the moment a line stops at one given station.
 * 
 */
public abstract class LineStationPair {
	protected transient Line line;
	protected Station station;
	protected HourMinTime departureTime;
	protected HourMinTime arrivalTime;
	protected LineStationPair() {
		// no
	}
	
	/**
	 * extra info for lsp besides station and line, such as arrival time
	 * @return
	 */
	abstract public String getExtraDataString() ;
	
	protected LineStationPair(Station s, Line l) throws  InvalidScheduleDataException{
		line = l;
		setStation(s);
		
		// update lsp list in line and station
		s.addLineStationPair(this);
		l.addLineStationPair(this);
	}

	/** 
	 * used in planning graph to compute short path, could be  running time or transfer time in minutes or walking distance between bus stops
	 * @param nextLsp next lsp on a path, such as next station on a line or a transfer lsp which is later than this lsp
	 * @throws if nextLsp cannot possibly be the next Lsp on a path for example not on the same line or same city or not adjacent station  
	 * */
	public abstract double getCostToNextLspOnPath(LineStationPair nextLsp) throws IllegalArgumentException;
	
	/**
	 * get minutes to stay in a vehicle and travel to next Lsp on the same  line, 
	 * @param nextLsp
	 * @return from departing this lsp to arrival of next lsp
	 * @throws IllegalArgumentException
	 */
	public abstract int getMinutesToTravelToNextLspOnSameLine(LineStationPair nextLsp) throws IllegalArgumentException;

	/**
	 * get how long vehicle stay on this station
	 * @return mins from arrival to departing this station
	 */
	public abstract int getVehicleStayTimeMinutes() ;
	
	/**
	 * get minutes to transfer to next lsp which can be same station or a different station in the same area, through wait, walk or taxi etc
	 * @param nextLsp
	 * @return
	 * @throws IllegalArgumentException
	 */
	public abstract int getMinutesToTransferToNextLsp(LineStationPair nextLsp) throws IllegalArgumentException;
	
	/**
	 * used in   ShortestPathPlanner as key to node hashset
	 * 
	 */
	public boolean equals(Object obj) {
		LineStationPair lsp = (LineStationPair) obj;
		return this == lsp;
	}

	public int hashCode() {
		return getStation().hashCode() + line.hashCode();
	}

	public String toString() {
		return "station" + getStation().toString() + "line" + line.toString();
	}

	public void setStation(Station station) {
		this.station = station;
	}

	public Station getStation() {
		return station;
	}

	/**
	 * @return Line
	 */
	public Line getLine() {
		return line;
	}

	/**
	 * Sets the line.
	 * @param line The line to set
	 */
	public void setLine(Line line) {
		this.line = line;
	}

	/**
	 * get the station index within the line
	 * @return
	 */
	public int getStationIndex() {
		return this.getLine().getLspIndex(this);
     }

	public void setDepartureTime(HourMinTime departure_time) {
	    this.departureTime = departure_time;
	}

	public HourMinTime getDepartureTime() {
	    return departureTime;
	}

	public void setArrivalTime(HourMinTime arrival_time) {
	    this.arrivalTime = arrival_time;
	}

	public HourMinTime getArrivalTime() {
	    return arrivalTime;
	}
}
