package com.gaocan.publictransportation;


/**
 * Given a string construct the right station class
 */
public interface AbstractLineFactory {
	public Line createLine(String name, String extraData);
	/**
	 * Does the textual representation of the line in the schedule data contain extra info besides line name
	 * @return
	 */
	public boolean lineHasExtraData();
}
