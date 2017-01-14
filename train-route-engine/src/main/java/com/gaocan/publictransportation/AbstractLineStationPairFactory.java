package com.gaocan.publictransportation;



/**
 * An abstract factory for constructing LSPs out of a string
 */
public abstract class AbstractLineStationPairFactory {
    /**
     * purely create a Lsp and append it to the line without it being in any schedule, used outside of a schedule context 
     * @param line
     * @param station
     * @param extraData
     * @return
     * @throws InvalidScheduleDataException
     */
    public LineStationPair createLsp (Line line, Station station,  String extraData) throws InvalidScheduleDataException {
        return createLsp(null, line, station, extraData); 
    }
    
    /**
     * 
     * @param sched the schedule to which the line and station belong, used for some intra-schedule logic 
     * @param line
     * @param station
     * @param extraData
     * @return
     * @throws InvalidScheduleDataException
     */
    public abstract LineStationPair createLsp (Schedule sched, Line line, Station station,
        String extraData) throws InvalidScheduleDataException;
      
	/** if true then do not read this lsp into schedule */
	public abstract boolean isLspInvalid(Line line, Station station) ; 
	/**
	 * Does the textual representation of the LSP in the schedule data contain extra info besides station name
	 * @return
	 */
	public abstract boolean lspHasExtraData();	
}
