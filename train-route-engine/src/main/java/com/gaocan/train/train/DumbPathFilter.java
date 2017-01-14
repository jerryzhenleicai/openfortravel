/*
 * Created on Oct 24, 2003
 * Copyright (c) Gaocan Inc., Oak Hill, VA, USA, All rights reserverd.
 */
package com.gaocan.train.train;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jpenguin.graph.Path;

import com.gaocan.publictransportation.TripInterval;


/**
 * @author zcai
 *
 */
public class DumbPathFilter {
    /**
     * Remove absurd paths, like K55 BJ-Xi'an, then path K55 (BJ-ZZ) then 535 (ZZ-XA) is dumb
     * @param graph_paths must be already sorted on cost, so we know if two paths conflict, we drop the second one
     * @return a List of TrainTrips
     */
    public static List<TrainTrip> filterOutDumbPaths(List graph_paths) {
        ArrayList routesFound = new ArrayList();
        TrainTrip currRoute;
        TrainTrip prevRoute = null;
        TripInterval interval1;
        TripInterval interval2;
        int i;
        int j;
        int route1IntervalNo;
        int route2IntervalNo;
        boolean overlap_interval_found;
        boolean newRouteIsDumb;

        for (i = 0; i < graph_paths.size(); i++) {
            // local check first
            if (isGraphPathDumb((Path) graph_paths.get(i))) {
                continue;
            }

            // now check prev paths against this, first create current trip path
            currRoute = new TrainTrip((Path) graph_paths.get(i));
            newRouteIsDumb = false;

            // make sure this path is not subsumed by an already found path
            // by subsume we mean interval1 achieve the same travel purpose
            // of (interval2+some other intervals follow interval2)
            for (j = 0; (newRouteIsDumb == false) && (j < routesFound.size()); j++) {
                prevRoute = (TrainTrip) routesFound.get(j);

                // scan the two paths intervals, find the first two intervals that has same train line
                for (route1IntervalNo = 0; (newRouteIsDumb == false) && (route1IntervalNo < prevRoute.getIntervals().size()); route1IntervalNo++) {
                    interval1 = (TripInterval) prevRoute.getIntervals().get(route1IntervalNo);

                    if (interval1.isIntraAreaTransfer()) {
                        continue;
                    }
                    overlap_interval_found = false;
                    // check if interval1's line appear in any path2's interval (overlap)
                    for (route2IntervalNo = 0; (newRouteIsDumb == false) && (route2IntervalNo < currRoute.getIntervals().size()); route2IntervalNo++) {
                        interval2 = (TripInterval) currRoute.getIntervals().get(route2IntervalNo);
                        if (interval2.isIntraAreaTransfer()) {
                            continue;
                        }

                        // the two intervals of the two paths are on the same line
                        if (interval2.getLine() == interval1.getLine()) {
                            // if interval2 is not completely contained by interval1 i.e. its begin station is in front of interval1's on train line,
                            // or its  end station is behind interval1's end station,  then there's no subsumption
                            if (interval1.getLine().isStationAfter(interval1.getStartLsp().getStation(), interval2.getStartLsp().getStation())) {
                            	// no subsumption
                                break;
                            }
                            if (interval1.getLine().isStationAfter(interval2.getEndLsp().getStation(), interval1.getEndLsp().getStation())) {
                                break;
                            }
                        	// or the two intervals are identical (in which case should not be considered subsumption)
                            if (interval1.getStartLsp() == interval2.getStartLsp() && interval1.getEndLsp() == interval2.getEndLsp()) {
                            	break;
                            }
                            // record the fact that interval1 contains interval2l 
                            overlap_interval_found = true;
                            
                            // if interval2 is the last leg in trip, then its previous leg also starts from the start station (S) of interval1, then route is dumb 
                            // because instead of going from S to dest city  using the two legs of this trip , we can just take interval1 of prev trip.
                            if (route2IntervalNo == currRoute.getIntervals().size() -1 && route2IntervalNo > 0) {
                            	TripInterval prevIntv = (TripInterval) currRoute.getIntervals().get(route2IntervalNo - 1);
                            	if (prevIntv.getStartLsp().getStation().equals(interval1.getStartLsp().getStation())) {
                           			 newRouteIsDumb = true;
                            	}
                            }  
                        }
                        // two intervals not having same line 
                        else {
                            // check if interval1 subsume interval2+...
                            if (overlap_interval_found) {
                                // if interval2's end station (ES) appear inside interval1, then path is dumb
                                // because taking path1 via interval1 alone can reach ES, no need to
                                // use two intervals as in path2
                                if (interval1.containsStation(interval2.getEndLsp().getStation()) ||
                                  		// or if interval2 actually reaches our dest area, then this check should be if interva1 also reaches dest area
                                		(route2IntervalNo == currRoute.getIntervals().size() -1 && route1IntervalNo ==  prevRoute.getIntervals().size() - 1) 
                                ) {
                                    newRouteIsDumb = true;
                                    //System.out.println("path 2 removed because interval1 : " + interval1.toString() + " " +
                                    //  "		subsumes it :" + interval2);
                                }
                            }
                        }
                    }
                }
            }

           if (!newRouteIsDumb) 
            {
                routesFound.add(currRoute);
            }
        }
        return routesFound;
    }

    /**
     * simple check to see if path is invalid
     * @return
     */
    private static boolean isGraphPathDumb(Path graphPath) {
        /** do not allow three consecutive lsps in same city, that means transfer from city A to A to A */
        int n_lsps = graphPath.getLength() + 1;
		if (n_lsps < 3)
			return false;
		Iterator lspIt = graphPath.getNodes();
        TrainLineStationPair prevLsp = null;
		TrainLineStationPair thisLsp = (TrainLineStationPair) lspIt.next();
		TrainLineStationPair nextLsp = (TrainLineStationPair) lspIt.next(); 

        while (lspIt.hasNext()) {
            prevLsp = thisLsp;
            thisLsp =  nextLsp;
            nextLsp = (TrainLineStationPair) lspIt.next();
            // make sure we do not have two transfers in the same city in a row 
            if ((((Station) prevLsp.getStation()).getCityServed() == ((Station) thisLsp.getStation()).getCityServed()) &&
                    (prevLsp.getLine() != thisLsp.getLine()) &&
                    (((Station) prevLsp.getStation()).getCityServed() == ((Station) nextLsp.getStation()).getCityServed()) &&
                    (nextLsp.getLine() != thisLsp.getLine())) {
                return true;
            }
        }
        return false;
    }
}
