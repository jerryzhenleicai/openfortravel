package com.gaocan.train.train;

import com.gaocan.publictransportation.HourMinTime;
import com.gaocan.publictransportation.InvalidScheduleDataException;
import com.gaocan.publictransportation.LineStationPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Copyright (c) 2003 Gaocan, Inc., All Rights Reserved
 * Represent the moment a line stops at one given station.
 *
 */
public class TrainLineStationPair extends LineStationPair implements Comparable {
	private static Logger logger = LoggerFactory.getLogger(TrainLineStationPair.class);
    /** transfer need some time */
    final static double MIN_TRANSFER_TIME_ACCEPTATBLE = 5;
    final static double MAX_TRANSFER_TIME_ACCEPTATBLE = 12 * 60;

    /**  penalty for same station transferring, in minutes */
    private static final double sameStationTransferPenalty = 900.0;

    /**  penalty for intra-city transferring, in minutes */
    private static final double intraCityTransferPenalty = 2400.0;

    /**  encourage transferring to a station which is the starting station of the new line */
    private final static double TRANSFER_START_DISCOUNT = 0.7;

    /**
     * minimum time needed for transferring within the same GA from one station to another,
     * like Beijing West to Beijing or from Zoo to Zoo West , in minutes
     */
    private final static double minimumIntraCityTransferTime = 120.0;
    private int kmsFromStart;

    public TrainLineStationPair(Station s, TrainLine l, HourMinTime arrival_t, HourMinTime departure_t, int kms)
        throws InvalidScheduleDataException {
        super(s, l);
        setDepartureTime(departure_t);
        setArrivalTime(arrival_t);
        setKmsFromStart(kms);
    }

    public int compareTo(Object o) {
    	TrainLineStationPair lsp2 = (TrainLineStationPair) o;
    	if (lsp2 == this) {
    		return 0;
    	}
    	TrainLine line1 = (TrainLine) this.getLine();
    	TrainLine line2 = (TrainLine) lsp2.getLine();
    	int res = line1.compareTo(line2);
    	if (res != 0 ) {
    		return res;
    	}
    	res = getStation().compareTo(lsp2.getStation());
    	if (res == 0) {
        res = kmsFromStart - lsp2.kmsFromStart;
        if (res == 0) {
          logger.warn("compareTo: LSP " + this.toString() + " is equal to LSP " + lsp2.toString());
        }
    	}
    	return res;
    }
    /**
     * @param nextLsp
     * @return from departing this lsp to arrival at next lsp
     * @throws IllegalArgumentException
     */
    public int getMinutesToTravelToNextLspOnSameLine(LineStationPair nextLsp)
        throws IllegalArgumentException {
        TrainLineStationPair nlsp = (TrainLineStationPair) nextLsp;

        if (!this.getDepartureTime().isEarlierThan(nlsp.getArrivalTime()) || (nlsp.getLine() != getLine())) {
            throw new IllegalArgumentException("the given lsp not possible to follow this lsp on a path, given:"
            		+ nlsp.toString() + " this : " + this.toString());
        }
        // make sure nlsp is the next station on the line
        if (getLine().areConsecutiveStations(getStation(), nlsp.getStation())) {
        	int res = getDepartureTime().minutesEarlierThan(nlsp.getArrivalTime());
        	if (res < 0) {
        		throw new RuntimeException("negative travel time " + res + ", this:" + toString() + " -> " +  nlsp.toString());
        	}
        	return res;
        } else {
            throw new IllegalArgumentException("the given same-line lsp not following this lsp ");
        }
    }

    /**
     * same station or intra-area transfer minutes, from arrival on this to departing from the next lsp
     */
    public int getMinutesToTransferToNextLsp(LineStationPair nextLsp)
        throws IllegalArgumentException {
        TrainLineStationPair nlsp = (TrainLineStationPair) nextLsp;

		// allow time for transfer
		if (!getArrivalTime().isEarlierThan(nlsp.getDepartureTime()) || (nlsp.getLine() == getLine())) {
            throw new IllegalArgumentException("the given lsp not possible a transfer for this lsp on a path: " + 
               toString() + ", "  + nlsp.toString());
        }

		// this lsp cannot be the start lsp of a line (why get on a line and then get off to get on another?)
		if (line.getBeginStation() == getStation()) {
			throw new IllegalArgumentException("cannot transfer from start lsp of a line to another line");
		}

        //  two stations must be either the same on or in same city for transfering
        City city = (City) getStation().getAreasServed().next();

        // verify they share same city
        if (city != nlsp.getStation().getAreasServed().next()) {
            throw new IllegalArgumentException("two lsps cannot be transfering if not in same city: " + this.toString() + ", " + nlsp.toString());
        }

		
        return getArrivalTime().minutesEarlierThan(nlsp.getDepartureTime());
    }

