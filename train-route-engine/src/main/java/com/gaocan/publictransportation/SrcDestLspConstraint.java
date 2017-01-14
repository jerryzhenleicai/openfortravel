package com.gaocan.publictransportation;

/**
 * A predicate on LineStationPair for restricting only certain lines to be considered during a path search such as time constraint 
 */
public interface SrcDestLspConstraint {
	/** this lsp can be the starting node on a path ? */
	public boolean isValidDepartLsp(LineStationPair lsp);
	/** this lsp can be the ending  node on a path ? */
	public boolean isValidArrivalLsp(LineStationPair lsp);
}



