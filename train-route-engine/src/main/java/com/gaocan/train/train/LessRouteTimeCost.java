package com.gaocan.train.train;

import java.util.Comparator;

import org.jpenguin.graph.Path;

import com.gaocan.publictransportation.TripInterval;

public class LessRouteTimeCost implements Comparator<Path>   {
	private  static final double SRC_STATION_SHIFA_DISCOUNT = 180;
      public int compare(Path path1, Path path2)   {
	      TrainTrip  trip1 = new TrainTrip(path1);
	      TrainTrip  trip2 = new TrainTrip(path2);
	      double cost1 = path1.getCost();
	      double cost2 =  path2.getCost();
	      // enforce preferring shi fa zhan in intervals, we had done this on tranfers but not the first interval
	      if (trip1.isSrcStationFirstInLine() == false ) {
	    	  // make sure the station's city is not the first city in line, otherwise we could just use 重庆 if  重庆 ->  重庆北 -> even if 	重庆北 is not literally first station
	     	Station firstStation = (Station) trip1.getFirstStation();
	     	if (false == firstStation.getCityServed().getStations().contains(trip1.getIntervals().get(0).getStartLsp().getLine().getBeginStation())) {
	     		cost1 +=  SRC_STATION_SHIFA_DISCOUNT;
	     	}
	      }
	      if (trip2.isSrcStationFirstInLine() == false) {
	    	  // make sure the station's city is not the first city in line, otherwise we could just use 重庆 if  重庆 ->  重庆北 -> even if 	重庆北 is not literally first station
		     Station firstStation = (Station) trip2.getFirstStation();
		     if (false == firstStation.getCityServed().getStations().contains(trip2.getIntervals().get(0).getStartLsp().getLine().getBeginStation())) {
		       	  cost2 += SRC_STATION_SHIFA_DISCOUNT;
		     }
	      }
	      if (cost1 > cost2)
	    	  return 1;
	      if (cost1 == cost2) {
	    	  return 0;
	      }
	      return -1;
   } 

}



