package com.gaocan.train.train;

import com.gaocan.publictransportation.IRoutePlanner;

public interface IRoutePlannerFactory {
	public IRoutePlanner getTrainRoutePlanner();
	public TrainPriceImporter getTrainPriceImporter();	
}
