package com.gaocan.train.train;

import com.gaocan.publictransportation.AreaServedByStations;

import java.util.Iterator;

/**
 * A big city (general area) may have several train stations, like Beijing,
 * Beijing West. However unlike a bus stop which may serve multiple points of
 * interests, a station belongs to exactly one city.
 */
public class Station extends com.gaocan.publictransportation.Station {
    public Station(String name) {
        super(name);
    }

    /**
     * if in the same area as another station, then return that area, if not
     * return null
     */
    public boolean isAreaServedByStation(AreaServedByStations area) {
        if (!(area instanceof City)) {
            throw new IllegalArgumentException("area must be city for train station");
        }
        String cityName = area.getName();
        // station same name as area?
        if (cityName.equals(name))
            return true;


        // BeijingXi serves Beijing, but DingNan cannot be a station in city
        // Ding
        if (name.length() <= 2 || name.length() <= cityName.length()) {
            return false;
        }

        // Sometimes X and Xwest are not really in the same area, they are false
        // same city stations
        if (Settings.falseCitySecondaryStations.contains(name)) {
            return false;
        }

        if (cityName.length() == (name.length() - 1)) {
            if (name.startsWith(cityName)) {
                if (name.endsWith("东") || name.endsWith("南") || name.endsWith("西") || name.endsWith("北")) {
                    return true;
                }
            }
        }

        // specials
        if (cityName.equals("上海") ){
            return name.equals("上海虹桥");
        }

        if (cityName.equals("武汉") ){
            return name.equals("武汉") || name.equals("武昌") || name.equals("汉口");
        }
        
        if (cityName.equals("厦门") ){
        	return name.equals("厦门高崎");
        }

        return false;
    }

    public AreaServedByStations createAreaFromStation() {
        return new City(name);
    }

    public City getCityServed() {
        Iterator it = getAreasServed();
        return (City) it.next();
    }
}