    /**
     * used in planning graph to compute short path, could be  running time if two lsp same line or transfer time in minutes
     * if two lsp same station or area.
     * @param nextLsp the next lsp in the path adjacent to this one.
     * @return cost to get nextLsp, 
     * @throws IllegalArgumentException if nextLsp cannot be possibly the next node on a path
     */
    public double getCostToNextLspOnPath(LineStationPair nextLsp)
        throws IllegalArgumentException {
        TrainLineStationPair nlsp = (TrainLineStationPair) nextLsp;

        // if two lsps are two adjacent station of the same line, then edge cost is running time in minutes
        if (nlsp.getLine() == getLine()) {
            // make sure nlsp is the next station on the line
            if (getLine().areConsecutiveStations(getStation(), nlsp.getStation())) {
                // transfer time more than 12 hours not allowed
                if (getArrivalTime().isEarlierThan(nlsp.getArrivalTime()) == false) {
                    throw new IllegalArgumentException("the given lsp not possible to follow this lsp on a line, lsp :" + toString() + " , " +
                        nlsp.toString());
                }
                // note we use arrival times of both stations for calculation
                return getArrivalTime().minutesEarlierThan(nlsp.getArrivalTime());
            } else {
                throw new IllegalArgumentException("the given same-line lsp not following this lsp " + toString() + " , " +
                    nlsp.toString());
            }
        } else {
			// this lsp cannot be the start lsp of a line (why get on a line and then get off to get on another?)
			if (line.getBeginStation() == getStation()) {
				throw new IllegalArgumentException("cannot transfer from start lsp of a line to another line");
			}
			
            //  two stations not on the same line, then they must be in same city for transfering
            City city = (City) getStation().getAreasServed().next();

            // verify they share same city
            if (city != nlsp.getStation().getAreasServed().next()) {
                throw new IllegalArgumentException("two lsps cannot be on path if neither same line now same city ");
            }

            // transfer time more than 12 hours not allowed
            if (getArrivalTime().isEarlierThan(nlsp.getDepartureTime()) == false) {
                throw new IllegalArgumentException("the given lsp not possible to follow this lsp as a transfer, lsp :" + toString() + " , " +
                    nlsp.toString());
            }

            
            double transferTime = getArrivalTime().minutesEarlierThan(nlsp.getDepartureTime());

            // not too much for transfer ?
            if ((transferTime > MAX_TRANSFER_TIME_ACCEPTATBLE) || (transferTime < MIN_TRANSFER_TIME_ACCEPTATBLE)) {
				throw new IllegalArgumentException("transfer time too large or too small");
            }

            double transferPenalty = 0.;

            //  same station transfer?
            if (getStation() == nlsp.getStation()) {
                // discourage transfering at a small station, this way those small cities appear
                // in multiple provinces won't be chosen
                // Beijing has 300+ passing lines, Shanghai has 190
                transferPenalty = sameStationTransferPenalty +   sameStationTransferPenalty / Math.sqrt(Math.sqrt((double) city.getNumLinesPassing()));
            } else {
                // intra-city transfer? (e.g. Beijing to BeijingWest)
                // make sure there's enough time for in-city transportation
                if (transferTime < minimumIntraCityTransferTime) {
					throw new IllegalArgumentException("intra city transfer time too small");
                }
                // add extra penalty for intra-area as it requires in-area travel
                transferPenalty = (20 * intraCityTransferPenalty) / Math.sqrt((double) city.getNumLinesPassing());
            }

            // if transfer station is the begin station of line2, then much
            // favor it by reducing penalty
            if (nlsp.getStation() == nlsp.getLine().getBeginStation()) {
                transferPenalty *= TRANSFER_START_DISCOUNT;
            } else {
                // else transfer time should be diff. between arrivals because
                // the time needed from transfer station to its next station is
                // calced based on arrrival interval
                transferTime = getArrivalTime().minutesEarlierThan(nlsp.getArrivalTime());
            }

            // discourage tansfers by adding penalty
            return transferTime + transferPenalty;
        }
    }
	public int getVehicleStayTimeMinutes() {
        return getArrivalTime().minutesEarlierThan(getDepartureTime());
    }

    public void setKmsFromStart(int kmsFromStart) {
        this.kmsFromStart = kmsFromStart;
    }

    public int getKmsFromStart() {
        return kmsFromStart;
    }

    public String toString() {
        return "[" + line.getFullNumber() + "," + station.getName() + "," +
        		this.arrivalTime + "," + this.departureTime + "," + this.kmsFromStart +  "km]";
    }
    
    public String getExtraDataString() {
		return arrivalTime + "," + departureTime + "," + kmsFromStart;
	}
	
}
