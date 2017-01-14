package com.gaocan.train.train;

import java.util.Comparator;

import org.jpenguin.graph.Path;

public class DepartTimeComparator implements Comparator<Path> {

    public int compare(Path path1, Path path2) {
        TrainTrip trip1 = new TrainTrip(path1);
        TrainTrip trip2 = new TrainTrip(path2);
        
        return trip1.getTripStartTime().compareTo(trip2.getTripStartTime());
    }

}
