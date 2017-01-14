/*
 * Created on Oct 13, 2003
 * Copyright (c) Gaocan Inc., Oak Hill, VA, USA, All rights reserverd.
 */
package com.gaocan.publictransportation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * trip between two stations where there's no transfer, like an edge in a graph
 * 
 * @author zcai
 * 
 */
public class TripInterval implements Serializable {
    private static final long serialVersionUID = 7780619900109322755L;

    public Line line;

    public LineStationPair startLsp;

    public LineStationPair endLsp;

    public int timeInMinutes;

    public boolean intraAreaTransfer = false;

    protected TripInterval(Line l, LineStationPair start, LineStationPair end, int min) {
        line = l;
        startLsp = start;
        endLsp = end;
        timeInMinutes = min;
    }

    /**
     * Return how many stations are between begin and end (include end exclude
     * begin)
     * 
     * @return
     */
    public int getNumberOfStops() {
        int result = Math.abs(line.getEndLspIndex(endLsp) - line.getLspIndex(startLsp)) + 1;
        return result;
    }

    protected TripInterval() {
        setIntraAreaTransfer(false);
    }

    public String toString() {
        if (isIntraAreaTransfer()) {
            return new String("IntraArea");
        } else {
            return line.toString() + getStartLsp().getStation().getName() + getEndLsp().getStation().getName();
        }
    }

    protected void setStartLsp(LineStationPair startLsp) {
        this.startLsp = startLsp;
    }

    public Line getLine() {
        return startLsp.getLine();
    }

    public boolean containsStation(Station station) {
        Line line = this.getLine();
        for (int i = 0; i < line.lineStationPairs.size(); i++) {
            if (station == ((LineStationPair) line.lineStationPairs.get(i)).getStation()) {
                return true;
            }
        }
        return false;
    }

    public LineStationPair getStartLsp() {
        return startLsp;
    }

    protected void setEndLsp(LineStationPair endLsp) {
        this.endLsp = endLsp;
    }

    public LineStationPair getEndLsp() {
        return endLsp;
    }

    public int getTimeInMinutes() {
        return timeInMinutes;
    }

    protected void setIntraAreaTransfer(boolean intraAreaTransfer) {
        this.intraAreaTransfer = intraAreaTransfer;
    }

    public boolean isIntraAreaTransfer() {
        return intraAreaTransfer;
    }

    public List<LineStationPair> getPassedLsp() {
        List<LineStationPair> passedLsp = new ArrayList<LineStationPair>();

        Line line = getLine();

        int start = line.getLspIndex(getStartLsp());
        int end = line.getEndLspIndex(getEndLsp());

        // for Bidirectional bus line
        if (start == end) {
            passedLsp.add(line.getLsp(start));
        } else if (start < end) {
            while (start <= end) {
                passedLsp.add(line.getLsp(start));
                start++;
            }
        } else {
            while (start >= end) {
                passedLsp.add(line.getLsp(start));
                start--;
            }
        }

        return passedLsp;
    }
}
