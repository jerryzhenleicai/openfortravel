package com.gaocan.train.route;

import java.io.File;

import org.jpenguin.ICityNameSource;

import com.gaocan.publictransportation.IRoutePlanner;
import com.gaocan.publictransportation.RoutePlanner;
import com.gaocan.publictransportation.Schedule;
import com.gaocan.publictransportation.ScheduleFactory;
import com.gaocan.train.train.City;
import com.gaocan.train.train.IRoutePlannerFactory;
import com.gaocan.train.train.Settings;
import com.gaocan.train.train.TrainPriceImporter;
import com.gaocan.train.train.TrainSchedule;
import com.gaocan.train.train.TrainScheduleFactory;

public class RoutePlannerFactory implements IRoutePlannerFactory {
	private RoutePlanner planner = null;
	private TrainPriceImporter train_price_importer ;
    
	/**
	 * 
	 * @param schedTopDir
	 * @param usePrecompute
	 * @param editableSchedCopy not used any more
	 */
	RoutePlannerFactory(String schedTopDir, boolean usePrecompute, boolean editableSchedCopy,
            ICityNameSource cityNameSource) {
        // init planner
        try {
            ScheduleFactory sf = new TrainScheduleFactory();
            Schedule sched = new TrainSchedule("trainsched");
            // populate cities in database as  initial areas in sched
            for (String name : cityNameSource.getCityNames()) {
                sched.addAreaServedByStations(new City(name));
            }
            String schedPath = schedTopDir + File.separator + "sched.txt";
            sched.setTextFilePath(schedPath);
            // read the schedule data
            sf.buildSchedule(sched, schedPath);
            // create the planner , but init it lazily because the graph building take a long time
            planner = new RoutePlanner(sched, schedTopDir + File.separator + Settings.top_city_file_name,
                    schedTopDir + File.separator + Settings.astar_from_top_city_file_name,
                    schedTopDir + File.separator + Settings.astar_to_top_city_file_name,
                    schedTopDir + File.separator + Settings.astar_top_city_pair_file_name, 
                    usePrecompute,  false);
            
            train_price_importer  = new TrainPriceImporter();
            train_price_importer.loadPriceTable(schedTopDir + File.separator + Settings.price_table_file_name);
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
	}
	
	public TrainPriceImporter getTrainPriceImporter() {
		return train_price_importer; 
	}
	/**
	 * construct the route planners lazily and return to caller
	 */
	public IRoutePlanner getTrainRoutePlanner() {
		if (!planner.isInitialized()) {
			synchronized (this) {
				if (!planner.isInitialized()) {
					planner.init();
				}
			}
		}
		return planner;
	}
}
