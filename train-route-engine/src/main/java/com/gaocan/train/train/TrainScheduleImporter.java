/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.gaocan.train.train;

import com.gaocan.publictransportation.InvalidScheduleDataException;

import java.util.HashSet;

/**
 * Interface for importing different forms of schedule data (HTML, txt)
 * into memory in the form of a train set and a line set.
 * Used by RailBuilder
 */
public interface TrainScheduleImporter
{

   // two sets are empty when passed in and populated when done
   public void buildStationLineSets(HashSet stationSet, HashSet trainLineSet)
      throws InvalidScheduleDataException;
}



