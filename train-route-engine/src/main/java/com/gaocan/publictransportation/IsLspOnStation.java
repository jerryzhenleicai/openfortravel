package com.gaocan.publictransportation;

import org.jpenguin.graph.UnaryPredicate;

/**
 * @author zcai
 *  
 */
public class IsLspOnStation implements UnaryPredicate {
	Station station;
	public IsLspOnStation(Station s) {
		station = s;
	}
	public boolean isTrueOn(Object lsp) {
		LineStationPair theLsp = (LineStationPair) lsp;
		return (theLsp.getStation() == station);
	}
}