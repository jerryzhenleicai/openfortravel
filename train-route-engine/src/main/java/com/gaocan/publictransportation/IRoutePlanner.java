package com.gaocan.publictransportation;

import java.util.List;

import org.jpenguin.graph.Path;

public interface IRoutePlanner {

	/**
	 * return a list of  paths sorted by cost between src and dest areas.
	 * @param srcAreaName name of the general area of user's starting point, example is a CityRegion for bus
	 * @param destAreaName name of the general area of user's ending point, example is a CityRegion for bus
	 * @return list of Paths
	 */
	public abstract List<Path> getShortestPaths(String srcAreaName, String destAreaName) throws NoSuchStationException;

	public abstract void setSchedule(Schedule sched);

	public abstract Schedule getSchedule();

	public abstract void setSrcDestLspConstraint(
			SrcDestLspConstraint srcDestLspConstraint);

	public abstract void setPathsToGetForEachLine(int pathsToGetForEachLine);

}