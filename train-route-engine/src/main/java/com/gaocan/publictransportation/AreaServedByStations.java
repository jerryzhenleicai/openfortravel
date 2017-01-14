package com.gaocan.publictransportation;

import java.util.Iterator;



/**
 * An area served by one or more stations, examples are a city served by 2 train stations,  
 * an area in city served by many bus stations such as a street, a park, Wai Tan in Shanghai.
 * Beijing has Beijing West, Beijing East stations. Department store can be reached from a few
 * bus stops.  Note the station and area has a m:n relationship,  like in a park
 * can be reached from multiple bus stations and a park and a street can be reached from the same station.
 * Areas can have a containment and adjacent relationships. Such as a department store is contained in 
 * PuXi road which is in PuDong district. The same department store is close to a park. 
 *  
 */

public interface AreaServedByStations extends Comparable<AreaServedByStations> {
	/**
	 * Number of transportation lines (bus) passing this area
	 * @return
	 */
	public abstract int getNumLinesPassing();
	public abstract void setName(String name);
	public abstract String getName();
	
	/** another station serving this area */ 
	public void addStation(Station station);
	public Iterator<LineStationPair> getLineStationIterator();
	
	/**
	 * Number of stations serving this area
	 * @return
	 */
	public abstract int getNumStations();
	
	/**
	 * Are two areas close?
	 * @param neighbor
	 * @return
	 */
	public abstract boolean isAdjacentTo(AreaServedByStations neighbor);
	/**
	 * examples are districts contained in a city, parks in a district etc.
	 * @return
	 */
	public abstract Iterator<AreaServedByStations> getAreasContained();
}