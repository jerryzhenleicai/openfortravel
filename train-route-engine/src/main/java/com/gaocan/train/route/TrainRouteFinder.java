package com.gaocan.train.route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jpenguin.graph.Path;

import com.gaocan.publictransportation.IRoutePlanner;
import com.gaocan.publictransportation.NoSuchStationException;
import com.gaocan.publictransportation.RoutePlanner;
import com.gaocan.publictransportation.Schedule;
import org.jpenguin.ICityNameSource;
import com.gaocan.train.train.ArrivalDepartureTimeConstraint;
import com.gaocan.train.train.ArriveTimeComparator;
import com.gaocan.train.train.DepartTimeComparator;
import com.gaocan.train.train.DumbPathFilter;
import com.gaocan.train.train.LessRouteMoneytaryCost;
import com.gaocan.train.train.LessRouteTimeCost;
import com.gaocan.train.train.Settings;
import com.gaocan.train.train.TrainPriceImporter;
import com.gaocan.train.train.TrainTrip;

/**
 * given source and dest cities find list of train routes for them
 * 
 * @TODO change queue to a concurrent hashmap
 * 
 * @author zcai
 * 
 */
public class TrainRouteFinder implements ITrainRouteFinder {
    public static Map<String, String[]> RemappedCityNames = new HashMap<String, String[]>();

    static {
        RemappedCityNames.put("绩溪", new String[] { "绩溪县" });
        RemappedCityNames.put("弋阳", new String[] { "弋阳东" });
    }

    /** requests are queued */
    private LinkedBlockingQueue<TrainSearchRequest> clientQueue = new LinkedBlockingQueue<TrainSearchRequest>();

    private RouteParam param;

    private ICityNameSource cityNameSource;

    private RoutePlannerFactory plannerFac = null;

    public BlockingQueue<TrainSearchRequest> getClientQueue() {
        return clientQueue;
    }

    public void setParameter(RouteParam p) {
        param = p;
    }

    public TrainPriceImporter getTrainPricer() {
        return getRoutePlannerFactory().getTrainPriceImporter();
    }

    public Schedule getSchedule() {
        return getRoutePlannerFactory().getTrainRoutePlanner().getSchedule();
    }

    private RoutePlannerFactory getRoutePlannerFactory() {
        if (plannerFac != null) {
            return plannerFac;
        }
        synchronized (this) {
            if (plannerFac != null) {
                return plannerFac;
            }
            plannerFac = new RoutePlannerFactory(param.getSchedDirectory(), param.isUsePrecompute(),
                    param.isSchedEditable(), cityNameSource);
            return plannerFac;
        }
    }

