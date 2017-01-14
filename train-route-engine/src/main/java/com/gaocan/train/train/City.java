package com.gaocan.train.train;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.gaocan.publictransportation.AreaServedByStations;
import com.gaocan.publictransportation.Station;
import com.gaocan.publictransportation.LineStationPair;

public class City implements AreaServedByStations {
    //String gisCity;
    Vector<Station> stations = new Vector<Station>();
    String name;

    public City(String n) {
        name = n;
    }

    public Collection<Station> getStations() {
    	return stations;
    }
    
    public int getNumLinesPassing() {
        // ### this is wrong, some line may pass both stations of a city and should not
        // be counted twice    
        int n = 0;
        Iterator<Station> stit = stations.iterator();

        while (stit.hasNext()) {
            n += stit.next().getNumLinesPassing();
        }

        return n;
    }
    
    /**
     * more lines bigger
     * @param o
     * @return
     */
    public int compareTo(AreaServedByStations other) {
    	return this.getNumLinesPassing() - other.getNumLinesPassing();
    }
    
	public int hashCode() {
		return name.hashCode();
	}
	
	public boolean equals(Object o) {
		City m = (City) o;
		return name.equals(m.name);
	}
    
    public void setName(String n) {
        name = n;
    }

    public String getName() {
        return name;
    }

    /** another station serving this area */
    public void addStation(Station station) {
        stations.add(station);
    }

    public Iterator getStationsIterator() {
        return stations.iterator();
    }

    /**
         * Number of stations serving this area
         * @return
         */
    public int getNumStations() {
        return stations.size();
    }

    /**
         * Are two areas close?
         * @param neighbor
         * @return
         */
    public boolean isAdjacentTo(AreaServedByStations neighbor) {
        return false;
    }

    /**
         * examples are districts contained in a city, parks in a district etc.
         * @return
         */
    public Iterator<AreaServedByStations> getAreasContained() {
        return null;
    }

    public Iterator<LineStationPair> getLineStationIterator() {
        List<LineStationPair> lsps = new ArrayList<LineStationPair>();
        for (Station station : stations) {
            Iterator<LineStationPair> lspIt = station.getLineStationIterator();
            while (lspIt.hasNext()) {
                lsps.add(lspIt.next());
            }
        }
        
        return lsps.iterator();
    }
}
