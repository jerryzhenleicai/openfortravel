package com.gaocan.train.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gaocan.publictransportation.Line;
import com.gaocan.publictransportation.LineStationPair;
import com.gaocan.publictransportation.Schedule;
import com.gaocan.train.train.Station;
import com.gaocan.train.train.TrainLine;
import com.gaocan.train.train.TrainLineStationPair;
import com.gaocan.train.train.TrainSchedule;
import com.gaocan.train.train.TrainScheduleFactory;

public class ScheduleCleanupStationDistance {
		public static void main(String[] args) {
			try {
			TrainScheduleFactory sf = new TrainScheduleFactory();
			Schedule sched = new TrainSchedule("200310");
			// read the schedule data
			sf.buildSchedule(sched, args[0]);
			// each station has a list of adjacent stations along with distance to them 
			HashMap<Station, HashMap<Station, Integer>> adjDistMaps = new HashMap<Station, HashMap<Station, Integer>>();
			for (com.gaocan.publictransportation.Station s : sched.getStations()) {
				Station st = (Station) s;
				adjDistMaps.put(st, new HashMap<Station,Integer>());
			}
			List<TrainLine> wrongLines = new ArrayList<TrainLine>();
			// scan lines and build pairwise station distance map
			for (Line l : sched.getLines() ) {
				if (((TrainLine) l).getFullLengthKm() == -1) {
					// this line dist is estimated, needs to be fixed later
					wrongLines.add((TrainLine) l);
					continue;
				}
				TrainLineStationPair  prevLsp = null;
				for (LineStationPair lsp : l.getLineStationPairs()) {
					TrainLineStationPair tlsp = (TrainLineStationPair ) lsp;
					if (prevLsp  != null) {
						// update adj distance
						Station currStation = (Station) tlsp.getStation();
						Station prevStation = (Station) prevLsp.getStation();
						HashMap<Station, Integer> adjMap = adjDistMaps.get(prevStation);
						Integer prevKm = adjMap.get(currStation);
						int dist = tlsp.getKmsFromStart() - prevLsp.getKmsFromStart();
						if (prevKm == null) {
							adjMap.put(currStation,  new Integer(dist));
						} else {
							// average it
							adjMap.put(currStation,  new Integer ( (dist + prevKm.intValue()) / 2));
						}
					}
					prevLsp  = tlsp;
				}
			}
			
			// now fix the wrong lines;
			for (TrainLine l : wrongLines ) {
				TrainLineStationPair  prevLsp = null;
				TrainLineStationPair  tlsp = null;
				for (LineStationPair lsp : l.getLineStationPairs()) {
					tlsp = (TrainLineStationPair ) lsp;
					if (prevLsp  == null) {
						tlsp.setKmsFromStart(0);
					} else {
						// update adj distance
						Station currStation = (Station) tlsp.getStation();
						Station prevStation = (Station) prevLsp.getStation();
						HashMap<Station, Integer> adjMap = adjDistMaps.get(prevStation);
						Integer km = adjMap.get(currStation);			
						if (km == null) {
							// 				System.err.println("Unable to determine distance from " + prevStation.getName() + " to " + currStation.getName());
							km = new Integer(30);
						}
						tlsp.setKmsFromStart(prevLsp.getKmsFromStart() + km.intValue());
					}
					prevLsp  = tlsp;
				}
				l.setFullLengthKm(tlsp.getKmsFromStart());
			}
			
			System.out.println("writing distance fixed sched to " + args[1]);
			sched.writeToFile(args[1]); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		}
}
