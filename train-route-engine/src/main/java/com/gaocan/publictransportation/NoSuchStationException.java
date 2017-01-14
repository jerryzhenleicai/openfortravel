package com.gaocan.publictransportation;

public class NoSuchStationException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = -2089643070546187911L;

    public NoSuchStationException(String name) {
        super(name);
    }

}
