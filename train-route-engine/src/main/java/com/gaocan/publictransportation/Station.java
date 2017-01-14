package com.gaocan.publictransportation;

import java.util.ArrayList;
import java.util.Iterator;


public abstract class Station
    extends com.gaocan.publictransportation.GeoStation {
    protected transient ArrayList<LineStationPair> lineStationPairs = new ArrayList<LineStationPair>(16); // capacity and increment
    private transient ArrayList<AreaServedByStations> areasServed = new ArrayList<AreaServedByStations>(4); // capacity and increment
    
    public Station(String name) {
    	super(name);
    }
    
    public Station() {
    }
 
    
    public Iterator<AreaServedByStations> getAreasServed() {
        return areasServed.iterator();
    }

    
    private void addAreaServed(AreaServedByStations area) {
        areasServed.add(area);
    }
    
    
    public void associateWithArea (AreaServedByStations area) {
        this.addAreaServed(area);
        area.addStation(this);
    }
    
    /**
     * determine if an area is served by this station
     * @param area
     * @return
     */
    
    public abstract boolean isAreaServedByStation(AreaServedByStations area);
    
    /**
     * Given this station create a new area with the same name 
     */
    
    public abstract AreaServedByStations createAreaFromStation();
    
    /**
     * associate a line that passes this station
     */
    
    public void addLineStationPair(LineStationPair lsp) {
        lineStationPairs.add(lsp);
    }

    
    public void removeLineStationPair(LineStationPair lsp) {
        lineStationPairs.remove(lsp);
    }

    @SuppressWarnings("unchecked")
    
    public Iterator<LineStationPair> getLineStationIterator() {
        return lineStationPairs.iterator();
    }

    /**
     *
     * @return how many lines pass this station
     */
    
    public int getNumLinesPassing() {
        return lineStationPairs.size();
    }
}
