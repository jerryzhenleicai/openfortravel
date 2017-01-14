package com.gaocan.publictransportation;

import java.util.Iterator;

import org.jpenguin.graph.UnaryPredicate;

/**
 */
public class IsLspInArea implements UnaryPredicate {
    private static final long serialVersionUID = -7644725630082350637L;

    AreaServedByStations area;

    public IsLspInArea(AreaServedByStations a) {
        area = a;
    }

    public boolean isTrueOn(Object lsp) {
        LineStationPair theLsp = (LineStationPair) lsp;
        Iterator<AreaServedByStations> ait = theLsp.getStation().getAreasServed();
        while (ait.hasNext()) {
            if (ait.next() == this.area)
                return true;
        }
        return false;
    }

}
