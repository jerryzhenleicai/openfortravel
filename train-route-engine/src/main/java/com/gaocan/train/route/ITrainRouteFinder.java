package com.gaocan.train.route;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import com.gaocan.publictransportation.NoSuchStationException;
import com.gaocan.publictransportation.Schedule;
import com.gaocan.train.train.TrainPriceImporter;
import com.gaocan.train.train.TrainTrip;

public interface ITrainRouteFinder {
    public List<TrainTrip> findTrainRidesBetweenCityPair(TrainSearchRequest rec) 
         throws RouteFinderTooBusyException, NoSuchStationException, ExceedSearchQuotaException ;
    public BlockingQueue<TrainSearchRequest> getClientQueue();
    public Schedule getSchedule();
    public TrainPriceImporter getTrainPricer();



                  
}
