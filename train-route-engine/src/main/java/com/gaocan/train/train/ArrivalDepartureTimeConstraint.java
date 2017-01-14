/*
 * Created on Oct 9, 2003
 * Copyright (c) Gaocan Inc., Oak Hill, VA, USA, All rights reserverd.
 */
package com.gaocan.train.train;

import com.gaocan.publictransportation.*;


/**
 * @author zcai
 *
 */
public class ArrivalDepartureTimeConstraint implements SrcDestLspConstraint {
    boolean arrivalTimeConstrained = true;
    int earliestArriveHour;
    int latestArriveHour;
    boolean departTimeConstrained = true;
    int earliestDepartHour;
    int latestDepartHour;

    public ArrivalDepartureTimeConstraint(boolean _arrivalTimeConstrained, boolean _departTimeConstrained, int _earliestArriveHour,
        int _latestArriveHour, int _earliestDepartHour, int _latestDepartHour) {
        arrivalTimeConstrained = _arrivalTimeConstrained;
        departTimeConstrained = _departTimeConstrained;

        earliestArriveHour = _earliestArriveHour;
        latestArriveHour = _latestArriveHour;

        earliestDepartHour = _earliestDepartHour;
        latestDepartHour = _latestDepartHour;
    }

    /** this lsp can be the starting node on a path ? */
    public boolean isValidDepartLsp(LineStationPair plsp) {
        TrainLineStationPair lsp = (TrainLineStationPair) plsp;
        if (departTimeConstrained == false) {
            return true;
        } else {
            return (lsp.getDepartureTime().getHour() <= latestDepartHour) && (lsp.getDepartureTime().getHour() >= earliestDepartHour);
        }
    }

    /** this lsp can be the ending  node on a path ? */
    public boolean isValidArrivalLsp(LineStationPair plsp) {
        TrainLineStationPair lsp = (TrainLineStationPair) plsp;
        if (arrivalTimeConstrained == false) {
            return true;
        } else {
            return (lsp.getArrivalTime().getHour() <= latestArriveHour) && (lsp.getArrivalTime().getHour() >= earliestArriveHour);
        }
    }
}
