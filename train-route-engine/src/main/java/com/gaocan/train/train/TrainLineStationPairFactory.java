package com.gaocan.train.train;

import java.util.HashMap;

import com.gaocan.publictransportation.AbstractLineStationPairFactory;
import com.gaocan.publictransportation.HourMinTime;
import com.gaocan.publictransportation.InvalidHMTimeFormatException;
import com.gaocan.publictransportation.InvalidScheduleDataException;
import com.gaocan.publictransportation.Line;
import com.gaocan.publictransportation.LineStationPair;
import com.gaocan.publictransportation.Schedule;
import com.gaocan.train.train.TrainSchedule.AdjacentStationDistanceStat;


public class TrainLineStationPairFactory extends AbstractLineStationPairFactory {
    public LineStationPair createLsp(Schedule schedule, Line line, com.gaocan.publictransportation.Station station, String extraData)
        throws InvalidScheduleDataException {
        HourMinTime arrival_t;
        HourMinTime departure_t;
        int kms;
        assert(station != null);
        try {
            if (extraData == null || line == null) {
            	throw new IllegalArgumentException("null in lsp");
            }

            // Arrival , Depart , kms
            String[] fields = extraData.split(",");
            arrival_t = new HourMinTime(fields[0].trim());
            departure_t = new HourMinTime(fields[1].trim());
            kms = Integer.parseInt(fields[2].trim());            
            // if in a schedule context, record the distance relation between the stations
            if (schedule != null && line.getNumPassingStations() > 0) {
            	TrainLineStationPair prevLsp = (TrainLineStationPair) line.getEndLsp();
	            TrainSchedule sched = (TrainSchedule) schedule;
	        	HashMap<String, AdjacentStationDistanceStat> kmMap = sched.getStationDistanceMap();
	        	String key = prevLsp.getStation().getName() + "-" + station.getName();
	        	AdjacentStationDistanceStat stat = kmMap.get(key);
	        	int dist = kms - prevLsp.getKmsFromStart();
	        	if (stat == null) {
	        		stat = new AdjacentStationDistanceStat();
	        		stat.kmSum = dist;
	        		stat.numPairs = 1;
	        		kmMap.put(key, stat);
	        	} else {
	        		stat.kmSum += dist;
	        		stat.numPairs ++;
	        	}
            }
            TrainLineStationPair lsp = new TrainLineStationPair((Station) station, (TrainLine) line, arrival_t, departure_t, kms);
        	return lsp;
        } catch (InvalidHMTimeFormatException e) {
            throw new InvalidScheduleDataException("Invalid time: " + e.getMessage() , 
                    " at line " + line.getFullNumber() +". Did you forget a 'eol' line? ");
        } catch (Exception e) {
            throw new InvalidScheduleDataException("Error found : " + e.getMessage() ,
                    " at line " + line.getFullNumber() );
        }
    }

    /**
     * 
     */
    public boolean isLspInvalid(Line line, com.gaocan.publictransportation.Station station) {
        String val = (String) Settings.duplicateCities.get(((TrainLine) line).getNumber());
        if (val == null) {
            return false;
        }
        return val.equals(station.getName());
    }
    

	public boolean lspHasExtraData() {
		return true;
	 }
}
