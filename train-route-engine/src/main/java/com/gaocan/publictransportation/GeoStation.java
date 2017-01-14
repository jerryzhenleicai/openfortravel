package com.gaocan.publictransportation;




/**
 * A stop on a line, can be train stations or bus stops or airports. In general there's a multiple-multiple relationship
 *  between stations and general areas (e.g. bus stations vs. point of interests).
 */
public abstract class GeoStation implements Comparable<GeoStation> {
    private long id;
    protected String name;
	
    
    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }
    
    public int hashCode() {
        return name.hashCode();
    }

	public int compareTo(GeoStation o) {
    	if (o.getClass() != this.getClass()) {
    		throw new IllegalStateException("Must be same type of station : " + this + " , " + o);
    	}
		return name.compareTo(((GeoStation)o).getName());
	}
	
    /**
     * Stations are considered equal if same name
     */
    public boolean equals(Object s) {
    	if (s.getClass() != this.getClass()) {
    		throw new IllegalStateException("Must be same type of station : " + this + " , " + s);
    	}
        return name.equals(((GeoStation) s).name);
    }

    public String toString() {
        return name;
    }

    public void setName(String n) {
        name = n;
    }

    public String getName() {
        return name;
    }

	public GeoStation() {
		name = null;
	}
	
    public GeoStation(String n) {
        this();
        // no | in name
       n = n.replaceAll("|", "");
        setName(n);
    }
	
}
