package com.gaocan.publictransportation;


/**
 * Given a string construct the right station class
 */
public interface AbstractStationFactory {
	public Station createStation(String name);
}