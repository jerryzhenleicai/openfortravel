package com.gaocan.train;

import com.gaocan.train.route.ITrainRouteFinder;
import com.gaocan.train.route.RouteParam;
import com.gaocan.train.route.TrainRouteFinder;
import org.jpenguin.ICityNameSource;
import org.jpenguin.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * a pool of 2 route finders and return the one with shortest queue to client
 * 
 * @author zcai
 * 
 */
public class RouteFinderFactory {
    // due to memory limits , set the pool size to 1, it could be higher for high traffic sites
    final TrainRouteFinder[] rfPool = new TrainRouteFinder[1];
    
    public RouteFinderFactory(ICityNameSource cityNameSource) {
        // common parameters
        RouteParam rp = new RouteParam();
        String qLen = Utils.getProperty("train.sched.maxqueuelen");
        if (qLen == null)
        	qLen = "5";
        rp.setMaxQueueLength(Integer.parseInt(qLen));
        String sDir = Utils.getProperty("train.sched.dir");
        rp.setSchedDirectory(sDir != null ? sDir : "/var/opt/traindata");
        rp.setUsePrecompute(true);
        rp.setSchedEditable(true);

        for (int p = 0; p < rfPool.length ; p++ ) {
            rfPool[p] = new TrainRouteFinder();
            rfPool[p].setParameter(rp);
            rfPool[p].setCityNameSource(cityNameSource);
        }
    }

    public Collection<ITrainRouteFinder> getAllFindersInPool() {
        List<ITrainRouteFinder> res = new ArrayList<ITrainRouteFinder>();
        for (int i = 0 ; i < rfPool.length ; i++) {
            res.add(rfPool[i]);
        }
        return res;
    }
    
    public ITrainRouteFinder get() {
        int minQueueSize = -1;
        int leastBusyRf = -1;
        for (int p = 0; p < rfPool.length ; p++ ) {
            TrainRouteFinder rf = rfPool[p]; 
            int queueLen = rf.getClientQueue().size();
            if (minQueueSize < 0 || queueLen < minQueueSize) { 
                leastBusyRf = p;
                minQueueSize = queueLen;
            } 
        }
        //Logging.debug(this, "", " use route finder " + leastBusyRf + " from the pool");
        return rfPool[leastBusyRf];
    }
}
