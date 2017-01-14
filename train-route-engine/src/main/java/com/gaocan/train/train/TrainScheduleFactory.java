package com.gaocan.train.train;
import java.util.List;

import com.gaocan.publictransportation.DuplicateStationException;
import com.gaocan.publictransportation.Line;
import com.gaocan.publictransportation.Schedule;
import com.gaocan.publictransportation.ScheduleFactory;
import com.gaocan.publictransportation.Station;

public class TrainScheduleFactory extends ScheduleFactory {
	public TrainScheduleFactory() {
		super(new TrainStationFactory(), new TrainLineFactory(), new TrainLineStationPairFactory());
	}
	

    /**
     * is it possible for same station appear in one line twice
     * @return
     */
	@Override
    public boolean isDupStationInSameLine() {
        return true;
    }
    
	public void diffLines(Schedule sched1, Schedule sched2, List<String> onlyOneHas, List<String> onlyTwoHas) {
		assert(onlyOneHas.size() == 0);
		assert(onlyTwoHas.size() == 0);
	 	for (Line line : sched1.getLines()) {
	         // if a line is in one but not in two
	         Line line2 = sched2.getLineEquivalentTo(line);
	         if (line2 == null) {
	        	 onlyOneHas.add(line.getFullNumber());
	         }
	    }
	 	for (Line line : sched2.getLines()) {
	         // if a line is in one but not in two
	         Line line2 = sched1.getLineEquivalentTo(line);
	         if (line2 == null) {
	        	 onlyTwoHas.add(line.getFullNumber());
	         }
	     }
	}
    /**
     * Combine an older schedule (A) and a newer one (B) into a better schedule (C).  
     *  1. If a line is in B but not in A, add it to C, if a line is in both A and B, add B's version to C, 
     *  If a line is only in A,  still add it to C.
     * @param oldSchedule
     * @param newSched
     * @return schedule built
     */
    public Schedule mergeSchedule(Schedule oldSched, Schedule newSched) {
        // first insert each line in new sched to the output while consulting the old sched
        Schedule sched = new TrainSchedule("merged");

        // first merge the station and areas
        for (Station st1 : oldSched.getStations()) {
            try {
                sched.addStation(st1);
            } catch (DuplicateStationException e) {
            }
        }
        
        for (Station st2 : newSched.getStations()) {
            try {
                sched.addStation(st2);
            } catch (DuplicateStationException e) {
            }
        }
        try {
          	 for (Line newLine : newSched.getLines()) {
                sched.addLine(newLine);
            }

            // add all OLD lines back except  ones already in the final result
           for (Line oldLine : oldSched.getLines())  	{
                sched.addLine(oldLine);
           }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sched;
    }

    public boolean isLineEndOfSchedule(String line) {
        return false;
    }

}
