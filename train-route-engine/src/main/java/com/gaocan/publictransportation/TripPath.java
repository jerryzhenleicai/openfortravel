package com.gaocan.publictransportation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * for route planning, convert planned path from a list of LSPs to a smaller
 * list of trip intervals (no 2 intervals are on same line).
 */
public abstract class TripPath implements Serializable {
    /** see Interval below */

    public Vector<TripInterval> intervals;

    transient org.jpenguin.graph.Path graphPath;

    // transfer from Beijing Xi to Beijing
    int numIntraAreaTransfers;

    private boolean endLspReached;

    public Station getFirstStation() {
        return intervals.get(0).getStartLsp().getStation();
    }

    protected TripPath(org.jpenguin.graph.Path path) {
        setIntervals(new Vector());
        numIntraAreaTransfers = 0;
        graphPath = path;
        calcTripIntervals();
    }

    /**
     * Get number of stations on path
     * 
     * @return
     */
    public int getNumberOfStops() {
        return graphPath.getLength();
    }

    public String toString() {
        return getIntervals().toString();
    }

    public List<Station> getTransferStations() {
        Vector intvs = getIntervals();
        List<Station> result = new ArrayList<Station>();
        for (int i = 0; i < intvs.size() - 1; i++) {
            TripInterval interval = (TripInterval) intvs.get(i);
            result.add(interval.getEndLsp().getStation());
        }
        return result;
    }

    public int getNumTransfers() {
        // need to exclude same city tranf because it causes two extra intervals
        // compared to 1 caused by same station transfer
        return getIntervals().size() - 1 - numIntraAreaTransfers;
    }

    /**
     * get real time elapsed for trip
     */
    public int getTripTimeInMinutes() {
        int mins = 0;
        TripInterval interval;
        TripInterval prev_interval = null;

        // real time elapsed, note interval's time is from depart to arrival,
        // the transfer time
        // is not included
        for (int i = 0; i < getIntervals().size(); i++) {
            interval = (TripInterval) getIntervals().get(i);

            // if traveler is transfering within the same station to another
            // line instead of going to another station (walk etc.) to transfer
            // (IntraArea tansfer),
            // then we need to add the wait time spent for transfer. Note
            // intra-area transfer itself is counted as a special interval
            if ((prev_interval != null) && !interval.isIntraAreaTransfer() && !prev_interval.isIntraAreaTransfer()) {
                // note interval's time is from depart to arrival, here include
                // the transfer time
                mins += prev_interval.getEndLsp().getMinutesToTransferToNextLsp(interval.getStartLsp());
            }
            mins += interval.getTimeInMinutes();
            prev_interval = interval;
        }
        return mins;
    }

    /* return x hours y minutes */
    public String getTripTimeString() {
        int min = getTripTimeInMinutes();
        int hour = min / 60;
        min = min - hour * 60;
        return hour + "小时" + min + "分";
    }

    /**
     * calc cost in RMB
     */
    public abstract int getMoneytaryCostInCents();

    /**
     * Given graph path, calc intervals
     * 
     */
    private void calcTripIntervals() {
        LineStationPair lsp;
        LineStationPair curr_start_lsp;
        LineStationPair prev_lsp;
        Line curr_line;
        TripInterval interval;
        int n_lsps;
        int time = 0;

        n_lsps = graphPath.getLength() + 1;

        // find lines
        Iterator it = graphPath.getNodes();
        lsp = (LineStationPair) it.next();
        curr_line = lsp.line;
        curr_start_lsp = lsp;

        // loop through each lsp on the path to group travel on different lines
        // into intervals
        while (it.hasNext()) {
            prev_lsp = lsp;
            
            // when it is 双向线路, start lsp, end lsp is same one, so no need read again
            if (endLspReached == false) {
                lsp = (LineStationPair) it.next();
            }

            // lsp is not start of a new line
            if (isSameLine(lsp, curr_line)) {
                // increment trip time
                time += prev_lsp.getMinutesToTravelToNextLspOnSameLine(lsp);
                if (time < 0) {
                	System.err.println(prev_lsp.toString() + " -> " +  lsp.toString());
                	throw new RuntimeException("Negative trip time");
                }
                // if prev lsp is not start station of interval, then needs time
                // of train staying on that station
                if (curr_start_lsp != prev_lsp) {
                    time += prev_lsp.getVehicleStayTimeMinutes();
                }

                // if lsp is the last one on the path, then a new interval is
                // needed
                if (it.hasNext() == false) {
                    interval = new TripInterval(curr_line, curr_start_lsp, lsp, time);
                    intervals.add(interval);
                }
            }
            // this lsp is start of a new line ?
            else {
                // if prev station is NOT the first station of our current
                // interval,
                // then this lsp concludes the travel on the OLD line.
                // O.W. could be a station transfer
                if (prev_lsp != curr_start_lsp) {
                    // time is curr accumulated interval travel time
                    // this interval concludes the travel on the OLD line
                    // prev_lsp is end of a line
                    interval = new TripInterval(curr_line, curr_start_lsp, prev_lsp, time);
                    intervals.add(interval);
                }

                // It is a intra-area transfer (like from Beijing West to
                // Beijing)
                // then need to treat this transfer as an interval
                if (lsp.getStation() != prev_lsp.getStation()) {
                    interval = new TripInterval(curr_line, prev_lsp, lsp, prev_lsp.getMinutesToTransferToNextLsp(lsp));
                    // a special interval
                    interval.setIntraAreaTransfer(true);
                    numIntraAreaTransfers++;
                    intervals.add(interval);
                }

                // this lsp is start of a new interval
                curr_start_lsp = lsp;
                curr_line = lsp.line;

                // reset interval time counter
                time = 0;
            }
        }
    }

    private boolean isSameLine(LineStationPair lsp, Line curr_line) {
        if (lsp.line == curr_line) {
            // if 环线, check lsp is end lsp of current line
            if (curr_line.getBeginStation().equals(curr_line.getEndStation())) {
                if (endLspReached == false) {
                    endLspReached = (lsp == curr_line.getEndLsp());
                } else { // enter current line twice
                    endLspReached = false;
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    public void setIntervals(Vector intervals) {
        this.intervals = intervals;
    }

    public Vector<TripInterval> getIntervals() {
        return intervals;
    }

    public HourMinTime getTripStartTime() {
        return intervals.get(0).getStartLsp().getDepartureTime();
    }

    public HourMinTime getTripEndTime() {
        return intervals.get(intervals.size() - 1).getEndLsp().getArrivalTime();
    }

    /**
     * is source station shi fa zhan in line?
     * 
     * @return
     */
    public boolean isSrcStationFirstInLine() {
        TripInterval firstLeg = intervals.get(0);
        return firstLeg.getLine().getBeginStation().equals(firstLeg.getStartLsp().getStation());
    }
}