    public List<TrainTrip> findTrainRidesBetweenCityPair(TrainSearchRequest req) throws RouteFinderTooBusyException,
            ExceedSearchQuotaException, NoSuchStationException {

        String src = req.getSrcStation();
        String dest = req.getDestStation();
        List<TrainTrip> result = new ArrayList<TrainTrip>();
        // station translation
        if (RemappedCityNames.get(src) != null) {
            for (String s : RemappedCityNames.get(src)) {
                TrainSearchRequest req1 = req.getClone();
                req1.setSrcStation(s);
                result.addAll(findTrainRidesBetweenCityPair(req1));
            }
            return result;
        }
        if (RemappedCityNames.get(dest) != null) {
            for (String s : RemappedCityNames.get(dest)) {
                TrainSearchRequest req1 = req.getClone();
                req1.setDestStation(s);
                result.addAll(findTrainRidesBetweenCityPair(req1));
            }
            return result;
        }
        // an escaped string for src or dest ? means no translation like LISP
        if (src.startsWith("\'")) {
            src = src.substring(1);
        }
        if (dest.startsWith("\'")) {
            dest = dest.substring(1);
        }

        IRoutePlanner planner = getRoutePlannerFactory().getTrainRoutePlanner();
        boolean queued = false;
        // // Logging.debug(this, "from " + req.getSrcStation() + " to " +
        // req.getDestStation());
        try {
            if (clientQueue.size() >= param.getMaxQueueLength()) {
                // Logging.debug(this, "denied because too busy");
                throw new RouteFinderTooBusyException();
            }
            Iterator<TrainSearchRequest> it = getClientQueue().iterator();
            while (it.hasNext()) {
                TrainSearchRequest rec = it.next();
                if (rec.getIp().equals(req.getIp()) && !req.getIp().startsWith("192.168.1")
                        && !req.getIp().startsWith("127.0.0.1")) {
                    // Logging.debug(this,
                    // "denied because same IP making too many requests");
                    throw new ExceedSearchQuotaException();
                }
            }

            long whenQed = System.currentTimeMillis();

            // make sure only one client can access the planner at a time
            // add myself to queue being served
            while (queued == false) {
                try {
                    clientQueue.put(req);
                    queued = true;
                } catch (InterruptedException ie) {
                }
            }

            List<Path> topGraphPaths = null;

            // search optimal route
            synchronized (planner) {
                long whenLeftQ = System.currentTimeMillis();
                // Logging.debug(this, "waited in queue for " + (whenLeftQ -
                // whenQed) + " ms");

                planner.setPathsToGetForEachLine(RoutePlanner.DEFAULT_PATHS_FOR_EACH_LINE);
                // clear any time constraint
                planner.setSrcDestLspConstraint(null);

                if (req.isSrcTimeContraint() || req.isDestTimeContraint()) {
                    planner.setSrcDestLspConstraint(new ArrivalDepartureTimeConstraint(req.isDestTimeContraint(), req
                            .isSrcTimeContraint(), req.getEarliestArriveHour(), req.getLatestArriveHour(), req
                            .getEarliestDepartHour(), req.getLatestDepartHour()));
                }
                topGraphPaths = planner.getShortestPaths(src, dest);
                // Logging.debug(this, "consumed the planner " +
                // (System.currentTimeMillis() - whenLeftQ) +
                // " ms to compute paths (only graph part)");
            } // synchronized planner

            if (topGraphPaths.size() == 0) {
                return new ArrayList<TrainTrip>();
            }
            // prefilter paths to only keep shorter ones
            Collections.sort(topGraphPaths, new LessRouteTimeCost());
            // prefilter to only keep to top 150 or so routes
            if (topGraphPaths.size() > Settings.NUM_ROUTES_PREFILTER) {
                topGraphPaths = topGraphPaths.subList(0, Settings.NUM_ROUTES_PREFILTER);
            }

            // Logging.debug(this, "graph paths found : " + topGraphPaths);

            // price
            switch (req.getSortOrder()) {
            case TrainSearchRequest.SORT_BY_PRICE:
                Collections.sort(topGraphPaths, new LessRouteMoneytaryCost(getRoutePlannerFactory()
                        .getTrainPriceImporter()));
                break;
            case TrainSearchRequest.SORT_BY_TRIP_TIME:
                // already sorted in prefiltering
                break;
            case TrainSearchRequest.SORT_BY_ARRIVE_TIME:
                // arrive time
                Collections.sort(topGraphPaths, new ArriveTimeComparator());
                break;
            case TrainSearchRequest.SORT_BY_DEPART_TIME:
                // depart time
                Collections.sort(topGraphPaths, new DepartTimeComparator());
                break;
            }

            // remove invalid routes
            List<TrainTrip> rides = DumbPathFilter.filterOutDumbPaths(topGraphPaths);
            // if sort by price or trip time, truncate
            switch (req.getSortOrder()) {
            case TrainSearchRequest.SORT_BY_PRICE:
            case TrainSearchRequest.SORT_BY_TRIP_TIME:
                if (rides.size() > Settings.MAX_ROUTES_SHOWN_TO_USER) {
                    rides = rides.subList(0, Settings.MAX_ROUTES_SHOWN_TO_USER);
                }
                break;
            case TrainSearchRequest.SORT_BY_ARRIVE_TIME:
            case TrainSearchRequest.SORT_BY_DEPART_TIME:
                // cannot truncate list here
                break;
            }
            // Logging.debug(this, "final rides found : " + rides);
            return rides;
        } finally {
            // remove myself from queue, note the head of queue may not be
            // myself as I may have grabbed the planner before the one who
            // enqeued before me
            if (queued) {
                boolean dequed = false;
                while (dequed == false) {
                    try {
                        clientQueue.take();
                        dequed = true;
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    public ICityNameSource getCityNameSource() {
        return cityNameSource;
    }

    public void setCityNameSource(ICityNameSource cityNameSource) {
        this.cityNameSource = cityNameSource;
    }

}
